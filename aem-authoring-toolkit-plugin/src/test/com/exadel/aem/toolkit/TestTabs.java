package com.exadel.aem.toolkit;

import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.container.PlaceOnTab;
import com.exadel.aem.toolkit.api.annotations.container.Tab;

@Dialog(
        name = "test-component",
        title = "test-component-dialog",
        layout = DialogLayout.TABS,
        tabs = {
                @Tab(title = "First tab"),
                @Tab(title = "Second tab"),
                @Tab(title = "Third tab")
        }
)
@SuppressWarnings("unused")
public class TestTabs {
    @DialogField(label = "Field on the first tab")
    @TextField
    @PlaceOnTab("First tab")
    String field1;

    @DialogField(label = "Field on the second tab")
    @TextField
    @PlaceOnTab("Second tab")
    String field2;

    @DialogField(description = "Field on the third tab")
    @TextField
    @PlaceOnTab("Third tab")
    String field3;
}
