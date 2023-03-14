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

class ExpandFacility extends OpenAiFacility {

    private static final String PROMPT = "Expand the following text";

    ExpandFacility(OpenAiService service) {
        super(service);
    }

    @Override
    public String getId() {
        return "text.expand.oai";
    }

    @Override
    public String getTitle() {
        return "Expand";
    }

    @Override
    public String getIcon() {
        return ICON_TEXT_ADD;
    }

    @Override
    public List<Setting> getSettings() {
        return COMPLETION_SETTINGS;
    }

    @Override
    public Solution execute(ValueMap args) {
        return getService().executeCompletion(args, PROMPT);
    }
}
