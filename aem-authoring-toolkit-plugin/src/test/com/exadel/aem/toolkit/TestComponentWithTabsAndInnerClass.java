package com.exadel.aem.toolkit;

import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.api.annotations.widgets.Checkbox;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;
import com.exadel.aem.toolkit.api.annotations.widgets.PathField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Attribute;
import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Data;
import com.exadel.aem.toolkit.api.annotations.widgets.radio.RadioButton;
import com.exadel.aem.toolkit.api.annotations.widgets.radio.RadioGroup;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Option;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Select;
import com.exadel.aem.toolkit.api.annotations.container.PlaceOnTab;
import com.exadel.aem.toolkit.api.annotations.container.Tab;

@Dialog(name = "helloworld",
        title = "Hello world 1 properties",
        layout = DialogLayout.TABS,
        tabs = {
                @Tab(title = "First tab"),
                @Tab(title = "Second tab"),
                @Tab(title = "Third tab")
        }
)
@SuppressWarnings("unused")
public class TestComponentWithTabsAndInnerClass {

    @DialogField(label = "Field 1", description = "This is the first field.",wrapperClass = "my-class",
            renderHidden = true)
    @TextField
    @Attribute(id = "field1-id",
            clas = "field1-attribute-class",
            data = {
                    @Data(name = "field1-data1", value = "value-data1"),
                    @Data(name = "field1-data2", value = "value-data2")
            })
    @PlaceOnTab("First tab")
    String field1;

    @DialogField(label="Field 2")
    @PathField(rootPath = "/content")
    @PlaceOnTab("Second tab")
    String field2;

    @DialogField(label="Field 2.1", wrapperClass = "my-wrapper-class")
    @TextField
    @PlaceOnTab("Third tab")
    String field3;

    @DialogField(description = "This is the second second field")
    @Checkbox(text = "Checkbox 2")
    @PlaceOnTab("First tab")
    String field4;

    @FieldSet(title = "Field set example")
    @PlaceOnTab("Second tab")
    FieldSetExample fieldSet;
    static class FieldSetExample{
        @DialogField
        @TextField
        String field6;

        @DialogField
        @TextField
        String field7;

        @DialogField
        @RadioGroup(buttons = {
                @RadioButton(text = "Button 1", value = "1"),
                @RadioButton(text = "Button 2", value = "2"),
                @RadioButton(text = "Button 3", value = "3")
        })
        String field8;
    }

    @DialogField(label = "Rating")
    @Select(options = {
            @Option(text = "1 star", value = "1"),
            @Option(text = "2 star", value = "2"),
            @Option(text = "3 star", value = "3"),
            @Option(text = "4 star", value = "4"),
            @Option(text = "5 star", value = "5")
    }, emptyText = "Select rating" )
    @PlaceOnTab("Third tab")
    String dropdown;

    @DialogField(label="hidden field", renderHidden = true)
    @TextField
    @PlaceOnTab("Forth tab")
    String field5;
}
