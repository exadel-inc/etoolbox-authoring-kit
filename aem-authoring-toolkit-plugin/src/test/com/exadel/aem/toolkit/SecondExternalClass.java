package com.exadel.aem.toolkit;

import com.exadel.aem.toolkit.api.annotations.widgets.Checkbox;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.PathField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.property.Properties;
import com.exadel.aem.toolkit.api.annotations.widgets.property.Property;
import com.exadel.aem.toolkit.api.annotations.container.PlaceOnTab;

@SuppressWarnings("unused")
class SecondExternalClass {
    @DialogField(
            name = "Label's name",
            label = "Label",
            description = "description",
            required = true
    )
    @TextField
    @PlaceOnTab("Main tab")
    private String label;

    @DialogField(
            name = "FieldName",
            label = "Field label",
            description = "Field's description",
            required = true
    )
    @PathField(
            rootPath = "root/path"
    )
    @PlaceOnTab("Main tab")
    private String url;

    @DialogField(
            name = "CheckboxName",
            label = "Checkbox's label",
            description = "Checkbox's description"
    )
    @Checkbox
    @PlaceOnTab("Main tab")
    private boolean checkbox;

    @DialogField(
            name = "IconName",
            label = "Icon label",
            description = "Icon description",
            required = true
    )
    @PathField(rootPath = "icons/folder/path")
    @Properties(@Property(name = "attribute", value = "attribute_value"))
    private String icon;
}