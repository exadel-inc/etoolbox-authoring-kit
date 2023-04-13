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
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import org.apache.commons.httpclient.ConnectTimeoutException;
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
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.assistant.models.facilities.Facility;
import com.exadel.aem.toolkit.core.assistant.models.solutions.Solution;
import com.exadel.aem.toolkit.core.assistant.services.AssistantService;
import com.exadel.aem.toolkit.core.assistant.services.ImportService;
import com.exadel.aem.toolkit.core.utils.ExecutorFactory;
import com.exadel.aem.toolkit.core.utils.HttpClientFactory;
import com.exadel.aem.toolkit.core.utils.ObjectConversionUtil;

@Component(service = AssistantService.class, immediate = true, property = "service.ranking:Integer=100")
@Designate(ocd = OpenAiServiceConfig.class)
public class OpenAiService implements AssistantService {
    private static final Logger LOG = LoggerFactory.getLogger(OpenAiService.class);

    private static final String PN_CHOICES = "choices";
    private static final String PN_CONTENT = "content";
    private static final String PN_DATA = "data";
    private static final String PN_ERROR = "error";
    private static final String PN_FINISH = "finish_reason";
    private static final String PN_INPUT = "input";
    private static final String PN_MESSAGES = "messages";
    private static final String PN_ROLE = "role";
    private static final String PN_URL = "url";

    private static final String VALUE_LENGTH = "length";

    private static final String HTTP_HEADER_BEARER = "Bearer ";

    private static final String VENDOR_NAME = "OpenAI";
    private static final String LOGO_RESOURCE = "assistant/logo-openai";
    private static final String LOGO;

    private static final String EXCEPTION_REQUEST_FAILED = "OpenAI service request failed";
    private static final String EXCEPTION_COULD_NOT_COMPLETE_ASYNC = "Could not complete request";
    private static final String EXCEPTION_TIMEOUT = "Connection to {} timed out";
    private static final String ROLE_USER = "user";

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
    @Reference(target = "(component.name=com.exadel.aem.toolkit.core.assistant.services.ImportService)")
    private AssistantService importService;

    private OpenAiServiceConfig config;
    private ExecutorService threadPoolExecutor;
    private List<Facility> facilities;

    @Activate
    @Modified
    private void init(OpenAiServiceConfig config) {
        destroy();
        this.config = config;
        if (facilities == null) {
            facilities = Arrays.asList(
                new ExpandFacility(this),
                new ShortenFacility(this),
                new RephraseFacility(this),
                new CorrectFacility(this),
                new ProduceImageFacility(this),
                new PageFacility(this, (ImportService) importService));
        }
        this.threadPoolExecutor = ExecutorFactory.newCachedThreadPoolExecutor();
    }

    @Deactivate
    private void destroy() {
        if (threadPoolExecutor != null) {
            threadPoolExecutor.shutdownNow();
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

    List<Solution> executeCompletion(List<ValueMap> args) {
        return execute(config.completionsEndpoint(), args, this::getCompletionRequestPayload);
    }

    Solution executeCompletion(ValueMap args) {
        return execute(config.completionsEndpoint(), args, this::getCompletionRequestPayload);
    }

    Solution executeEdit(ValueMap args) {
        return execute(config.editsEndpoint(), args, this::getEditRequestPayload);
    }

    List<Solution> executeImageGeneration(List<ValueMap> args) {
        return execute(config.imagesEndpoint(), args, this::getImageGenerationPayload);
    }

    Solution executeImageGeneration(ValueMap args) {
        return execute(config.imagesEndpoint(), args, this::getImageGenerationPayload);
    }

    private List<Solution> execute(String endpoint, List<ValueMap> args, Function<ValueMap, String> payloadFactory) {
        if (args.size() == 1) {
            return Collections.singletonList(execute(endpoint, args.get(0), payloadFactory));
        }
        CompletableFuture<?>[] tasks = new CompletableFuture[args.size()];
        for (int i = 0; i < args.size(); i++) {
            tasks[i] = executeAsync(endpoint, args.get(i), payloadFactory);
        }
        CompletableFuture.allOf(tasks).join();
        List<Solution> result = new ArrayList<>();
        for (CompletableFuture<?> task : tasks) {
            try {
                Solution solution = (Solution) task.get(
                    (long) (config.timeout() * HttpClientFactory.DEFAULT_ATTEMPTS_COUNT * 1.1),
                    TimeUnit.MILLISECONDS);
                result.add(solution);
            } catch (InterruptedException e) {
                LOG.warn(EXCEPTION_COULD_NOT_COMPLETE_ASYNC, e);
                Thread.currentThread().interrupt();
            } catch (TimeoutException | ExecutionException e) {
                LOG.warn(EXCEPTION_COULD_NOT_COMPLETE_ASYNC, e);
            }
        }
        return result;
    }

    private Solution execute(String endpoint, ValueMap args, Function<ValueMap, String> payloadFactory) {
        String effectiveEndpoint = OpenAiServiceConfig.DEFAULT_CHAT_MODEL.equals(args.get(OpenAiConstants.PN_MODEL, String.class))
            ? config.chatEndpoint()
            : endpoint;

        String payload = payloadFactory.apply(args);
        HttpPost request = new HttpPost(effectiveEndpoint);
        request.setHeader(HttpHeaders.AUTHORIZATION, HTTP_HEADER_BEARER + config.token());
        request.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        request.setEntity(new StringEntity(payload, StandardCharsets.UTF_8));

        int lastExceptionStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        String lastExceptionMessage = null;
        for (int attempt = 0; attempt < HttpClientFactory.DEFAULT_ATTEMPTS_COUNT; attempt++) {
            try (
                CloseableHttpClient client = HttpClientFactory.newClient(config.timeout());
                CloseableHttpResponse response = client.execute(request)
            ) {
                Solution solution = parseOpenAiResponse(response, args);
                EntityUtils.consume(response.getEntity());
                return solution;
            } catch (ConnectTimeoutException | SocketTimeoutException e) {
                LOG.warn(EXCEPTION_TIMEOUT, effectiveEndpoint);
                lastExceptionStatus = HttpStatus.SC_REQUEST_TIMEOUT;
                lastExceptionMessage = e.getMessage();
            } catch (IOException e) {
                LOG.error(EXCEPTION_REQUEST_FAILED, e);
                lastExceptionStatus = HttpStatus.SC_BAD_GATEWAY;
                lastExceptionMessage = e.getMessage();
            }
        }
        return Solution.from(args).withMessage(lastExceptionStatus, StringUtils.defaultIfEmpty(lastExceptionMessage, EXCEPTION_REQUEST_FAILED));
    }

    private CompletableFuture<Solution> executeAsync(String endpoint, ValueMap args, Function<ValueMap, String> payloadFactory) {
        return CompletableFuture.supplyAsync(
            () -> execute(endpoint, args, payloadFactory),
            threadPoolExecutor);
    }

    private String getCompletionRequestPayload(ValueMap args) {
        boolean isChat = OpenAiServiceConfig.DEFAULT_CHAT_MODEL.equals(args.get(OpenAiConstants.PN_MODEL, String.class));
        return isChat
            ? getChatRequestPayload(args)
            : getInstructRequestPayload(args);
    }

    private String getChatRequestPayload(ValueMap args) {
        String prompt = args.get(CoreConstants.PN_PROMPT, String.class);
        if (!StringUtils.endsWith(prompt, CoreConstants.SEPARATOR_COLON)) {
            prompt += CoreConstants.SEPARATOR_COLON;
        }
        Map<String, Object> properties = new HashMap<>();
        properties.put(OpenAiConstants.PN_MODEL, args.get(OpenAiConstants.PN_MODEL, config.completionModel()));
        properties.put(PN_MESSAGES, getChatPrompt(prompt + StringUtils.SPACE + args.get(CoreConstants.PN_TEXT)));
        return ObjectConversionUtil.toJson(properties);
    }
    private String getInstructRequestPayload(ValueMap args) {
        String prompt = args.get(CoreConstants.PN_PROMPT, String.class);
        if (!StringUtils.endsWith(prompt, CoreConstants.SEPARATOR_COLON)) {
            prompt += CoreConstants.SEPARATOR_COLON;
        }
        Map<String, Object> properties = new HashMap<>();
        properties.put(CoreConstants.PN_PROMPT, prompt + StringUtils.SPACE + args.get(CoreConstants.PN_TEXT));
        properties.put(OpenAiConstants.PN_MODEL, args.get(OpenAiConstants.PN_MODEL, String.class));
        properties.put(OpenAiConstants.PN_MAX_TOKENS, args.get(OpenAiConstants.PN_MAX_TOKENS, config.textLength()));
        properties.put(OpenAiConstants.PN_TEMPERATURE, args.get(OpenAiConstants.PN_TEMPERATURE, config.temperature()));
        properties.put(OpenAiConstants.PN_CHOICES_COUNT, args.get(OpenAiConstants.PN_CHOICES_COUNT, config.choices()));
        return ObjectConversionUtil.toJson(properties);
    }

    private String getEditRequestPayload(ValueMap args) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(OpenAiConstants.PN_INSTRUCTION, args.get(OpenAiConstants.PN_INSTRUCTION, StringUtils.EMPTY));
        properties.put(OpenAiConstants.PN_MODEL, args.get(OpenAiConstants.PN_MODEL, config.editModel()));
        properties.put(PN_INPUT, args.get(CoreConstants.PN_TEXT));
        properties.put(OpenAiConstants.PN_TEMPERATURE, args.get(OpenAiConstants.PN_TEMPERATURE, config.temperature()));
        properties.put(OpenAiConstants.PN_CHOICES_COUNT, args.get(OpenAiConstants.PN_CHOICES_COUNT, config.choices()));
        return ObjectConversionUtil.toJson(properties);
    }

    private String getImageGenerationPayload(ValueMap args) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(CoreConstants.PN_PROMPT, args.get(CoreConstants.PN_TEXT));
        properties.put(CoreConstants.PN_SIZE, args.getOrDefault(CoreConstants.PN_SIZE, config.imageSize()));
        properties.put(OpenAiConstants.PN_CHOICES_COUNT, args.get(OpenAiConstants.PN_CHOICES_COUNT, config.choices()));
        return ObjectConversionUtil.toJson(properties);
    }

    private static Solution parseOpenAiResponse(CloseableHttpResponse response, Map<String, Object> args) throws IOException {
        JsonNode jsonNode = ObjectConversionUtil.toNodeTree(response.getEntity().getContent());
        JsonNode errorNode = jsonNode.get(PN_ERROR);
        if (errorNode != null) {
            String exceptionMessage = errorNode.get(CoreConstants.PN_MESSAGE) != null ? errorNode.get(CoreConstants.PN_MESSAGE).asText() : errorNode.asText();
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
            } else if (nextElement.has(CoreConstants.PN_MESSAGE)) {
                JsonNode messageNode = nextElement.get(CoreConstants.PN_MESSAGE);
                if (messageNode.has(PN_CONTENT)) {
                    options.add(messageNode.get(PN_CONTENT).asText());
                }
            }
        }
        return Solution.from(args).withOptions(options);
    }

    /* ---------------
       Utility methods
       --------------- */

    private static List<Map<String, String>> getChatPrompt(String prompt) {
        Map<String, String> content = new LinkedHashMap<>();
        content.put(PN_ROLE, ROLE_USER);
        content.put(PN_CONTENT, prompt);
        return Collections.singletonList(content);
    }
}
