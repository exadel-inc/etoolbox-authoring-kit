package com.exadel.aem.toolkit.samples.models;

import com.exadel.aem.toolkit.api.annotations.editconfig.ChildEditConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.exadel.aem.toolkit.api.annotations.container.PlaceOnTab;
import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditorType;
import com.exadel.aem.toolkit.api.annotations.editconfig.InplaceEditingConfig;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.PathField;
import com.exadel.aem.toolkit.api.annotations.widgets.Switch;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Attribute;
import com.exadel.aem.toolkit.samples.constants.GroupConstants;
import com.exadel.aem.toolkit.samples.constants.PathConstants;

@Dialog(
        name = "content/warrior-component",
        title = "Warrior Component",
        description = "Make your own warrior",
        resourceSuperType = PathConstants.FOUNDATION_PARBASE_PATH,
        componentGroup = GroupConstants.COMPONENT_CONTAINER_GROUP,
        tabs = {
                @Tab(title = WarriorComponent.TAB_MAIN),
                @Tab(title = WarriorComponent.TAB_PHOTO),
                @Tab(title = WarriorComponent.TAB_THEME)
        },
        isContainer = true
)
@EditConfig(
        inplaceEditing = {
                @InplaceEditingConfig(
                        title = "Component title",
                        propertyName = "title",
                        type = EditorType.PLAINTEXT,
                        editElementQuery = ".warrior-component-title"
                ),
                @InplaceEditingConfig(
                        title = "Warrior's name",
                        propertyName = "name",
                        type = EditorType.PLAINTEXT,
                        editElementQuery = ".warrior-name-span"
                )
        }
)
@ChildEditConfig(
        actions = "copymove"
)
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class WarriorComponent {

    static final String TAB_MAIN = "Main info";
    static final String TAB_PHOTO = "Photo";
    static final String TAB_THEME = "Color theme";
    static final String DEFAULT_NAME = "The Guy";

    private static final String DEFAULT_TITLE = "Make your warrior";
    private static final String DARK_THEME_CLASS = "dark-theme";
    private static final String LIGHT_THEME_CLASS = "light-theme";

    @DialogField(label = "Container title")
    @TextField(emptyText = DEFAULT_TITLE)
    @Attribute(className = "test")
    @ValueMapValue
    private String title;

    @DialogField(label = "Warrior name")
    @TextField(emptyText = WarriorComponent.DEFAULT_NAME)
    @ValueMapValue
    private String name;

    @DialogField(label = "Photo of warrior")
    @PathField(
            emptyText = "Face",
            rootPath = "/content/dam"
    )
    @PlaceOnTab(WarriorComponent.TAB_PHOTO)
    @ValueMapValue
    private String photo;

    @DialogField(label = "Dark theme")
    @Switch
    @PlaceOnTab(WarriorComponent.TAB_THEME)
    @Default(booleanValues = {false})
    @ValueMapValue
    private boolean colorTheme;

    public String getTitle() {
        return StringUtils.defaultIfBlank(title, DEFAULT_TITLE);
    }

    public String getName() {
        return StringUtils.defaultIfBlank(name, DEFAULT_NAME);
    }

    public String getPhoto() {
        return photo;
    }

    public String getColorTheme() {
        return colorTheme ? DARK_THEME_CLASS : LIGHT_THEME_CLASS;
    }
}
