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

import org.apache.sling.api.resource.ValueMap;

import com.exadel.aem.toolkit.core.assistant.models.facilities.Setting;
import com.exadel.aem.toolkit.core.assistant.models.facilities.SettingPersistence;
import com.exadel.aem.toolkit.core.assistant.models.solutions.Solution;
import com.exadel.aem.toolkit.core.assistant.utils.VersionableValueMap;

class RephraseFacility extends OpenAiFacility {

    private static final String DEFAULT_INSTRUCTION = "Make this text sound friendly";
    private static final Setting INSTRUCTION_SETTING = Setting
            .builder()
            .id("instruction")
            .title("Tell how to rephrase content")
            .persistence(SettingPersistence.TRANSIENT)
            .defaultValue(DEFAULT_INSTRUCTION)
            .build();
    private static final List<Setting> REPHRASE_SETTINGS;

    static {
        REPHRASE_SETTINGS = new ArrayList<>();
        REPHRASE_SETTINGS.add(INSTRUCTION_SETTING);
        REPHRASE_SETTINGS.addAll(EDIT_SETTINGS);

    }

    public RephraseFacility(OpenAiService service) {
        super(service);
    }

    @Override
    public String getId() {
        return "text.rephrase.oai";
    }

    @Override
    public String getTitle() {
        return "Rephrase";
    }

    @Override
    public int getRanking() {
        return 100;
    }

    @Override
    public List<Setting> getSettings() {
        return REPHRASE_SETTINGS;
    }

    @Override
    public Solution execute(ValueMap args) {
        ValueMap newArgs = new VersionableValueMap(args)
            .putIfMissing(OpenAiConstants.PN_INSTRUCTION, DEFAULT_INSTRUCTION)
            .putIfMissing(OpenAiConstants.PN_MODEL, OpenAiServiceConfig.DEFAULT_EDIT_MODEL);
        return getService().executeEdit(newArgs);
    }
}
