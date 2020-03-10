package com.exadel.aem.toolkit.test.widget;

import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.api.annotations.main.HtmlTag;

@Dialog(
        name = "test-component",
        title = "test-component-dialog",
        layout = DialogLayout.FIXED_COLUMNS
)
@HtmlTag(
        className = "wrapper",
        tagName = "span"
)
@SuppressWarnings("unused")
public class ColorFieldWidgetWithHtmlTag extends ColorFieldWidget{

}
