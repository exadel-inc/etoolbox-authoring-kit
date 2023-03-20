/*
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.exadel.aem.toolkit.core.assistant.services.writesonic;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.assistant.models.facilities.Facility;
import com.exadel.aem.toolkit.core.assistant.models.solutions.Solution;
import com.exadel.aem.toolkit.core.assistant.services.AssistantService;
import com.exadel.aem.toolkit.core.utils.HttpClientFactory;
import com.exadel.aem.toolkit.core.utils.ObjectConversionUtil;

@Component(service = AssistantService.class, immediate = true, property = "service.ranking:Integer=101")
@Designate(ocd = WritesonicServiceConfig.class)
public class WritesonicService implements AssistantService {
    private static final Logger LOG = LoggerFactory.getLogger(WritesonicService.class);

    private static final String HEADER_API_KEY = "X-API-KEY";
    private static final String PN_DETAIL = "detail";
    private static final String PN_ENDPOINT = "endpoint";
    private static final String PN_MESSAGE = "msg";
    private static final String VENDOR_NAME = "Writesonic";
    private static final String LOGO_RESOURCE = "assistant/logo-writesonic";
    private static final String LOGO;
    static {
        URL logoUrl = WritesonicService.class.getClassLoader().getResource(LOGO_RESOURCE);
        String logo = null;
        try {
            logo = logoUrl != null ? IOUtils.toString(logoUrl, StandardCharsets.UTF_8).trim() : null;
        } catch (IOException e) {
            LOG.error("Could not read resource at {}", LOGO_RESOURCE, e);
        }
        LOGO = logo;
    }

    private static final String EXCEPTION_REQUEST_FAILED = "Writesonic service request failed";
    private static final String EXCEPTION_TIMEOUT = "Connection to {} timed out";

    private static final Pattern PATTERN_ENDPOINT_VERSION = Pattern.compile("/v(\\d)/");
    private static final Pattern PATTERN_IMAGE_SIZE = Pattern.compile("(\\d+)x(\\d+)");

    private List<Facility> facilities;
    private WritesonicServiceConfig config;

    @Activate
    @Modified
    private void init(WritesonicServiceConfig config) {
        if (facilities == null) {
            facilities = Arrays.asList(
                new ExpandFacility(this),
                new ShortenFacility(this),
                new RephraseFacility(this),
                new ProduceImageFacility(this));
        }
        this.config = config;
    }

    @Override
    public String getVendorName() {
        return VENDOR_NAME;
    }

    @Override
    public String getLogo() {
        return LOGO;
    }

    @Override
    public boolean isEnabled() {
        return config.enabled();
    }

    @Override
    public List<Facility> getFacilities() {
        return facilities;
    }

    WritesonicServiceConfig getConfig() {
        return config;
    }

    Solution executeContentChange(String command, String payloadKey, ValueMap args) {
        return execute(config.contentEndpoint(), command, getContentRequestPayload(payloadKey, args), args);
    }

    Solution executeImageGeneration(ValueMap args) {
        return execute(config.imagesEndpoint(), StringUtils.EMPTY, getImageGenerationPayload(args), args);
    }

    private Solution execute(String endpoint, String command, String payload, ValueMap args) {
        String effectiveEndpoint = getEffectiveEndpoint(endpoint, command, args);

        HttpPost request = new HttpPost(effectiveEndpoint);
        request.setHeader(HEADER_API_KEY, getEffectiveApiKey(effectiveEndpoint));
        request.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        request.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        request.setEntity(new StringEntity(payload, StandardCharsets.UTF_8));

        try (
            CloseableHttpClient client = HttpClientFactory.newClient(config.timeout());
            CloseableHttpResponse response = client.execute(request)
        ) {
            Solution solution = parseWritesonicResponse(args, response);
            EntityUtils.consume(response.getEntity());
            return solution;
        } catch (IOException e) {
            LOG.error("Writesonic service request failed", e);
            return Solution.from(args).withMessage(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private String getEffectiveEndpoint(String endpoint, String command, ValueMap args) {
        String effectiveEngine = args.get(WritesonicConstants.PN_ENGINE, config.engine());
        String effectiveLanguage = args.get(WritesonicConstants.PN_LANGUAGE, config.language());
        return args.get(PN_ENDPOINT, endpoint)
            .replace("{command}", command)
            .replace("{engine}", effectiveEngine)
            .replace("{language}", effectiveLanguage);
    }

    private String getEffectiveApiKey(String endpoint) {
        return getEndpointVersion(endpoint) == WritesonicConstants.DEFAULT_ENDPOINT_VERSION
            ? config.api1Key()
            : config.api2Key();
    }

    private String getContentRequestPayload(String payloadKey, ValueMap args) {
        String effectiveText = args.get(CoreConstants.PN_TEXT, StringUtils.EMPTY);
        String effectiveTone = args.get(WritesonicConstants.PN_TONE, config.tone());
        Map<String, String> payload = new HashMap<>();
        payload.put(payloadKey, effectiveText);
        payload.put(WritesonicConstants.PN_TONE, effectiveTone);
        return ObjectConversionUtil.toJson(payload);
    }

    private String getImageGenerationPayload(ValueMap args) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(WritesonicConstants.PN_PROMPT, args.get(CoreConstants.PN_TEXT));
        String size = args.get(CoreConstants.PN_SIZE, config.imageSize());
        Matcher sizeMatcher = WritesonicService.PATTERN_IMAGE_SIZE.matcher(size);
        if (sizeMatcher.find()) {
            properties.put(WritesonicConstants.PN_IMAGE_WIDTH, Integer.parseInt(sizeMatcher.group(1)));
            properties.put(WritesonicConstants.PN_IMAGE_HEIGHT, Integer.parseInt(sizeMatcher.group(2)));
        }
        properties.put(
            WritesonicConstants.PN_IMAGES_COUNT,
            args.get(WritesonicConstants.PN_IMAGES_COUNT, config.imagesCount()));
        return ObjectConversionUtil.toJson(properties);
    }

    private static Solution parseWritesonicResponse(Map<String, Object> args, CloseableHttpResponse response) throws IOException {
        int statusCode = response.getStatusLine().getStatusCode();
        String responsePayload = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
        if (statusCode == HttpStatus.SC_OK) {
            Solution.Builder solutionBuilder = Solution.from(args);
            if (responsePayload.startsWith("[")) {
                return solutionBuilder.withJsonContent(CoreConstants.PN_OPTIONS, responsePayload);
            } else {
                return solutionBuilder.withJsonContent(responsePayload.replace("\"images\"", "\"options\""));
            }
        }
        String exceptionMessage = extractExceptionMessage(responsePayload);
        return StringUtils.isNotBlank(exceptionMessage)
            ? Solution.from(args).withMessage(HttpStatus.SC_BAD_REQUEST, exceptionMessage)
            : Solution.from(args).withMessage(HttpStatus.SC_BAD_REQUEST, "Failure: " + response.getStatusLine());
    }

    private static String extractExceptionMessage(String value) {
        JsonNode root;
        try {
            root = ObjectConversionUtil.toNodeTree(value);
        } catch (IOException e) {
            return value;
        }
        JsonNode detail = root.get(PN_DETAIL);
        if (detail == null) {
            return root.asText();
        }
        JsonNode detailEntry = detail.isArray() ? detail.get(0) : detail;
        JsonNode message = detailEntry.get(PN_MESSAGE);
        return message != null ? message.asText() : detailEntry.asText();
    }

    static int getEndpointVersion(String endpoint) {
        Matcher matcher = PATTERN_ENDPOINT_VERSION.matcher(endpoint);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : WritesonicConstants.DEFAULT_ENDPOINT_VERSION;
    }
}
