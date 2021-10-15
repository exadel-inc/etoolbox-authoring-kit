package com.exadel.aem.toolkit.test.component;

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.ClassMember;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.WriteMode;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.Replace;
import com.exadel.aem.toolkit.api.markers._Super;
import com.exadel.aem.toolkit.plugin.utils.TestConstants;

@SuppressWarnings("unused")
public class ReplaceTestCase {

    public static class Parent {

        @DialogField(
            label = "Title"
        )
        @TextField
        private int title;

        @DialogField(
            label = "Another field"
        )
        @TextField
        private int anotherField;

    }

    @AemComponent(
        path = TestConstants.DEFAULT_COMPONENT_NAME,
        writeMode = WriteMode.CREATE,
        title = TestConstants.DEFAULT_COMPONENT_TITLE
    )
    @Dialog
    public static class Child extends Parent {

        @DialogField(
            label = "Title Child"
        )
        @TextField
        @Replace(@ClassMember(source = _Super.class, value = "title"))
        private int childTitle;
    }
}
