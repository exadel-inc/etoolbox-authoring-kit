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
package com.exadel.aem.toolkit.core.assistant.services;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import com.exadel.aem.toolkit.core.assistant.AssistantConstants;
import com.exadel.aem.toolkit.core.assistant.models.facility.Facility;
import com.exadel.aem.toolkit.core.assistant.models.facility.Setting;
import com.exadel.aem.toolkit.core.assistant.models.facility.SettingType;
import com.exadel.aem.toolkit.core.assistant.models.facility.SimpleFacility;
import com.exadel.aem.toolkit.core.assistant.models.solution.Solution;
import com.exadel.aem.toolkit.core.utils.ObjectConversionUtil;

@Component(service = AiService.class, immediate = true, property = "service.ranking:Integer=100")
@Designate(ocd = OpenAiService.Config.class)
public class OpenAiService implements AiService {

    private static final Logger LOG = LoggerFactory.getLogger(OpenAiService.class);

    private static final String PN_CHOICES = "choices";
    private static final String PN_CHOICES_COUNT = "n";
    private static final String PN_DATA = "data";
    private static final String PN_ERROR = "error";
    private static final String PN_FINISH = "finish_reason";
    private static final String PN_INPUT = "input";
    private static final String PN_INSTRUCTION = "instruction";
    private static final String PN_MAX_TOKENS = "max_tokens";
    private static final String PN_MESSAGE = "message";
    private static final String PN_MODEL = "model";
    private static final String PN_PROMPT = "prompt";
    private static final String PN_SIZE = "size";
    private static final String PN_TEMPERATURE = "temperature";
    private static final String PN_URL = "url";

    private static final String VALUE_LENGTH = "length";

    private static final String DEFAULT_COMPLETION_MODEL = "text-davinci-003";
    private static final String DEFAULT_EDIT_MODEL = "text-davinci-edit-001";
    private static final double DEFAULT_TEMPERATURE = 0.8d;
    private static final int DEFAULT_TEXT_LENGTH = 120;
    private static final String DEFAULT_IMAGE_SIZE = "512x512";

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

    private List<Facility> facilities;
    private boolean enabled;
    private String completionsEndpoint;
    private String editsEndpoint;
    private String imagesEndpoint;
    private String defaultCompletionModel;
    private String defaultEditModel;
    private double defaultTemperature;
    private int defaultTextLength;
    private String defaultImageSize;
    private int choices;
    private String token;
    private int timeout;

    @Activate
    @Modified
    private void init(Config config) {
        if (facilities == null) {
            facilities = Arrays.asList(
                new Expand(),
                new Shorten(),
                new Rephrase(),
                new Correct(),
                new ProduceImage());
        }
        enabled = config.enabled();
        completionsEndpoint = config.completionsEndpoint();
        editsEndpoint = config.editsEndpoint();
        imagesEndpoint = config.imagesEndpoint();
        defaultCompletionModel = config.completionModel();
        defaultEditModel = config.editModel();
        defaultTemperature = config.temperature();
        defaultTextLength = config.textLength();
        defaultImageSize = config.imageSize();
        choices = config.choices();
        token = config.token();
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
        return enabled && StringUtils.isNotBlank(token);
    }

    @Override
    public List<Facility> getFacilities() {
        return facilities;
    }

    private Solution execute(String endpoint, String payload, Map<String, Object> args) {
        HttpPost request = new HttpPost(endpoint);
        request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        request.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
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
            LOG.error("OpenAI service request failed", e);
            return Solution.from(args).withMessage(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private String getCompletionRequestPayload(String prompt, ValueMap args) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(PN_PROMPT, prompt + args.get(CoreConstants.PN_TEXT));
        properties.put(PN_MODEL, args.get(PN_MODEL, defaultCompletionModel));
        properties.put(PN_MAX_TOKENS, args.get(PN_MAX_TOKENS, defaultTextLength));
        properties.put(PN_TEMPERATURE, args.get(PN_TEMPERATURE, defaultTemperature));
        properties.put(PN_CHOICES_COUNT, choices);
        return ObjectConversionUtil.toJson(properties);
    }

    private String getEditRequestPayload(String prompt, ValueMap args) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(PN_MODEL, args.get(PN_MODEL, defaultEditModel));
        properties.put(PN_INSTRUCTION, prompt);
        properties.put(PN_INPUT, args.get(CoreConstants.PN_TEXT));
        properties.put(PN_TEMPERATURE, args.get(PN_TEMPERATURE, defaultTemperature));
        return ObjectConversionUtil.toJson(properties);
    }

    private static Solution getSolution(Map<String, Object> args, CloseableHttpResponse response) throws IOException {
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

    /* ----------
       Facilities
       ---------- */

    private abstract static class OpenAiFacility extends SimpleFacility {

        private static final Setting COMPLETION_MODEL_SETTING = Setting
            .builder()
            .id(PN_MODEL)
            .title("Model")
            .option(DEFAULT_COMPLETION_MODEL, "DaVinci: Most capable, slower")
            .option("text-curie-001", "Curie: Capable, agile")
            .option("text-babbage-001", "Babbage: For straightforward tasks, very fast")
            .option("text-ada-001", "Ada: For simplistic tasks, fastest")
            .defaultValue(DEFAULT_COMPLETION_MODEL)
            .build();
        private static final Setting EDIT_MODEL_SETTING = Setting
            .builder()
            .id(PN_MODEL)
            .title("Model")
            .option(DEFAULT_EDIT_MODEL, "DaVinci")
            .defaultValue(DEFAULT_EDIT_MODEL)
            .build();
        private static final Setting TEMPERATURE_SETTING = Setting
            .builder()
            .id(PN_TEMPERATURE)
            .title("Temperature")
            .type(SettingType.DOUBLE)
            .minValue(0d)
            .maxValue(2d)
            .defaultValue(String.valueOf(DEFAULT_TEMPERATURE))
            .build();
        private static final Setting MAX_TOKENS_SETTING = Setting
            .builder()
            .id(PN_MAX_TOKENS)
            .title("Max Text Length (tokens)")
            .type(SettingType.INTEGER)
            .minValue(0)
            .defaultValue(DEFAULT_TEXT_LENGTH)
            .build();
        private static final Setting IMAGE_SIZE_SETTING = Setting
            .builder()
            .id(PN_SIZE)
            .title("Image Size")
            .type(SettingType.STRING)
            .option("256x256")
            .option(DEFAULT_IMAGE_SIZE)
            .option("1024x1024")
            .defaultValue(DEFAULT_IMAGE_SIZE)
            .build();
        static final List<Setting> COMPLETION_SETTINGS = Arrays.asList(COMPLETION_MODEL_SETTING, TEMPERATURE_SETTING, MAX_TOKENS_SETTING);
        static final List<Setting> EDIT_SETTINGS = Arrays.asList(EDIT_MODEL_SETTING, TEMPERATURE_SETTING);
        static final List<Setting> IMAGE_SETTINGS = Collections.singletonList(IMAGE_SIZE_SETTING);

        @Override
        public String getIcon() {
            return ICON_TEXT_EDIT;
        }

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

    private class Expand extends OpenAiFacility {

        @Override
        public String getId() {
            return "text.expand.oai";
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
        public List<Setting> getSettings() {
            return COMPLETION_SETTINGS;
        }

        @Override
        public Solution execute(ValueMap args) {
            return OpenAiService.this.execute(
                completionsEndpoint,
                getCompletionRequestPayload("Expand the following text: ", args),
                args);
        }
    }

    private class Shorten extends OpenAiFacility {

        @Override
        public String getId() {
            return "text.shorten.oai";
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
        public List<Setting> getSettings() {
            return EDIT_SETTINGS;
        }

        @Override
        public Solution execute(ValueMap args) {
            return OpenAiService.this.execute(
                editsEndpoint,
                getEditRequestPayload("Make the text shorter and more focused", args),
                args);
        }
    }

    private class Rephrase extends OpenAiFacility {

        @Override
        public String getId() {
            return "text.rephrase.oai";
        }

        @Override
        public String getTitle() {
            return "Rephrase";
        }

        @Override
        public List<Setting> getSettings() {
            return COMPLETION_SETTINGS;
        }

        @Override
        public Solution execute(ValueMap args) {
            return OpenAiService.this.execute(
                completionsEndpoint,
                getCompletionRequestPayload("Rephrase the following text: ", args),
                args);
        }
    }

    private class Correct extends OpenAiFacility {

        @Override
        public String getId() {
            return "text.correct.oai";
        }

        @Override
        public String getTitle() {
            return "Correct";
        }

        @Override
        public int getRanking() {
            return 1000;
        }

        @Override
        public List<Setting> getSettings() {
            return EDIT_SETTINGS;
        }

        @Override
        public Solution execute(ValueMap args) {
            return OpenAiService.this.execute(
                editsEndpoint,
                getEditRequestPayload("Correct spelling and grammar", args),
                args);
        }
    }

    private class ProduceImage extends OpenAiFacility {

        @Override
        public String getId() {
            return "image.produce.oai";
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
            return OpenAiService.this.execute(
                imagesEndpoint,
                getImageGenerationPayload(args),
                args);
        }

        private String getImageGenerationPayload(Map<String, Object> args) {
            Map<String, Object> properties = new HashMap<>();
            properties.put(PN_PROMPT, args.get(CoreConstants.PN_TEXT));
            properties.put(PN_SIZE, args.getOrDefault(PN_SIZE, defaultImageSize));
            properties.put(PN_CHOICES_COUNT, choices);
            return ObjectConversionUtil.toJson(properties);
        }
    }

    /* --------
       Settings
       -------- */

    @ObjectClassDefinition(name = "EToolbox Authoring Kit - Assistant: OpenAI Integration")
    public @interface Config {

        @AttributeDefinition(name = "Enabled")
        boolean enabled() default true;

        @AttributeDefinition(name = "Authorization Token")
        String token() default StringUtils.EMPTY;

        @AttributeDefinition(name = "Completions Endpoint")
        String completionsEndpoint() default "https://api.openai.com/v1/completions";

        @AttributeDefinition(name = "Edits Endpoint")
        String editsEndpoint() default "https://api.openai.com/v1/edits";

        @AttributeDefinition(name = "Images Endpoint")
        String imagesEndpoint() default "https://api.openai.com/v1/images/generations";

        @AttributeDefinition(name = "Default Completion Model")
        String completionModel() default DEFAULT_COMPLETION_MODEL;

        @AttributeDefinition(name = "Default Edit Model")
        String editModel() default DEFAULT_EDIT_MODEL;

        @AttributeDefinition(name = "Default Temperature")
        double temperature() default DEFAULT_TEMPERATURE;

        @AttributeDefinition(name = "Default Output Length (tokens)")
        int textLength() default DEFAULT_TEXT_LENGTH;

        @AttributeDefinition(name = "Default Output Image Size")
        String imageSize() default DEFAULT_IMAGE_SIZE;

        @AttributeDefinition(name = "Number of Choices")
        int choices() default 3;

        @AttributeDefinition(name = "Connection Timeout (ms)")
        int timeout() default AssistantConstants.HTTP_TIMEOUT;
    }
}
