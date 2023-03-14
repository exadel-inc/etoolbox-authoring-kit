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
package com.exadel.aem.toolkit.core.assistant.services.openai;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
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

@Component(service = AssistantService.class, immediate = true, property = "service.ranking:Integer=100")
@Designate(ocd = OpenAiServiceConfig.class)
public class OpenAiService implements AssistantService {

    private static final Logger LOG = LoggerFactory.getLogger(OpenAiService.class);

    private static final String PN_CHOICES = "choices";
    private static final String PN_CHOICES_COUNT = "n";
    private static final String PN_DATA = "data";
    private static final String PN_ERROR = "error";
    private static final String PN_FINISH = "finish_reason";
    private static final String PN_INPUT = "input";
    private static final String PN_INSTRUCTION = "instruction";
    private static final String PN_MESSAGE = "message";
    private static final String PN_PROMPT = "prompt";
    private static final String PN_URL = "url";

    private static final String VALUE_LENGTH = "length";

    private static final String HTTP_HEADER_BEARER = "Bearer ";

    private static final String VENDOR_NAME = "OpenAI";
    private static final String LOGO_RESOURCE = "assistant/logo-openai";
    private static final String LOGO;
    static {
        URL logoUrl = OpenAiService.class.getClassLoader().getResource(LOGO_RESOURCE);
        String logo = null;
        try {
            logo = logoUrl != null ? IOUtils.toString(logoUrl, StandardCharsets.UTF_8).trim() : null;
        } catch (IOException e) {
            LOG.error("Could not read resource at {}", LOGO_RESOURCE, e);
        }
        LOGO = logo;
    }

    private OpenAiServiceConfig config;
    private List<Facility> facilities;

    @Activate
    @Modified
    private void init(OpenAiServiceConfig config) {
        this.config = config;
        if (facilities == null) {
            facilities = Arrays.asList(
                new ExpandFacility(this),
                new ShortenFacility(this),
                new RephraseFacility(this),
                new CorrectFacility(this),
                new ProduceImageFacility(this));
        }
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

    OpenAiServiceConfig getConfig() {
        return config;
    }

    Solution executeCompletion(ValueMap args, String defaultPrompt) {
        return execute(config.completionsEndpoint(), getCompletionRequestPayload(args, defaultPrompt), args);
    }

    Solution executeEdit(ValueMap args, String defautlInstruction) {
        return execute(config.editsEndpoint(), getEditRequestPayload(args, defautlInstruction), args);
    }

    Solution executeImageGeneration(ValueMap args) {
        return execute(config.imagesEndpoint(), getImageGenerationPayload(args), args);
    }

    private Solution execute(String endpoint, String payload, Map<String, Object> args) {
        HttpPost request = new HttpPost(endpoint);
        request.setHeader(HttpHeaders.AUTHORIZATION, HTTP_HEADER_BEARER + config.token());
        request.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        request.setEntity(new StringEntity(payload, StandardCharsets.UTF_8));

        try (
            CloseableHttpClient client = HttpClientFactory.newClient(config.timeout());
            CloseableHttpResponse response = client.execute(request)
        ) {
            Solution solution = parseOpenAiResponse(args, response);
            EntityUtils.consume(response.getEntity());
            return solution;
        } catch (IOException e) {
            LOG.error("OpenAI service request failed", e);
            return Solution.from(args).withMessage(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    String getCompletionRequestPayload(ValueMap args, String defaultPrompt) {
        Map<String, Object> properties = new HashMap<>();
        String prompt = extractPrompt(args, PN_PROMPT, defaultPrompt);
        properties.put(PN_PROMPT, prompt + StringUtils.SPACE + args.get(CoreConstants.PN_TEXT));
        properties.put(OpenAiConstants.PN_MODEL, args.get(OpenAiConstants.PN_MODEL, config.completionModel()));
        properties.put(OpenAiConstants.PN_MAX_TOKENS, args.get(OpenAiConstants.PN_MAX_TOKENS, config.textLength()));
        properties.put(OpenAiConstants.PN_TEMPERATURE, args.get(OpenAiConstants.PN_TEMPERATURE, config.temperature()));
        properties.put(PN_CHOICES_COUNT, config.choices());
        return ObjectConversionUtil.toJson(properties);
    }

    String getEditRequestPayload(ValueMap args, String defaultInstruction) {
        Map<String, Object> properties = new HashMap<>();
        String instruction = extractPrompt(args, PN_INSTRUCTION, defaultInstruction);
        properties.put(PN_INSTRUCTION, instruction);
        properties.put(OpenAiConstants.PN_MODEL, args.get(OpenAiConstants.PN_MODEL, config.editModel()));
        properties.put(PN_INPUT, args.get(CoreConstants.PN_TEXT));
        properties.put(OpenAiConstants.PN_TEMPERATURE, args.get(OpenAiConstants.PN_TEMPERATURE, config.temperature()));
        properties.put(PN_CHOICES_COUNT, config.choices());
        return ObjectConversionUtil.toJson(properties);
    }

    private String getImageGenerationPayload(Map<String, Object> args) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(PN_PROMPT, args.get(CoreConstants.PN_TEXT));
        properties.put(CoreConstants.PN_SIZE, args.getOrDefault(CoreConstants.PN_SIZE, config.imageSize()));
        properties.put(OpenAiService.PN_CHOICES_COUNT, config.choices());
        return ObjectConversionUtil.toJson(properties);
    }

    private static String extractPrompt(ValueMap args, String key, String defaultValue) {
        String result = args.get(key, StringUtils.EMPTY);
        result = StringUtils.defaultIfBlank(result, defaultValue).trim();
        if (!result.endsWith(CoreConstants.SEPARATOR_COLON)) {
            result += CoreConstants.SEPARATOR_COLON;
        }
        return result;
    }

    private static Solution parseOpenAiResponse(Map<String, Object> args, CloseableHttpResponse response) throws IOException {
        JsonNode jsonNode = ObjectConversionUtil.toNodeTree(response.getEntity().getContent());
        JsonNode errorNode = jsonNode.get(PN_ERROR);
        if (errorNode != null) {
            String exceptionMessage = errorNode.get(PN_MESSAGE) != null ? errorNode.get(PN_MESSAGE).asText() : errorNode.asText();
            return Solution.from(args).withMessage(HttpStatus.SC_BAD_REQUEST, exceptionMessage);
        }
        JsonNode choices = ObjectUtils.firstNonNull(
            jsonNode.get(PN_CHOICES),
            jsonNode.get(PN_DATA));
        if (choices == null) {
            return Solution.from(args).empty();
        }
        List<String> options = new ArrayList<>();
        Iterator<JsonNode> elements = choices.elements();
        while (elements.hasNext()) {
            JsonNode nextElement = elements.next();
            if (nextElement.hasNonNull(PN_URL)) {
                Optional.of(nextElement.get(PN_URL))
                    .map(JsonNode::asText)
                    .filter(StringUtils::isNotEmpty)
                    .ifPresent(options::add);
            } else if (nextElement.hasNonNull(CoreConstants.PN_TEXT)) {
                JsonNode text = nextElement.get(CoreConstants.PN_TEXT);
                boolean isCutOff = Optional.ofNullable(nextElement.get(PN_FINISH))
                    .map(JsonNode::asText)
                    .orElse(StringUtils.EMPTY)
                    .equals(VALUE_LENGTH);
                options.add(text.asText() + (isCutOff ? CoreConstants.ELLIPSIS : StringUtils.EMPTY));
            }
        }
        return Solution.from(args).withOptions(options);
    }
}
