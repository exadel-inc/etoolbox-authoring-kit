package com.exadel.aem.toolkit.test.widget;

import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.color.ColorField;
import com.exadel.aem.toolkit.api.annotations.widgets.color.ColorValue;
import com.exadel.aem.toolkit.api.annotations.widgets.color.ColorVariant;
import com.exadel.aem.toolkit.api.annotations.widgets.color.GenerateColorsState;

@Dialog(
        name = "test-component",
        title = "test-component-dialog",
        layout = DialogLayout.FIXED_COLUMNS
)
@SuppressWarnings("unused")
public class ColorFieldWidget {

    @DialogField
    @ColorField(
            value = ColorValue.HEX,
            emptyText = "test-string",
            variant = ColorVariant.SWATCH,
            autogenerateColors = GenerateColorsState.SHADES,
            showSwatches = false,
            showDefaultColors = false,
            validation = "foundation.jcr.name"
    )
    String color;
}
