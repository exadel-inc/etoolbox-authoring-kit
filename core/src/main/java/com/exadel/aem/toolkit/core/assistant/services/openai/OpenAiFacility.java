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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.assistant.models.facilities.Setting;
import com.exadel.aem.toolkit.core.assistant.models.facilities.SettingType;
import com.exadel.aem.toolkit.core.assistant.models.facilities.SimpleFacility;
import com.exadel.aem.toolkit.core.assistant.models.solutions.Solution;

abstract class OpenAiFacility extends SimpleFacility {

    private static final Setting COMPLETION_MODEL_SETTING = Setting
        .builder()
        .id(OpenAiConstants.PN_MODEL)
        .title("Model")
        .option(OpenAiServiceConfig.DEFAULT_COMPLETION_MODEL, "DaVinci: Most capable, slower")
        .option("text-curie-001", "Curie: Capable, agile")
        .option("text-babbage-001", "Babbage: For straightforward tasks, very fast")
        .option("text-ada-001", "Ada: For simplistic tasks, fastest")
        .defaultValue(OpenAiServiceConfig.DEFAULT_COMPLETION_MODEL)
        .build();
    private static final Setting EDIT_MODEL_SETTING = Setting
        .builder()
        .id(OpenAiConstants.PN_MODEL)
        .title("Model")
        .option(OpenAiServiceConfig.DEFAULT_EDIT_MODEL, "DaVinci")
        .defaultValue(OpenAiServiceConfig.DEFAULT_EDIT_MODEL)
        .build();
    private static final Setting TEMPERATURE_SETTING = Setting
        .builder()
        .id(OpenAiConstants.PN_TEMPERATURE)
        .title("Temperature")
        .type(SettingType.DOUBLE)
        .minValue(0d)
        .maxValue(2d)
        .defaultValue(String.valueOf(OpenAiServiceConfig.DEFAULT_TEMPERATURE))
        .build();
    private static final Setting MAX_TOKENS_SETTING = Setting
        .builder()
        .id(OpenAiConstants.PN_MAX_TOKENS)
        .title("Max Text Length (tokens)")
        .type(SettingType.INTEGER)
        .minValue(0)
        .defaultValue(OpenAiServiceConfig.DEFAULT_TEXT_LENGTH)
        .build();
    static final List<Setting> COMPLETION_SETTINGS = Arrays.asList(COMPLETION_MODEL_SETTING, TEMPERATURE_SETTING, MAX_TOKENS_SETTING);
    static final List<Setting> EDIT_SETTINGS = Arrays.asList(EDIT_MODEL_SETTING, TEMPERATURE_SETTING);
    static final String EXCEPTION_TOKEN_MISSING = "Authentication token is missing";

    private final OpenAiService service;

    OpenAiFacility(OpenAiService service) {
        this.service = service;
    }

    @Override
    public String getIcon() {
        return ICON_TEXT_EDIT;
    }

    @Override
    public String getVendorName() {
        return service.getVendorName();
    }

    @Override
    public Solution execute(SlingHttpServletRequest request) {
        ValueMap args = getArguments(request);
        boolean isCacheable = service.getConfig().caching()
            && !args.get(OpenAiConstants.NO_CACHE, false);

        if (isCacheable) {
            args.put(ResourceResolver.class.getName(), request.getResourceResolver());
        }
        if (StringUtils.isBlank(service.getConfig().token())) {
            return Solution.from(EXCEPTION_TOKEN_MISSING);
        }
        if (StringUtils.isBlank(args.get(CoreConstants.PN_TEXT, String.class))) {
            return Solution.from(EXCEPTION_INVALID_REQUEST);
        }
        return execute(args);
    }

    Solution execute(ValueMap args) {
        return Solution.from(args).empty();
    }

    OpenAiService getService() {
        return service;
    }
}
