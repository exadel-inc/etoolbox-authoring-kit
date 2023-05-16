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

import org.apache.sling.api.resource.ValueMap;

import com.exadel.aem.toolkit.core.assistant.models.facilities.Setting;
import com.exadel.aem.toolkit.core.assistant.models.facilities.SettingPersistence;
import com.exadel.aem.toolkit.core.assistant.models.solutions.Solution;
import com.exadel.aem.toolkit.core.assistant.utils.VersionableValueMap;

class TranslateFacility extends WritesonicFacility {

    private static final List<Setting> CONTENT_V1_SETTINGS = Arrays.asList(
        V1_ENGINE,
        LANGUAGE_SETTING_BUILDER.persistence(SettingPersistence.TRANSIENT).defaultValue("fr").build(),
        TONE);

    private static final List<Setting> CONTENT_V2_SETTINGS = Arrays.asList(
        V2_ENGINE,
        LANGUAGE_SETTING_BUILDER.persistence(SettingPersistence.TRANSIENT).defaultValue("fr").build(),
        TONE);

    TranslateFacility(WritesonicService service) {
        super(service);
    }

    @Override
    public String getId() {
        return "text.translate.ws";
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
        return WritesonicService.getEndpointVersion(getService().getConfig().contentEndpoint()) == WritesonicConstants.DEFAULT_ENDPOINT_VERSION
            ? CONTENT_V1_SETTINGS
            : CONTENT_V2_SETTINGS;
    }

    @Override
    Solution execute(ValueMap args) {
        ValueMap newArgs = new VersionableValueMap(args)
            .put(WritesonicConstants.PN_OPTIONS_COUNT, 2)
            .put(WritesonicConstants.PN_ENGINE, "business");
        return getService().executeContentChange("content-rephrase","content_to_rephrase", newArgs);
    }
}
