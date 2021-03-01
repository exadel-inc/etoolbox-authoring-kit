package com.exadel.aem.toolkit.test.widget;

import com.exadel.aem.toolkit.api.annotations.layouts.Place;
import com.exadel.aem.toolkit.api.annotations.main.ClassMember;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;

@Dialog(
    name = "test-component",
    title = "test-component-dialog",
    layout = DialogLayout.FIXED_COLUMNS
)
public class PlaceWidget {

    @TextField
    String field0;

    @Place(before = @ClassMember(name = "field0"))
    @TextField
    String field1;

    @Place(before = @ClassMember(name = "field6"))
    @TextField
    String field2;

    @Place(before = @ClassMember(name = "field2"))
    @TextField
    String field3;

    @Place(after = @ClassMember(name = "field2"))
    @TextField
    String field4;

    @TextField
    String field5;

    @Place(before = @ClassMember(name = "field0"))
    @TextField
    String field6;

    @Place(after = @ClassMember(name = "field6"))
    @TextField
    String field7;

    @TextField
    String field8;

    @Place(before = @ClassMember(name = "field5"))
    @TextField
    String field9;
}

interface Interface {

    default String getField1() {
        return null;
    }

    default String getField2() {
        return null;
    }

    default String getField7() {
        return null;
    }

    default String getField8() {
        return null;
    }
}
