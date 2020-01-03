package com.exadel.aem.toolkit.samples.models;

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
import com.exadel.aem.toolkit.samples.constants.PathConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Dialog(
        name = "content/warrior-component",
        title = "Warrior component",
        description = "Make your own warrior",
        resourceSuperType = PathConstants.FOUNDATION_PARBASE_PATH,
        componentGroup = "Toolkit Samples",
        tabs = {
                @Tab(title = WarriorComponent.TAB_MAIN),
                @Tab(title = WarriorComponent.TAB_ICON),
                @Tab(title = WarriorComponent.TAB_THEME)
        }
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
                        propertyName = "warriorName",
                        type = EditorType.PLAINTEXT,
                        editElementQuery = ".warrior-name-span"
                )
        }
)
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class WarriorComponent {

    static final String TAB_MAIN = "Main info";
    static final String TAB_ICON = "Icon";
    static final String TAB_THEME = "Color theme";
    static final String DEFAULT_NAME = "The Guy";

    private static final String DEFAULT_TITLE = "Make your warrior";
    private static final String DARK_THEME_CLASS = "dark-theme";
    private static final String LIGHT_THEME_CLASS = "light-theme";

    @Attribute(clas = "test")
    @TextField(emptyText = DEFAULT_TITLE)
    @DialogField(
            label = "Container title",
            name = "./title"
    )
    @ValueMapValue
    private String title;

    @TextField(emptyText = WarriorComponent.DEFAULT_NAME)
    @DialogField(
            label = "Warrior name",
            name = "./warriorName"
    )
    @ValueMapValue
    private String warriorName;

    @PlaceOnTab(WarriorComponent.TAB_ICON)
    @PathField(
            emptyText = "Face",
            rootPath = "/content/dam"
    )
    @DialogField(
            label = "Photo of warrior",
            name = "./iconPath"
    )
    @ValueMapValue
    private String iconPath;

    @PlaceOnTab(WarriorComponent.TAB_THEME)
    @Switch()
    @DialogField(
            label = "Dark theme",
            name = "./colorTheme"
    )
    @Default(booleanValues = {false})
    @ValueMapValue
    private boolean colorTheme;

    public String getTitle() { return StringUtils.defaultIfBlank(title, DEFAULT_TITLE); }

    public String getWarriorName() { return StringUtils.defaultIfBlank(warriorName, DEFAULT_NAME); }

    public String getIconPath() {
        return iconPath;
    }

    public String getColorTheme() { return colorTheme ? DARK_THEME_CLASS : LIGHT_THEME_CLASS; }
}
