package com.exadel.aem.toolkit.test.widget;

import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.api.annotations.widgets.ClassField;
import com.exadel.aem.toolkit.api.annotations.widgets.Fields;

@Dialog(
        name = "test-component",
        title = "test-component-dialog",
        layout = DialogLayout.TABS,
        tabs = {
                @Tab(title = "First tab"),
                @Tab(title = "Second tab"),
        }
)
@Fields(ignoreFields = {
        @ClassField(value = Tabs.class, field = "field3")
})
@SuppressWarnings("unused")
public class IgnoreTabsWidgetField extends Tabs {
}