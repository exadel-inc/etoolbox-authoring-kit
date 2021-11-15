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


public class TestReplaceMultipleTimes {

    @AemComponent(
        path = TestConstants.DEFAULT_COMPONENT_NAME,
        writeMode = WriteMode.CREATE,
        title = TestConstants.DEFAULT_COMPONENT_TITLE
    )
    @Dialog
    public static class FirstCase {

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

    @AemComponent(
        path = TestConstants.DEFAULT_COMPONENT_NAME,
        writeMode = WriteMode.CREATE,
        title = TestConstants.DEFAULT_COMPONENT_TITLE
    )
    @Dialog
    public static class SecondCase extends Parent {

        @DialogField(
            label = "Title Replace Second"
        )
        @TextField
        @Replace(@ClassMember(source = GrandParent.class, value = "title"))
        private int anotherReplacementTitle;
    }

    static class Parent extends GrandParent {
        @DialogField(
            label = "Title Replace First"
        )
        @TextField
        @Replace(@ClassMember(source = _Super.class, value = "title"))
        private int replacementTitle;
    }

    static class GrandParent {
        @DialogField(
            label = "Title"
        )
        @TextField
        private int title;
    }
}
