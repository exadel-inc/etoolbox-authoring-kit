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

import org.apache.sling.api.resource.ValueMap;

import com.exadel.aem.toolkit.core.assistant.models.solutions.Solution;

class ShortenFacility extends WritesonicFacility {

    ShortenFacility(WritesonicService service) {
        super(service);
    }

    @Override
    public String getId() {
        return "text.shorten.ws";
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
    Solution execute(ValueMap args) {
        return getService().executeContentChange("content-shorten","content_to_shorten", args);
    }
}
