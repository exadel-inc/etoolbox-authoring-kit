package com.exadel.aem.toolkit;

import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;

@SuppressWarnings("unused")
class SampleFieldSet {
    private static final String FIELD =  "Content";
    private static final String LABEL = "Content label";
    private static final String DESCRIPTION = "Content description";

    @DialogField(
            name = FIELD,
            label = LABEL,
            description = DESCRIPTION
    )
    @TextField
    private String content;

    @SuppressWarnings("unused")
    private SampleInnerClass innerClass;

    private static class SampleInnerClass {
        @DialogField(
                name = "Inner class",
                label = "Inner class label",
                description = "Inner class description"
        )
        @TextField
        private String innerClassField;
    }
}
