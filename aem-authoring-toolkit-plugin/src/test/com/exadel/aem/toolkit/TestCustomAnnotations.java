package com.exadel.aem.toolkit;

import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;

@Dialog(
        name = "test-component",
        title = "test-component-dialog",
        layout = DialogLayout.FIXED_COLUMNS
)
@SuppressWarnings("unused")
public class TestCustomAnnotations {
    @DialogField
    @TextField
    @CustomAnnotation(customField = "Custom!")
    String testCustomAnnotation;

    @DialogField
    @TextField
    @CustomAnnotation
    String testCustomAnnotationDefault;

    @DialogField
    @TextField
    @CustomAnnotationAutomapping(customField = "Custom!")
    String testCustomAnnotationAuto;

    @DialogField
    @TextField
    @CustomAnnotationAutomapping
    String testCustomAnnotationAutoDefault;
}
