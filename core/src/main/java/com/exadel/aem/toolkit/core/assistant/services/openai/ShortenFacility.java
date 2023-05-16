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

import java.util.List;

import org.apache.sling.api.resource.ValueMap;

import com.exadel.aem.toolkit.core.assistant.models.facilities.Setting;
import com.exadel.aem.toolkit.core.assistant.models.solutions.Solution;
import com.exadel.aem.toolkit.core.assistant.utils.VersionableValueMap;

class ShortenFacility extends OpenAiFacility {

    private static final String INSTRUCTION = "Make the text shorter and more focused";

    ShortenFacility(OpenAiService service) {
        super(service);
    }

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
    public int getRanking() {
        return 300;
    }

    @Override
    public List<Setting> getSettings() {
        return EDIT_SETTINGS;
    }

    @Override
    public Solution execute(ValueMap args) {
        ValueMap newArgs = new VersionableValueMap(args)
            .putIfMissing(OpenAiConstants.PN_INSTRUCTION, INSTRUCTION)
            .putIfMissing(OpenAiConstants.PN_MODEL, OpenAiServiceConfig.DEFAULT_EDIT_MODEL);
        return getService().executeEdit(newArgs);
    }
}
