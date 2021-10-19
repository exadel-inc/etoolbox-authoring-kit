package com.exadel.aem.toolkit.test.component;

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.ClassMember;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.WriteMode;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.Replace;

@AemComponent(
    path = "content/a-component",
    writeMode = WriteMode.CREATE,
    title = "A component"
)
@Dialog
public class TestReplaceMultipleTimes {

    @DialogField(
        label = "Title"
    )
    @TextField
    private int title;

    @DialogField(
        label = "Title Replace First"
    )
    @TextField
    @Replace(@ClassMember(value = "title"))
    private int replacementTitle;

    @DialogField(
        label = "Title Replace Second"
    )
    @TextField
    @Replace(@ClassMember(value = "title"))
    private int anotherReplacementTitle;
}
