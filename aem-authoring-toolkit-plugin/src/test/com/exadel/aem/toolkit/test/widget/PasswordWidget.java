package com.exadel.aem.toolkit.test.widget;

import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.Password;

@Dialog(
        name = "test-component",
        title = "test-component-dialog",
        layout = DialogLayout.FIXED_COLUMNS
)
@SuppressWarnings("unused")
public class PasswordWidget {

    @DialogField
    @Password(
            emptyText = "test-password",
            autocomplete = "on",
            autofocus = true,
            retype = "test-password",
            validation = "foundation.jcr.name"
    )
    String password;
}
