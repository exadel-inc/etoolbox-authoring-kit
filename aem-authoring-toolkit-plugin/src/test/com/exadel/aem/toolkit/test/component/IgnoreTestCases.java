/*
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.exadel.aem.toolkit.test.component;

import java.util.List;

import com.exadel.aem.toolkit.api.annotations.container.IgnoreTabs;
import com.exadel.aem.toolkit.api.annotations.container.PlaceOnTab;
import com.exadel.aem.toolkit.api.annotations.main.ClassField;
import com.exadel.aem.toolkit.api.annotations.main.ClassMember;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;
import com.exadel.aem.toolkit.api.annotations.widgets.MultiField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.IgnoreFields;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.IgnoreMembers;
import com.exadel.aem.toolkit.plugin.util.TestConstants;
import com.exadel.aem.toolkit.test.widget.SelectWidget;
import com.exadel.aem.toolkit.test.widget.Tabs;

import static com.exadel.aem.toolkit.plugin.util.TestConstants.LABEL_TAB_2;
import static com.exadel.aem.toolkit.plugin.util.TestConstants.LABEL_TAB_3;

@SuppressWarnings("unused")
public class IgnoreTestCases {
    @Dialog(
            name = TestConstants.DEFAULT_COMPONENT_NAME,
            title = TestConstants.DEFAULT_COMPONENT_NAME,
            layout = DialogLayout.FIXED_COLUMNS
    )
    @IgnoreMembers({
                    @ClassMember(source = SelectWidget.class, name = "timezone"),
                    @ClassMember(source = SelectWidget.class, name = "rating")
    })
    public static class IgnoreFieldsFixedColumnsLayout extends SelectWidget {}


    @Dialog(
            name = TestConstants.DEFAULT_COMPONENT_NAME,
            title = TestConstants.DEFAULT_COMPONENT_NAME,
            layout = DialogLayout.TABS
    )
    @IgnoreFields({
            @ClassField(source = Tabs.class, field = "field3"),
            @ClassField(field = "field4") // source value falls back to the annotated class'es type
    })
    @IgnoreTabs(LABEL_TAB_3)
    public static class IgnoreFieldsTabsLayout extends Tabs {
        @DialogField
        @TextField
        private String field4;
    }

    @Dialog(
            name = TestConstants.DEFAULT_COMPONENT_NAME,
            title = TestConstants.DEFAULT_COMPONENT_NAME,
            layout = DialogLayout.TABS
    )
    @IgnoreMembers({
            @ClassMember(source = ComponentWithTabsAndInnerClass.class, name = "field1"),
            @ClassMember(source = ComponentWithTabsAndInnerClass.class, name = "field2"),
            @ClassMember(source = ComponentWithTabsAndInnerClass.class, name = "dropdown"),
            @ClassMember(source = ComponentWithTabsAndInnerClass.class, name = "fieldSet")
    })
    public static class IgnoreFieldsInFieldSet extends ComponentWithTabsAndInnerClass {
        @FieldSet(title = "Field set example")
        @PlaceOnTab(LABEL_TAB_2)
        FieldSetExampleCut fieldSet;

        @IgnoreMembers(@ClassMember(source = FieldSetExample.class, name = "field6"))
        private static class FieldSetExampleCut extends FieldSetExample {}
    }

    @Dialog(
            name = TestConstants.DEFAULT_COMPONENT_NAME,
            title = TestConstants.DEFAULT_COMPONENT_NAME,
            layout = DialogLayout.TABS
    )
    @IgnoreFields({
            @ClassField(source = ComponentWithTabsAndInnerClass.class, field = "field1"),
            @ClassField(source = ComponentWithTabsAndInnerClass.class, field = "field2"),
            @ClassField(source = ComponentWithTabsAndInnerClass.class, field = "dropdown"),
            @ClassField(source = ComponentWithTabsAndInnerClass.class, field = "fieldSet"),
            @ClassField(field = "localIgnored")
    })
    public static class IgnoreFieldsImposedOnFieldSet extends ComponentWithTabsAndInnerClass {
        @FieldSet(title = "Field set example")
        @PlaceOnTab(LABEL_TAB_2)
        @IgnoreMembers(@ClassMember(name = "field6")) // sourceClass value falls back to the annotated field's type
        FieldSetExample fieldSet;

        @DialogField
        @TextField
        String localIgnored;
    }

    @Dialog(
            name = TestConstants.DEFAULT_COMPONENT_NAME,
            title = TestConstants.DEFAULT_COMPONENT_NAME,
            layout = DialogLayout.TABS
    )
    @IgnoreMembers({
            @ClassMember(source = ComponentWithTabsAndInnerClass.class, name = "field1"),
            @ClassMember(source = ComponentWithTabsAndInnerClass.class, name = "field2"),
            @ClassMember(source = ComponentWithTabsAndInnerClass.class, name = "dropdown"),
            @ClassMember(source = ComponentWithTabsAndInnerClass.FieldSetExample.class, name = "field6")
    })
    public static class IgnoreFieldsImposedOnFieldSetClassLevel extends ComponentWithTabsAndInnerClass {}

    @Dialog(
            name = TestConstants.DEFAULT_COMPONENT_NAME,
            title = TestConstants.DEFAULT_COMPONENT_NAME,
            layout = DialogLayout.FIXED_COLUMNS
    )
    public static class IgnoreFieldsInMultifield {
        @DialogField
        @MultiField(field = SampleMultifieldCut.class)
        private List<SampleMultifieldCut> links;

        @IgnoreMembers({
                @ClassMember(source = SampleMultifieldBase.class, name = "checkbox"),
                @ClassMember(source = SampleMultifieldBase.class, name = "iconName")
        })
        private static class SampleMultifieldCut extends SampleMultifieldBase {}
    }

    @Dialog(
            name = TestConstants.DEFAULT_COMPONENT_NAME,
            title = TestConstants.DEFAULT_COMPONENT_NAME,
            layout = DialogLayout.FIXED_COLUMNS
    )
    public static class IgnoreFieldsImposedOnMultifield {
        @DialogField
        @MultiField(field = SampleMultifieldBase.class)
        @IgnoreMembers({
                @ClassMember(source = SampleMultifieldBase.class, name = "checkbox"),
                @ClassMember(name = "iconName")
        })
        private List<SampleMultifieldBase> links;
    }

    @Dialog(
            name = TestConstants.DEFAULT_COMPONENT_NAME,
            title = TestConstants.DEFAULT_COMPONENT_NAME,
            layout = DialogLayout.FIXED_COLUMNS
    )
    @IgnoreMembers({
            @ClassMember(source = SampleMultifieldBase.class, name = "checkbox"),
            @ClassMember(source = SampleMultifieldBase.class, name = "iconName"),
    })
    public static class IgnoreFieldsImposedOnMultifieldClassLevel {
        @DialogField
        @MultiField(field = SampleMultifieldBase.class)
        @IgnoreMembers({
                @ClassMember(name = "additionalLabel"),
                @ClassMember(name = "additionalInfo"),
        })
        private List<SampleMultifieldExtension> links;
    }
}
