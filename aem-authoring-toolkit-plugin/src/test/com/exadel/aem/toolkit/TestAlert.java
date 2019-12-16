package com.exadel.aem.toolkit;

import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.api.annotations.widgets.alert.Alert;
import com.exadel.aem.toolkit.api.annotations.widgets.alert.AlertSize;
import com.exadel.aem.toolkit.api.annotations.widgets.common.StatusVariantConstants;

@Dialog(
        name = "test-component",
        title = "test-component-dialog",
        layout = DialogLayout.FIXED_COLUMNS
)
@SuppressWarnings("unused")
public class TestAlert {

    @Alert(
            text = "Alert content",
            title = "Alert title",
            size = AlertSize.LARGE,
            variant = StatusVariantConstants.SUCCESS
    )
    String alertField;
}
