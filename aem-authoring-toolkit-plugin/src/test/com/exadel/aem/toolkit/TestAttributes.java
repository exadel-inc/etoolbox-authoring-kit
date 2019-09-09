package com.exadel.aem.toolkit;

import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Attribute;
import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Data;

@SuppressWarnings("unused")
@Dialog(
        name = "test-component",
        title = "test-component-dialog",
        layout = DialogLayout.FIXED_COLUMNS
)
public class TestAttributes {
    @DialogField
    @TextField
    @Attribute(
            id = "field1-id",
            clas = "field1-attribute-class",
            data = {
                    @Data(name = "field1-data1", value = "value-data1"),
                    @Data(name = "field1-data2", value = "value-data2")
            })
    String field1;
}
