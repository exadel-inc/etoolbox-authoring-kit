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

import java.util.ArrayList;
import java.util.List;

import com.exadel.aem.toolkit.core.CoreConstants;

import org.apache.sling.api.resource.ValueMap;

import com.exadel.aem.toolkit.core.assistant.models.facilities.Setting;
import com.exadel.aem.toolkit.core.assistant.models.facilities.SettingPersistence;
import com.exadel.aem.toolkit.core.assistant.models.solutions.Solution;
import com.exadel.aem.toolkit.core.assistant.utils.VersionableValueMap;

class TranslateFacility extends OpenAiFacility {

    private static final String PROPERTY_LANGUAGE = "language";
    private static final String DEFAULT_TRANSLATION = "French";

    private static final Setting LANGUAGE_SETTING = Setting
            .builder()
            .id(PROPERTY_LANGUAGE)
            .title("Language")
            .option("English")
            .option("Dutch")
            .option(DEFAULT_TRANSLATION)
            .option("German")
            .option("Italian")
            .option("Polish")
            .option("Spanish")
            .option("Portuguese")
            .option("Portuguese (Brazil)")
            .option("Russian")
            .option("Japanese")
            .option("Chinese")
            .option("Bulgarian")
            .option("Chech")
            .option("Danish")
            .option("Greek")
            .option("Hungarian")
            .option("Lithuanian")
            .option("Latvian")
            .option("Romanian")
            .option("Slovak")
            .option("Slovenian")
            .option("Finnish")
            .option("Estonian")
            .defaultValue(DEFAULT_TRANSLATION)
            .persistence(SettingPersistence.TRANSIENT)
            .build();
    private static final List<Setting> REPHRASE_SETTINGS;

    static {
        REPHRASE_SETTINGS = new ArrayList<>();
        REPHRASE_SETTINGS.add(LANGUAGE_SETTING);
        REPHRASE_SETTINGS.addAll(SETTINGS);

    }

    public TranslateFacility(OpenAiService service) {
        super(service);
    }

    @Override
    public String getId() {
        return "text.translate.oai";
    }

    @Override
    public String getTitle() {
        return "Translate";
    }

    @Override
    public String getIcon() {
        return ICON_TEXT_PASTE;
    }

    @Override
    public int getRanking() {
        return 1;
    }

    @Override
    public List<Setting> getSettings() {
        return REPHRASE_SETTINGS;
    }

    @Override
    public Solution execute(ValueMap args) {
        String prompt = "Translate this text into " + args.get(PROPERTY_LANGUAGE, DEFAULT_TRANSLATION);
        ValueMap newArgs = new VersionableValueMap(args)
            .put(CoreConstants.PN_PROMPT, prompt);
        return getService().generateText(newArgs);
    }
}
