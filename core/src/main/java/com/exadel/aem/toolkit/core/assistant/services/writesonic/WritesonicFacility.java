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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ValueMap;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.assistant.models.facilities.Setting;
import com.exadel.aem.toolkit.core.assistant.models.facilities.SimpleFacility;
import com.exadel.aem.toolkit.core.assistant.models.solutions.Solution;

abstract class WritesonicFacility extends SimpleFacility {

    static final Setting V1_ENGINE = Setting
        .builder()
        .id(WritesonicConstants.PN_ENGINE)
        .title("Engine")
        .option(WritesonicServiceConfig.DEFAULT_ENGINE)
        .option("business")
        .defaultValue(WritesonicServiceConfig.DEFAULT_ENGINE)
        .build();

    static final Setting V2_ENGINE = Setting
        .builder()
        .id(WritesonicConstants.PN_ENGINE)
        .title("Engine")
        .option(WritesonicServiceConfig.DEFAULT_ENGINE)
        .option("average")
        .option("good")
        .option("premium")
        .defaultValue(WritesonicServiceConfig.DEFAULT_ENGINE)
        .build();

    static final Setting.Builder LANGUAGE_SETTING_BUILDER = Setting
        .builder()
        .id(WritesonicConstants.PN_LANGUAGE)
        .title("Language")
        .option(WritesonicServiceConfig.DEFAULT_LANGUAGE, "English")
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
        .defaultValue(WritesonicServiceConfig.DEFAULT_LANGUAGE);

    static final Setting TONE = Setting
        .builder()
        .id(WritesonicConstants.PN_TONE)
        .title("Tone of Voice")
        .option("excited")
        .option(WritesonicServiceConfig.DEFAULT_TONE)
        .option("funny")
        .option("encouraging")
        .option("dramatic")
        .option("witty")
        .option("sarcastic")
        .option("engaging")
        .option("creative")
        .defaultValue(WritesonicServiceConfig.DEFAULT_TONE)
        .build();

    private static final List<Setting> CONTENT_V1_SETTINGS = Arrays.asList(
        V1_ENGINE,
        LANGUAGE_SETTING_BUILDER.build(),
        TONE);

    private static final List<Setting> CONTENT_V2_SETTINGS = Arrays.asList(
        V2_ENGINE,
        LANGUAGE_SETTING_BUILDER.build(),
        TONE);

    private static final String EXCEPTION_API_KEY_MISSING = "API key is missing";

    private final WritesonicService service;

    WritesonicFacility(WritesonicService service) {
        this.service = service;
    }

    @Override
    public String getVendorName() {
        return getService().getVendorName();
    }

    @Override
    public List<Setting> getSettings() {
        return WritesonicService.getEndpointVersion(service.getConfig().contentEndpoint()) == WritesonicConstants.DEFAULT_ENDPOINT_VERSION
            ? CONTENT_V1_SETTINGS
            : CONTENT_V2_SETTINGS;
    }

    @Override
    public Solution execute(SlingHttpServletRequest request) {
        if (!isApiKeyPresent()) {
            return Solution.from(EXCEPTION_API_KEY_MISSING);
        }
        ValueMap args = getArguments(request);
        if (StringUtils.isBlank(args.get(CoreConstants.PN_TEXT, String.class))) {
            return Solution.from(EXCEPTION_INVALID_REQUEST);
        }
        return execute(args);
    }

    abstract Solution execute(ValueMap args);

    WritesonicService getService() {
        return service;
    }

    boolean isApiKeyPresent() {
        int version = WritesonicService.getEndpointVersion(service.getConfig().contentEndpoint());
        if (version == WritesonicConstants.DEFAULT_ENDPOINT_VERSION) {
            return StringUtils.isNotBlank(getService().getConfig().api1Key());
        }
        return StringUtils.isNotBlank(getService().getConfig().api2Key());
    }

}
