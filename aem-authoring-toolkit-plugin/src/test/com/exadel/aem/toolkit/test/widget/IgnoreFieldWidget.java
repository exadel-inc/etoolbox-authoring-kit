package com.exadel.aem.toolkit.test.widget;

import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.IgnoreField;

@Dialog(
        name = "test-component",
        title = "test-component-dialog",
        layout = DialogLayout.FIXED_COLUMNS
)
@SuppressWarnings("unused")
public class IgnoreFieldWidget {

    @DialogField
    @IgnoreField
    String field;
}
