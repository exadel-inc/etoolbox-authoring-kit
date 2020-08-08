package com.exadel.aem.toolkit.samples.models;

import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.HtmlTag;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;
import com.exadel.aem.toolkit.samples.annotations.FieldsetPostfix;
import com.exadel.aem.toolkit.samples.constants.GroupConstants;
import com.exadel.aem.toolkit.samples.constants.PathConstants;
import com.exadel.aem.toolkit.samples.models.fieldsets.ArmorColorFields;

@Dialog(
        name = "content/armor-color-component",
        title = "Armor Color Component",
        description = "Choose colors of warrior's armor",
        resourceSuperType = PathConstants.FOUNDATION_PARBASE_PATH,
        componentGroup = GroupConstants.COMPONENT_GROUP,
        tabs = {
                @Tab(title = ArmorColorComponent.TAB_COLOR)
        }
)
@HtmlTag(className = "component-wrap")
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class ArmorColorComponent {

    private static final String TITLE_ARMOR_COLOR = "Color of warrior's armor";

    static final String TAB_COLOR = "Armor color";
    public static final String FIELDS_PREFIX = "color";
    public static final String FIELDS_POSTFIX = "-test";

    @DialogField
    @FieldSet(
            title = TITLE_ARMOR_COLOR,
            namePrefix = ArmorColorComponent.FIELDS_PREFIX
    )
    @FieldsetPostfix(postfix = ArmorColorComponent.FIELDS_POSTFIX)
    @Self
    private ArmorColorFields armorColor;

    public String getHelmetColor() {
        return armorColor.getHelmet();
    }

    public String getArmorColor() {
        return armorColor.getArmor();
    }

    public String getShoesColor() {
        return armorColor.getShoes();
    }
}
