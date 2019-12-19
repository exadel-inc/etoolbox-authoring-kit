package com.exadel.aem.toolkit.samples.models;

import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;
import com.exadel.aem.toolkit.api.annotations.widgets.color.ColorField;
import com.exadel.aem.toolkit.api.annotations.widgets.color.ColorValue;
import com.exadel.aem.toolkit.samples.annotations.FieldsetPostfix;
import com.exadel.aem.toolkit.samples.constants.PathConstants;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Dialog(
        name = "content/armor-color-component",
        title = "Armor Color Component",
        description = "Choose colors of warrior's armor",
        resourceSuperType = PathConstants.FOUNDATION_PARBASE_PATH,
        componentGroup = "Toolkit Samples",
        tabs= {
                @Tab(title = ArmorColorComponent.TAB_COLOR),
        }
)
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class ArmorColorComponent {

        public static final String TAB_COLOR = "Armor color";
        public static final String FIELDS_PREFIX = "color";
        public static final String FIELDS_POSTFIX = "-test";

        @FieldsetPostfix(postfix = ArmorColorComponent.FIELDS_POSTFIX)
        @FieldSet(
                title = "Color of warrior's armor",
                namePrefix = ArmorColorComponent.FIELDS_PREFIX
        )
        @Self
        private ArmorColorFields armorColor;

        @Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
        public static class ArmorColorFields {

                @ColorField(
                        value = ColorValue.HEX,
                        emptyText = "Choose color for armor"
                )
                @DialogField(
                        name = "./armor",
                        ranking = 2
                )
                @Default(values = {"#A9A9A9"})
                @ValueMapValue(name = ArmorColorComponent.FIELDS_PREFIX + "armor" + ArmorColorComponent.FIELDS_POSTFIX)
                private String armor;

                @ColorField(
                        value = ColorValue.HEX,
                        emptyText = "Choose color for shoes"
                )
                @DialogField(
                        name = "./shoes",
                        ranking = 3
                )
                @Default(values = {"#A9A9A9"})
                @ValueMapValue(name = ArmorColorComponent.FIELDS_PREFIX + "shoes" + ArmorColorComponent.FIELDS_POSTFIX)
                private String shoes;

                @ColorField(
                        value = ColorValue.HEX,
                        emptyText = "Choose color for helmet"
                )
                @DialogField(
                        name = "./helmet",
                        ranking = 1
                )
                @Default(values = {"#A9A9A9"})
                @ValueMapValue(name = ArmorColorComponent.FIELDS_PREFIX + "helmet" + ArmorColorComponent.FIELDS_POSTFIX)
                private String helmet;

                public String getArmor() { return armor; }

                public String getShoes() {
                        return shoes;
                }

                public String getHelmet() {
                        return helmet;
                }
        }

        public String getHelmetColor() { return armorColor.getHelmet(); }

        public String getArmorColor() { return armorColor.getArmor(); }

        public String getShoesColor() { return armorColor.getShoes(); }
}
