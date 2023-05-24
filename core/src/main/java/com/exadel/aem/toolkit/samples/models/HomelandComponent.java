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

package com.exadel.aem.toolkit.samples.models;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.Self;

import com.exadel.aem.toolkit.api.annotations.editconfig.DropTargetConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;
import com.exadel.aem.toolkit.api.annotations.layouts.Tab;
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.imageupload.ImageUpload;
import com.exadel.aem.toolkit.samples.constants.GroupConstants;
import com.exadel.aem.toolkit.samples.constants.PathConstants;

@AemComponent(
    path = "content/homeland-component",
    title = "Homeland Component",
    description = "Homeland of your warrior",
    resourceSuperType = PathConstants.FOUNDATION_PARBASE_PATH,
    componentGroup = GroupConstants.COMPONENT_GROUP
)
@Dialog
@EditConfig(
    dropTargets = {
        @DropTargetConfig(
            propertyName = "./homelandImage/fileReference",
            nodeName = "homelandImage",
            accept = "image/.*",
            groups = "media"
        )
    }
)
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class HomelandComponent {

    private static final String TAB_HOMELAND = "Homeland";

    @Self
    private HomelandTab homelandTab;

    @Tab(title = HomelandComponent.TAB_HOMELAND)
    @Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
    public static class HomelandTab {

        private static final String FIELD_HOMELAND_IMAGE = "./homelandImage/";
        private static final String DESCRIPTION_HOMELAND_IMAGE = "Choose an image of your warrior's homeland";

        @Self
        Resource currentResource;

        @DialogField(
            name = FIELD_HOMELAND_IMAGE,
            description = DESCRIPTION_HOMELAND_IMAGE
        )
        @ImageUpload
        @ChildResource
        private Resource homelandImage;

        public String getHomelandImage() {
            if (homelandImage != null) {
                return homelandImage.getValueMap().get("fileReference", String.class);
            }
            return StringUtils.EMPTY;
        }

        public String getWarriorName() {
            return Optional.ofNullable(currentResource.getParent())
                .map(Resource::getParent)
                .map(Resource::getValueMap)
                .map(valueMap -> valueMap.get("name", String.class))
                .orElse(WarriorComponent.DEFAULT_NAME);
        }
    }

    public String getWarriorName() {
        return homelandTab.getWarriorName();
    }

    public String getHomelandImage() {
        return homelandTab.getHomelandImage();
    }
}
