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
package com.exadel.aem.toolkit.core.ai.services;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.ai.AssistantConstants;
import com.exadel.aem.toolkit.core.ai.models.facility.Facility;
import com.exadel.aem.toolkit.core.ai.models.facility.Setting;
import com.exadel.aem.toolkit.core.ai.models.facility.SimpleFacility;
import com.exadel.aem.toolkit.core.ai.models.solution.Solution;
import com.exadel.aem.toolkit.core.utils.ObjectConversionUtil;

@Component(service = AiService.class, immediate = true, property = "service.ranking:Integer=101")
@Designate(ocd = WritesonicService.Config.class)
public class WritesonicService implements AiService {
    private static final Logger LOG = LoggerFactory.getLogger(WritesonicService.class);

    private static final String HEADER_API_KEY = "X-API-KEY";
    private static final String PN_DETAIL = "detail";
    private static final String PN_ENDPOINT = "endpoint";
    private static final String PN_ENGINE = "engine";
    private static final String PN_IMAGE_SIZE = "size";
    private static final String PN_IMAGE_HEIGHT = "image_height";
    private static final String PN_IMAGE_WIDTH = "image_width";
    private static final String PN_IMAGES_COUNT = "num_images";
    private static final String PN_LANGUAGE = "language";
    private static final String PN_MESSAGE = "msg";
    private static final String PN_PROMPT = "prompt";
    private static final String PN_TONE = "tone_of_voice";

    private static final String DEFAULT_ENGINE = "economy";
    private static final String DEFAULT_LANGUAGE = "en";
    private static final String DEFAULT_TONE = "professional";

    private static final String DEFAULT_IMAGE_SIZE = "512x512";
    private static final int DEFAULT_IMAGES_COUNT = 3;

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

    private static final int DEFAULT_ENDPOINT_VERSION = 1;

    private static final Pattern PATTERN_ENDPOINT_VERSION = Pattern.compile("/v(\\d)/");
    private static final Pattern PATTERN_IMAGE_SIZE = Pattern.compile("(\\d+)x(\\d+)");

    private List<Facility> facilities;

    private boolean enabled;
    private String contentEndpoint;
    private String imagesEndpoint;
    private String defaultEngine;
    private String defaultLanguage;
    private String defaultTone;
    private String defaultImageSize;
    private int defaultImagesCount;
    private String api1Key;
    private String api2Key;
    private int timeout;

    @Activate
    @Modified
    private void init(Config config) {
        if (facilities == null) {
            facilities = Arrays.asList(
                new Expand(),
                new Shorten(),
                new Rephrase(),
                new ProduceImage());
        }
        enabled = config.enabled();
        contentEndpoint = config.contentEndpoint();
        imagesEndpoint = config.imagesEndpoint();
        defaultEngine = config.engine();
        defaultLanguage = config.language();
        defaultTone = config.tone();
        defaultImageSize = config.imageSize();
        defaultImagesCount = config.imagesCount();
        api1Key = config.api1Key();
        api2Key = config.api2Key();
        timeout = config.timeout();
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
        return enabled;
    }

    @Override
    public List<Facility> getFacilities() {
        return facilities;
    }

    private Solution execute(String endpoint, String command, String payload, ValueMap args) {
        String effectiveEndpoint = getEffectiveEndpoint(endpoint, command, args);

        HttpPost request = new HttpPost(effectiveEndpoint);
        request.setHeader(HEADER_API_KEY, getEffectiveApiKey(effectiveEndpoint));
        request.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        request.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        request.setEntity(new StringEntity(payload, StandardCharsets.UTF_8));

        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(timeout)
            .setConnectionRequestTimeout(timeout)
            .setSocketTimeout(timeout)
            .build();

        try (
            CloseableHttpClient client = HttpClientBuilder
                .create()
                .setDefaultRequestConfig(requestConfig)
                .build();
            CloseableHttpResponse response = client.execute(request)
        ) {
            Solution solution = getSolution(args, response);
            EntityUtils.consume(response.getEntity());
            return solution;
        } catch (IOException e) {
            LOG.error("Writesonic service request failed", e);
            return Solution.from(args).withMessage(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private String getEffectiveEndpoint(String endpoint, String command, ValueMap args) {
        String effectiveEngine = args.get(PN_ENGINE, defaultEngine);
        String effectiveLanguage = args.get(PN_LANGUAGE, defaultLanguage);
        return args.get(PN_ENDPOINT, endpoint)
            .replace("{command}", command)
            .replace("{engine}", effectiveEngine)
            .replace("{language}", effectiveLanguage);
    }

    private int getEndpointVersion(String endpoint) {
        Matcher matcher = PATTERN_ENDPOINT_VERSION.matcher(endpoint);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : DEFAULT_ENDPOINT_VERSION;
    }

    private String getEffectiveApiKey(String endpoint) {
        return getEndpointVersion(endpoint) == DEFAULT_ENDPOINT_VERSION ? api1Key : api2Key;
    }

    private String getContentRequestPayload(String payloadKey, ValueMap args) {
        String effectiveText = args.get(CoreConstants.PN_TEXT, StringUtils.EMPTY);
        String effectiveTone = args.get(PN_TONE, defaultTone);
        Map<String, String> payload = new HashMap<>();
        payload.put(payloadKey, effectiveText);
        payload.put(PN_TONE, effectiveTone);
        return ObjectConversionUtil.toJson(payload);
    }


    private Solution getSolution(Map<String, Object> args, CloseableHttpResponse response) throws IOException {
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

    private String extractExceptionMessage(String value) {
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

    /* ----------
       Facilities
       ---------- */

    private abstract static class WritesonicFacility extends SimpleFacility {

        private static final Setting V1_ENGINE = Setting
            .builder()
            .id(PN_ENGINE)
            .title("Engine")
            .option(DEFAULT_ENGINE)
            .option("business")
            .defaultValue(DEFAULT_ENGINE)
            .build();

        private static final Setting V2_ENGINE = Setting
            .builder()
            .id(PN_ENGINE)
            .title("Engine")
            .option(DEFAULT_ENGINE)
            .option("average")
            .option("good")
            .option("premium")
            .defaultValue(DEFAULT_ENGINE)
            .build();

        private static final Setting LANGUAGE = Setting
            .builder()
            .id(PN_LANGUAGE)
            .title( "Language")
            .option(DEFAULT_LANGUAGE, "English")
            .option("nl", "Dutch")
            .option("fr", "French")
            .option("de", "German")
            .option("it", "Italian")
            .option("pl", "Polish")
            .option("es", "Spanish")
            .option("pt-pt", "Portuguese")
            .option("pt-br", "Portuguese (Brazil)")
            .option("ru", "Russian")
            .option("ja", "Japanese")
            .option("zh", "Chinese")
            .option("bg", "Bulgarian")
            .option("cs", "Chech")
            .option("da", "Danish")
            .option("el", "Greek")
            .option("hu", "Hungarian")
            .option("lt", "Lithuanian")
            .option("lv", "Latvian")
            .option("ro", "Romanian")
            .option("sk", "Slovak")
            .option("sv", "Slovenian")
            .option("fi", "Finnish")
            .option("et", "Estonian")
            .defaultValue(DEFAULT_LANGUAGE)
            .build();

        private static final Setting TONE = Setting
            .builder()
            .id(PN_TONE)
            .title("Tone of Voice")
            .option("excited")
            .option(DEFAULT_TONE)
            .option("funny")
            .option("encouraging")
            .option("dramatic")
            .option("witty")
            .option("sarcastic")
            .option("engaging")
            .option("creative")
            .defaultValue(DEFAULT_TONE)
            .build();

        private static final Setting IMAGE_SIZE = Setting
            .builder()
            .id(PN_IMAGE_SIZE)
            .title("Image size")
            .option(DEFAULT_IMAGE_SIZE, "Square (512x512)")
            .option("768x512", "Horizontal (768x512)")
            .option("512x768", "Vertical (512x768)")
            .defaultValue(DEFAULT_IMAGE_SIZE)
            .build();

        static final List<Setting> CONTENT_V1_SETTINGS = Arrays.asList(
            V1_ENGINE,
            LANGUAGE,
            TONE);

        static final List<Setting> CONTENT_V2_SETTINGS = Arrays.asList(
            V2_ENGINE,
            LANGUAGE,
            TONE);

        static final List<Setting> IMAGE_SETTINGS = Collections.singletonList(IMAGE_SIZE);

        @Override
        public String getVendorName() {
            return VENDOR_NAME;
        }

        @Override
        public Solution execute(SlingHttpServletRequest request) {
            return execute(getArguments(request));
        }

        abstract Solution execute(ValueMap args);
    }

    private abstract class WritesonicContentFacility extends WritesonicFacility {

        @Override
        public List<Setting> getSettings() {
            return getEndpointVersion(contentEndpoint) == DEFAULT_ENDPOINT_VERSION ? CONTENT_V1_SETTINGS : CONTENT_V2_SETTINGS;
        }
    }

    private class Expand extends WritesonicContentFacility {

        @Override
        public String getId() {
            return "text.expand.ws";
        }

        @Override
        public String getTitle() {
            return "Expand";
        }

        @Override
        public String getIcon() {
            return ICON_TEXT_ADD;
        }

        @Override
        Solution execute(ValueMap args) {
            return WritesonicService.this.execute(
                contentEndpoint,
                "sentence-expand",
                getContentRequestPayload("content_to_expand", args),
                args);
        }
    }

    private class Shorten extends WritesonicContentFacility {

        @Override
        public String getId() {
            return "text.shorten.ws";
        }

        @Override
        public String getTitle() {
            return "Shorten";
        }

        @Override
        public String getIcon() {
            return ICON_TEXT_REMOVE;
        }

        @Override
        Solution execute(ValueMap args) {
            return WritesonicService.this.execute(
                contentEndpoint,
                "content-shorten",
                getContentRequestPayload("content_to_shorten", args),
                args);
        }
    }

    private class Rephrase extends WritesonicContentFacility {

        @Override
        public String getId() {
            return "text.rephrase.ws";
        }

        @Override
        public String getTitle() {
            return "Rephrase";
        }

        @Override
        public String getIcon() {
            return ICON_TEXT_EDIT;
        }

        @Override
        Solution execute(ValueMap args) {
            return WritesonicService.this.execute(
                contentEndpoint,
                "content-rephrase",
                getContentRequestPayload("content_to_rephrase", args),
                args);
        }
    }

    private class ProduceImage extends WritesonicFacility {

        @Override
        public String getId() {
            return "image.produce.ws";
        }

        @Override
        public String getTitle() {
            return "Produce Image";
        }

        @Override
        public String getIcon() {
            return ICON_IMAGE_ADD;
        }

        @Override
        public int getRanking() {
            return 1001;
        }

        @Override
        public List<Setting> getSettings() {
            return IMAGE_SETTINGS;
        }

        @Override
        public Solution execute(ValueMap args) {
            return WritesonicService.this.execute(
                imagesEndpoint,
                StringUtils.EMPTY,
                getImageGenerationPayload(args),
                args);
        }

        private String getImageGenerationPayload(ValueMap args) {
            Map<String, Object> properties = new HashMap<>();
            properties.put(PN_PROMPT, args.get(CoreConstants.PN_TEXT));
            String size = args.get(PN_IMAGE_SIZE, defaultImageSize);
            Matcher sizeMatcher = PATTERN_IMAGE_SIZE.matcher(size);
            if (sizeMatcher.find()) {
                properties.put(PN_IMAGE_WIDTH, Integer.parseInt(sizeMatcher.group(1)));
                properties.put(PN_IMAGE_HEIGHT, Integer.parseInt(sizeMatcher.group(2)));
            }
            properties.put(PN_IMAGES_COUNT, args.get(PN_IMAGES_COUNT, defaultImagesCount));
            return ObjectConversionUtil.toJson(properties);
        }
    }


    /* --------
       Settings
       -------- */

    @ObjectClassDefinition(name = "EToolbox Authoring Kit - Assistant: Writesonic Integration")
    public @interface Config {

        @AttributeDefinition(name = "Enabled")
        boolean enabled() default true;

        @AttributeDefinition(name = "Content Endpoint")
        String contentEndpoint() default "https://api.writesonic.com/v1/business/content/{command}?engine={engine}&language={language}";

        @AttributeDefinition(name = "Images Endpoint")
        String imagesEndpoint() default "https://api.writesonic.com/v1/business/photosonic/generate-image";

        @AttributeDefinition(name = "API (v.1) Key")
        String api1Key() default StringUtils.EMPTY;

        @AttributeDefinition(name = "API (v.2) Key")
        String api2Key() default StringUtils.EMPTY;

        @AttributeDefinition(name = "Default Engine")
        String engine() default DEFAULT_ENGINE;

        @AttributeDefinition(name = "Default Language")
        String language() default "en";

        @AttributeDefinition(name = "Default Tone of Voice")
        String tone() default DEFAULT_TONE;

        @AttributeDefinition(name = "Default Output Image Size")
        String imageSize() default DEFAULT_IMAGE_SIZE;

        @AttributeDefinition(name = "Number of image choices")
        int imagesCount() default DEFAULT_IMAGES_COUNT;

        @AttributeDefinition(name = "Connection Timeout (ms)")
        int timeout() default AssistantConstants.HTTP_TIMEOUT;
    }
}
