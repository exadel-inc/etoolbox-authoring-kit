package com.exadel.aem.toolkit.core.assistant.services.openai;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.utils.ObjectConversionUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ValueMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class PayloadHelper {

    private static final String PN_MESSAGES = "messages";
    private static final String PN_ROLE = "role";
    private static final String ROLE_SYSTEM = "system";
    private static final String ROLE_USER = "user";

    private final OpenAiServiceConfig config;

    PayloadHelper(OpenAiServiceConfig config) {
        this.config = config;
    }

    /* ------------
       Text payload
       ------------ */

    String getTextGenerationPayload(ValueMap args) {
        return EndpointUtil.isChatEndpoint(args)
            ? getChatTextGenerationPayload(args)
            : getInstructTextGenerationPayload(args);
    }

    String getChatTextGenerationPayload(ValueMap args) {
        String prompt = formatPrompt(args);
        Map<String, Object> properties = new HashMap<>();
        properties.put(OpenAiConstants.PN_MODEL, args.get(OpenAiConstants.PN_MODEL, String.class));
        properties.put(PN_MESSAGES, getChatProlog(prompt + args.get(CoreConstants.PN_TEXT)));
        properties.put(OpenAiConstants.PN_MAX_TOKENS, args.get(OpenAiConstants.PN_MAX_TOKENS, config.textLength()));
        properties.put(OpenAiConstants.PN_CHOICES_COUNT, args.get(OpenAiConstants.PN_CHOICES_COUNT, config.choices()));
        return ObjectConversionUtil.toJson(properties);
    }

    private String getInstructTextGenerationPayload(ValueMap args) {
        String prompt = formatPrompt(args);
        Map<String, Object> properties = new HashMap<>();
        properties.put(CoreConstants.PN_PROMPT, prompt + args.get(CoreConstants.PN_TEXT));
        properties.put(OpenAiConstants.PN_MODEL, args.get(OpenAiConstants.PN_MODEL, String.class));
        properties.put(OpenAiConstants.PN_MAX_TOKENS, args.get(OpenAiConstants.PN_MAX_TOKENS, config.textLength()));
        properties.put(OpenAiConstants.PN_TEMPERATURE, args.get(OpenAiConstants.PN_TEMPERATURE, config.temperature()));
        properties.put(OpenAiConstants.PN_CHOICES_COUNT, args.get(OpenAiConstants.PN_CHOICES_COUNT, config.choices()));
        return ObjectConversionUtil.toJson(properties);
    }

    /* -------------
       Image payload
       ------------- */

    String getImageGenerationPayload(ValueMap args) {
        Map<String, Object> properties = new HashMap<>();
        String effectivePrompt = args.get(CoreConstants.PN_PROMPT, args.get(CoreConstants.PN_TEXT, StringUtils.EMPTY));
        properties.put(CoreConstants.PN_PROMPT, effectivePrompt);
        properties.put(CoreConstants.PN_SIZE, args.getOrDefault(CoreConstants.PN_SIZE, config.imageSize()));
        properties.put(OpenAiConstants.PN_CHOICES_COUNT, args.get(OpenAiConstants.PN_CHOICES_COUNT, config.choices()));
        return ObjectConversionUtil.toJson(properties);
    }

    /* ---------------
       Utility methods
       --------------- */

    private static List<Map<String, String>> getChatProlog(String prompt) {
        List<Map<String, String>> result = new ArrayList<>();
        Map<String, String> message = new LinkedHashMap<>();
        message.put(PN_ROLE, ROLE_SYSTEM);
        message.put(OpenAiConstants.PN_CONTENT, "You are a competent and eloquent journalist who writes for the web");
        result.add(message);
        message = new LinkedHashMap<>();
        message.put(PN_ROLE, ROLE_USER);
        message.put(OpenAiConstants.PN_CONTENT, prompt);
        result.add(message);
        return result;
    }

    private static String formatPrompt(ValueMap args) {
        String prompt = args.get(CoreConstants.PN_PROMPT, StringUtils.EMPTY).trim();
        if (prompt.isEmpty()) {
            return prompt;
        }
        if (!StringUtils.endsWith(prompt, CoreConstants.SEPARATOR_COLON)) {
            prompt += CoreConstants.SEPARATOR_COLON;
        }
        return prompt + StringUtils.LF;
    }
}
