package com.exadel.aem.toolkit.samples.models;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.Self;

import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.api.annotations.editconfig.DropTargetConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.imageupload.ImageUpload;
import com.exadel.aem.toolkit.samples.constants.GroupConstants;
import com.exadel.aem.toolkit.samples.constants.PathConstants;

@Dialog(
        name = "content/homeland-component",
        title = "Homeland Component",
        description = "Homeland of your warrior",
        resourceSuperType = PathConstants.FOUNDATION_PARBASE_PATH,
        componentGroup = GroupConstants.COMPONENT_GROUP,
        layout = DialogLayout.TABS
)
@EditConfig(
        dropTargets = {
                @DropTargetConfig(
                        propertyName = "./homelandImage/fileReference",
                        nodeName = "homelandImage",
                        accept = {"image/.*"},
                        groups = {"media"}
                )
        }
)
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class HomelandComponent {

    static final String TAB_HOMELAND = "Homeland";

    @Self
    private HomelandTab homelandTab;

    @Tab(title = HomelandComponent.TAB_HOMELAND)
    @Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
    public static class HomelandTab {

        @Inject
        private ResourceResolver resourceResolver;

        @ImageUpload
        @DialogField(
                name = "./homelandImage/",
                description = "Choose an image of your warrior homeland"
        )
        @ChildResource
        private Resource homelandImage;

        public String getHomelandImage() {
            if (homelandImage != null) {
                return homelandImage.getValueMap().get("fileReference", String.class);
            }
            return StringUtils.EMPTY;
        }

        public String getWarriorName() {
            String warriorName = null;

            Resource resource = resourceResolver.getResource(PathConstants.WARRIOR_COMPONENT_PATH);

            if (resource != null) {
                ValueMap resourceValueMap = resource.getValueMap();
                warriorName = resource.getValueMap().get("warriorName", String.class);
            }

            return StringUtils.defaultIfBlank(warriorName, WarriorComponent.DEFAULT_NAME);
        }
    }

    public String getWarriorName() {
        return homelandTab.getWarriorName();
    }

    public String getHomelandImage() {
        return homelandTab.getHomelandImage();
    }
}
