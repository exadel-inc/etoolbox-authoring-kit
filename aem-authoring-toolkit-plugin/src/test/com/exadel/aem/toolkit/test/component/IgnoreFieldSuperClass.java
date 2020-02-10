package com.exadel.aem.toolkit.test.component;

import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.api.annotations.widgets.ClassField;
import com.exadel.aem.toolkit.api.annotations.widgets.IgnoreFields;
import com.exadel.aem.toolkit.test.widget.Tabs;

@Dialog(
        name = "test-component",
        title = "test-component-dialog",
        layout = DialogLayout.TABS,
        tabs = {
                @Tab(title = "First tab"),
        }
)
@IgnoreFields(ignoreFields = {
        @ClassField(value = Tabs.class, field = "field2")
})
@SuppressWarnings("unused")
public class IgnoreFieldSuperClass extends IgnoreTabsWidgetField {

}
