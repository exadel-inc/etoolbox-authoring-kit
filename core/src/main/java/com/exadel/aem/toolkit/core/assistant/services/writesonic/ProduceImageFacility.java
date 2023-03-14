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

import java.util.Collections;
import java.util.List;

import org.apache.sling.api.resource.ValueMap;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.assistant.models.facilities.Setting;
import com.exadel.aem.toolkit.core.assistant.models.facilities.SettingPersistence;
import com.exadel.aem.toolkit.core.assistant.models.solutions.Solution;

class ProduceImageFacility extends WritesonicFacility {

    private static final List<Setting> IMAGE_SETTINGS = Collections.singletonList(
        Setting
            .builder()
            .id(CoreConstants.PN_SIZE)
            .title("Image size")
            .persistence(SettingPersistence.REQUIRED)
            .option(WritesonicServiceConfig.DEFAULT_IMAGE_SIZE, "Square (512x512)")
            .option("768x512", "Horizontal (768x512)")
            .option("512x768", "Vertical (512x768)")
            .defaultValue(WritesonicServiceConfig.DEFAULT_IMAGE_SIZE)
            .build());

    ProduceImageFacility(WritesonicService service) {
        super(service);
    }

    @Override
    public String getId() {
        return "image.produce.ws";
    }

    @Override
    public String getTitle() {
        return "Produce Image";
    }

    @Override
    public String getIcon() {
        return ICON_IMAGE_ADD;
    }

    @Override
    public int getRanking() {
        return 1001;
    }

    @Override
    public List<Setting> getSettings() {
        return IMAGE_SETTINGS;
    }

    @Override
    public Solution execute(ValueMap args) {
        return getService().executeImageGeneration(args);
    }

}
