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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.sling.api.resource.ValueMap;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.assistant.models.facilities.Setting;
import com.exadel.aem.toolkit.core.assistant.models.facilities.SettingPersistence;
import com.exadel.aem.toolkit.core.assistant.models.solutions.Solution;
import com.exadel.aem.toolkit.core.assistant.utils.VersionableValueMap;

class TargetingFacility extends OpenAiFacility {

    private static final String PROMPT_PREFIX = "Rephrase the following text to make it sound ";
    private static final String DEFAULT_AUDIENCE = "Business people";
    private static final String DEFAULT_TARGET = "accurate, convincing and business-like";
    private static final Map<String, String> AUDIENCES;
    private static final Setting PROMPT_SETTING;
    private static final List<Setting> PROFILE_SETTINGS;
    static {

        AUDIENCES = new LinkedHashMap<>();
        AUDIENCES.put(DEFAULT_AUDIENCE, DEFAULT_TARGET);
        AUDIENCES.put("Young adults", "like a Zoomer's blog post addressed at young adults in their late teens and early twenties");
        AUDIENCES.put("Elderly people", "an article from a magazine addressed at elderly people");
        AUDIENCES.put("Customers from East Asia", " most suitable for customers from East Asia");
        AUDIENCES.put("Customers from South America", " most suitable for customers from South America");

        Setting.Builder settingBuilder = Setting
            .builder()
            .id("prompt")
            .title("Select the audience")
            .persistence(SettingPersistence.TRANSIENT);
        AUDIENCES.keySet().forEach(settingBuilder::option);
        settingBuilder.defaultValue(AUDIENCES.keySet().iterator().next());
        PROMPT_SETTING = settingBuilder.build();

        PROFILE_SETTINGS = new ArrayList<>();
        PROFILE_SETTINGS.add(PROMPT_SETTING);
        PROFILE_SETTINGS.addAll(COMPLETION_SETTINGS);
    }

    public TargetingFacility(OpenAiService service) {
        super(service);
    }

    @Override
    public String getId() {
        return "text.target.oai";
    }

    @Override
    public String getTitle() {
        return "Adjust to the audience";
    }

    @Override
    public String getIcon() {
        return "imageProfile";
    }

    @Override
    public int getRanking() {
        return 101;
    }

    @Override
    public List<Setting> getSettings() {
        return PROFILE_SETTINGS;
    }

    @Override
    public Solution execute(ValueMap args) {
        String prompt = args.get(CoreConstants.PN_PROMPT, DEFAULT_AUDIENCE);
        ValueMap newArgs = new VersionableValueMap(args)
            .put(CoreConstants.PN_PROMPT, PROMPT_PREFIX + AUDIENCES.getOrDefault(prompt, DEFAULT_TARGET) + CoreConstants.SEPARATOR_COLON)
            .putIfMissing(OpenAiConstants.PN_MODEL, OpenAiServiceConfig.DEFAULT_COMPLETION_MODEL);
        return getService().executeCompletion(newArgs);
    }
}
