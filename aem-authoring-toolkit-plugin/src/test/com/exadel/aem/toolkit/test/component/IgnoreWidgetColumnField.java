package com.exadel.aem.toolkit.test.component;

import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.api.annotations.widgets.ClassField;
import com.exadel.aem.toolkit.api.annotations.widgets.IgnoreFields;
import com.exadel.aem.toolkit.test.widget.SelectWidget;

@Dialog(
        name = "test-component",
        title = "test-component-dialog",
        layout = DialogLayout.FIXED_COLUMNS
)
@IgnoreFields(ignoreFields = {
        @ClassField(value = SelectWidget.class, field = "timezone"),
        @ClassField(value = SelectWidget.class, field = "rating")
})
@SuppressWarnings("unused")
public class IgnoreWidgetColumnField extends SelectWidget {

}