package com.exadel.aem.toolkit;

import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.radio.RadioButton;
import com.exadel.aem.toolkit.api.annotations.widgets.radio.RadioGroup;

@Dialog(
        name = "test-component",
        title = "test-component-dialog",
        layout = DialogLayout.FIXED_COLUMNS
)
@SuppressWarnings("unused")
public class TestRadioGroup {
    @DialogField
    @RadioGroup(buttons = {
            @RadioButton(text = "Button 1", value = "1", checked=true),
            @RadioButton(text = "Button 2", value = "2"),
            @RadioButton(text = "Button 3", value = "3", disabled=true)
    })
    String field;
}
