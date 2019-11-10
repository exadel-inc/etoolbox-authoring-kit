package com.exadel.aem.toolkit;

import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditorType;
import com.exadel.aem.toolkit.api.annotations.editconfig.InplaceEditingConfig;

@Dialog(
        name = "test-component",
        title = "test-component-dialog",
        layout = DialogLayout.FIXED_COLUMNS)
@EditConfig(
        inplaceEditing = @InplaceEditingConfig(
                type = EditorType.TEXT,
                editElementQuery = ".editable-header",
                name = "header",
                propertyName = "header"
        )
)
@SuppressWarnings("unused")
public class TestEditConfig {
    @DialogField
    @TextField
    String field1;
}
