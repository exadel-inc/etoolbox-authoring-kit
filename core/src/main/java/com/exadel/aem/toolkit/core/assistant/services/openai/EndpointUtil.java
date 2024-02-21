package com.exadel.aem.toolkit.core.assistant.services.openai;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ValueMap;

class EndpointUtil {

    private EndpointUtil() {
    }

    static boolean isChatEndpoint(ValueMap args) {
        String model = args.get(OpenAiConstants.PN_MODEL, String.class);
        return StringUtils.equalsAny(
            model,
            OpenAiServiceConfig.TEXT_MODEL_GPT_4,
            OpenAiServiceConfig.TEXT_MODEL_GPT_4_TURBO,
            OpenAiServiceConfig.TEXT_MODEL_GPT_3_5_TURBO);
    }
}
