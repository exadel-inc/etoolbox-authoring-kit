package com.exadel.aem.toolkit.samples.models.fieldsets;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.color.ColorField;
import com.exadel.aem.toolkit.api.annotations.widgets.color.ColorValue;
import com.exadel.aem.toolkit.samples.models.ArmorColorComponent;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class ArmorColorFields {

    private static final String DEFAULT_COLOR = "#A9A9A9";

    @ColorField(
            value = ColorValue.HEX,
            emptyText = "Choose color for armor"
    )
    @DialogField(ranking = 2)
    @Default(values = {DEFAULT_COLOR})
    @ValueMapValue(name = ArmorColorComponent.FIELDS_PREFIX + "armor" + ArmorColorComponent.FIELDS_POSTFIX)
    private String armor;

    @ColorField(
            value = ColorValue.HEX,
            emptyText = "Choose color for shoes"
    )
    @DialogField(ranking = 3)
    @Default(values = {DEFAULT_COLOR})
    @ValueMapValue(name = ArmorColorComponent.FIELDS_PREFIX + "shoes" + ArmorColorComponent.FIELDS_POSTFIX)
    private String shoes;

    @ColorField(
            value = ColorValue.HEX,
            emptyText = "Choose color for helmet"
    )
    @DialogField(ranking = 1)
    @Default(values = {DEFAULT_COLOR})
    @ValueMapValue(name = ArmorColorComponent.FIELDS_PREFIX + "helmet" + ArmorColorComponent.FIELDS_POSTFIX)
    private String helmet;

    public String getArmor() {
        return armor;
    }

    public String getShoes() {
        return shoes;
    }

    public String getHelmet() {
        return helmet;
    }
}

