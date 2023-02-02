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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.ai.models.facility.Facility;
import com.exadel.aem.toolkit.core.ai.models.facility.Option;
import com.exadel.aem.toolkit.core.ai.models.facility.Setting;
import com.exadel.aem.toolkit.core.ai.models.facility.SimpleFacility;
import com.exadel.aem.toolkit.core.ai.models.solution.Solution;

@Component(service = AiService.class, immediate = true)
@Designate(ocd = WritesonicService.Config.class)
public class WritesonicService implements AiService {
    private static final Logger LOG = LoggerFactory.getLogger(WritesonicService.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private List<Facility> facilities;

    private boolean enabled;
    private String defaultEndpoint;
    private String defaultEngine;
    private String defaultLanguage;
    private String defaultTone;
    private String token;
    private int timeout;

    @Activate
    private void activate(Config config) {
        enabled = config.enabled();
        defaultEndpoint = config.endpoint();
        defaultEngine = config.engine();
        defaultLanguage = config.language();
        defaultTone = config.tone();
        token = config.token();
        timeout = config.timeout();
        facilities = Arrays.asList(
            new Expand(),
            new Shorten(),
            new Rephrase());
    }

    @Override
    public boolean isEnabled() {
        return enabled && StringUtils.isNotBlank(token);
    }

    @Override
    public List<Facility> getFacilities() {
        return facilities;
    }

    private Solution execute(Map<String, Object> requestArguments, String command, String payloadKey) {
        String effectiveText = Optional.ofNullable(requestArguments)
            .map(args -> args.getOrDefault(CoreConstants.PN_TEXT, StringUtils.EMPTY))
            .orElse(StringUtils.EMPTY).toString();
        if (StringUtils.isBlank(effectiveText)) {
            return Solution.fromMessage("Content is missing or invalid");
        }

        String effectiveEngine = getEffectiveProperty(requestArguments, "engine", defaultEngine).toString();
        String effectiveLanguage = getEffectiveProperty(requestArguments, "language", defaultLanguage).toString();
        String effectiveEndpoint = getEffectiveProperty(requestArguments, "endpoint", defaultEndpoint)
            .toString()
            .replace("{command}", command)
            .replace("{engine}", effectiveEngine)
            .replace("{language}", effectiveLanguage);

        HttpPost request = new HttpPost(effectiveEndpoint);
        request.setHeader("X-API-KEY", token);
        request.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        request.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        request.setEntity(new StringEntity(
            getRequestPayload(requestArguments, payloadKey, effectiveText),
            StandardCharsets.UTF_8));

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
            return getSolution(response);
        } catch (IOException e) {
            LOG.error("Writesonic service request failed", e);
            return Solution.fromMessage(e.getMessage());
        }
    }

    private Object getEffectiveProperty(Map<String, Object> requestArguments, String argumentName, Object defaultValue) {
        if (requestArguments == null || requestArguments.get(argumentName) == null) {
            return defaultValue;
        }
        return requestArguments.get(argumentName);
    }

    private String getRequestPayload(Map<String, Object> requestArguments, String payloadKey, String text) {
        String effectiveTone = getEffectiveProperty(requestArguments, "tone", defaultTone).toString();
        Map<String, String> payload = new HashMap<>();
        payload.put(payloadKey, text);
        payload.put("tone_of_voice", effectiveTone);
        try {
            return OBJECT_MAPPER.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            LOG.error("Could not serialize Writesonic request payload", e);
            return "{}";
        }
    }

    private Solution getSolution(CloseableHttpResponse response) throws IOException {
        int statusCode = response.getStatusLine().getStatusCode();
        String responsePayload = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
        if (statusCode == HttpStatus.SC_OK) {
            return Solution.fromJson(responsePayload);
        }
        String errorMessage = parseResponsePayload(responsePayload);
        return StringUtils.isNotBlank(errorMessage)
            ? Solution.fromMessage(errorMessage)
            : Solution.fromMessage("Failure: " + response.getStatusLine());
    }

    private String parseResponsePayload(String value) throws IOException {
        JsonNode root = OBJECT_MAPPER.readTree(value);
        JsonNode detail = root.get("detail");
        if (detail == null) {
            return root.asText();
        }
        JsonNode detailEntry = detail.isArray() ? detail.get(0) : detail;
        JsonNode message = detailEntry.get("msg");
        return message != null ? message.asText() : detailEntry.asText();
    }

    /* ----------
       Facilities
       ---------- */

    private static abstract class WritesonicFacility extends SimpleFacility {

        private static final List<Setting> SETTINGS = Arrays.asList(
            new Setting(
                "engine",
                "Engine",
                Arrays.asList(
                    new Option("economy", true),
                    new Option("business")
                )),
            new Setting(
                "language",
                "Language",
                Arrays.asList(
                    new Option("en", "English", true),
                    new Option("en", "Dutch"),
                    new Option("fr", "French"),
                    new Option("de", "German"),
                    new Option("it", "Italian"),
                    new Option("pl", "Polish"),
                    new Option("es", "Spanish"),
                    new Option("pt-pt", "Portuguese"),
                    new Option("pt-br", "Portuguese (Brazil)"),
                    new Option("ru", "Russian"),
                    new Option("ja", "Japanese"),
                    new Option("zh", "Chinese"),
                    new Option("bg", "Bulgarian"),
                    new Option("zh", "Chinese"),
                    new Option("cs", "Chech"),
                    new Option("da", "Danish"),
                    new Option("el", "Greek"),
                    new Option("hu", "Hungarian"),
                    new Option("lt", "Lithuanian"),
                    new Option("lv", "Latvian"),
                    new Option("ro", "Romanian"),
                    new Option("sk", "Slovak"),
                    new Option("sv", "Slovenian"),
                    new Option("fi", "Finnish"),
                    new Option("et", "Estonian"))),
            new Setting(
                "tone",
                "Expression Tone",
                Arrays.asList(
                    new Option("excited"),
                    new Option("professional", true),
                    new Option("funny"),
                    new Option("encouraging"),
                    new Option("dramatic"),
                    new Option("witty"),
                    new Option("sarcastic"),
                    new Option("engaging"),
                    new Option("creative"))));

        @Override
        public String getVendor() {
            return "Writesonic";
        }

        @Override
        public String getIcon() {
            return "textEdit";
        }

        @Override
        public List<Setting> getSettings() {
            return SETTINGS;
        }
    }

    private class Expand extends WritesonicFacility {

        @Override
        public String getId() {
            return "expand.ws";
        }

        @Override
        public String getTitle() {
            return "Expand";
        }

        @Override
        public Solution execute(Map<String, Object> arguments) {
            return WritesonicService.this.execute(arguments, "sentence-expand", "content_to_expand");
        }
    }

    private class Shorten extends WritesonicFacility {

        @Override
        public String getId() {
            return "shorten.ws";
        }

        @Override
        public String getTitle() {
            return "Shorten";
        }

        @Override
        public Solution execute(Map<String, Object> arguments) {
            return WritesonicService.this.execute(arguments, "content-shorten", "content_to_shorten");
        }
    }

    private class Rephrase extends WritesonicFacility {

        @Override
        public String getId() {
            return "rephrase.ws";
        }

        @Override
        public String getTitle() {
            return "Rephrase";
        }

        @Override
        public Solution execute(Map<String, Object> arguments) {
            return WritesonicService.this.execute(arguments, "content-rephrase", "content_to_rephrase");
        }
    }

    /* --------
       Settings
       -------- */

    @ObjectClassDefinition(name = "EToolbox Authoring Kit - AI Bridge: Writesonic Integration")
    public @interface Config {

        @AttributeDefinition(name = "Enabled")
        boolean enabled() default true;

        @AttributeDefinition(name = "Default Service Endpoint")
        String endpoint() default "https://api.writesonic.com/v1/business/content/{command}?engine={engine}&language={language}";

        @AttributeDefinition(name = "Authorization Token")
        String token() default StringUtils.EMPTY;

        @AttributeDefinition(name = "Default Engine")
        String engine() default "economy";

        @AttributeDefinition(name = "Default Language")
        String language() default "en";

        @AttributeDefinition(name = "Default Expression Tone")
        String tone() default "professional";

        @AttributeDefinition(name = "Connection Timeout (ms)")
        int timeout() default 10000;
    }
}
