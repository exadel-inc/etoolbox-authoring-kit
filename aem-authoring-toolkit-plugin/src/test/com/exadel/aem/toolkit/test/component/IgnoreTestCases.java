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
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;
import com.exadel.aem.toolkit.api.annotations.widgets.MultiField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.Ignore;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.IgnoreFields;
import com.exadel.aem.toolkit.plugin.util.TestConstants;
import com.exadel.aem.toolkit.test.widget.AccordionWidget;
import com.exadel.aem.toolkit.test.widget.SelectWidget;
import com.exadel.aem.toolkit.test.widget.TabsWidget;

import static com.exadel.aem.toolkit.plugin.util.TestConstants.LABEL_TAB_2;
import static com.exadel.aem.toolkit.plugin.util.TestConstants.LABEL_TAB_3;

@SuppressWarnings("unused")
public class IgnoreTestCases {
    @Dialog(
            name = TestConstants.DEFAULT_COMPONENT_NAME,
            title = TestConstants.DEFAULT_COMPONENT_NAME
    )
    @Ignore(
        members ={
                    @ClassMember(source = SelectWidget.class, name = "timezone"),
                    @ClassMember(source = SelectWidget.class, name = "optionList"),
                    @ClassMember(source = SelectWidget.class, name = "rating")
    })
    public static class IgnoreMembersFixedColumnsLayout extends SelectWidget {}


    @Dialog(
            name = TestConstants.DEFAULT_COMPONENT_NAME,
            title = TestConstants.DEFAULT_COMPONENT_NAME
    )
    @IgnoreFields({
            @ClassField(source = TabsWidget.class, field = "field3"),
            @ClassField(field = "field4") // source value falls back to the annotated class'es type
    })
    @IgnoreTabs(LABEL_TAB_3)
    public static class IgnoreMembersTabsLayout extends TabsWidget {
        @DialogField
        @TextField
        private String field4;
    }


    @Dialog(
        name = TestConstants.DEFAULT_COMPONENT_NAME,
        title = TestConstants.DEFAULT_COMPONENT_NAME
    )
    @Ignore(
        sections = "Basic2",
        members = @ClassMember(source = AccordionWidget.class, name = "field1")
    )
    public static class IgnoreMembersAccordionLayout extends AccordionWidget {
        @DialogField
        @TextField
        private String field2;
    }

    @Dialog(
            name = TestConstants.DEFAULT_COMPONENT_NAME,
            title = TestConstants.DEFAULT_COMPONENT_NAME
    )
    @Ignore(
        members = {
            @ClassMember(source = ComponentWithTabsAndInnerClass.class, name = "field1"),
            @ClassMember(source = ComponentWithTabsAndInnerClass.class, name = "field2"),
            @ClassMember(source = ComponentWithTabsAndInnerClass.class, name = "dropdown"),
            @ClassMember(source = ComponentWithTabsAndInnerClass.class, name = "fieldSet")
    })
    public static class IgnoreMembersInFieldSet extends ComponentWithTabsAndInnerClass {
        @FieldSet(title = "Field set example")
        @PlaceOnTab(LABEL_TAB_2)
        FieldSetExampleCut fieldSet;

        @Ignore(
            members = @ClassMember(source = FieldSetExample.class, name = "field6")
        )
        private static class FieldSetExampleCut extends FieldSetExample {}
    }

    @Dialog(
            name = TestConstants.DEFAULT_COMPONENT_NAME,
            title = TestConstants.DEFAULT_COMPONENT_NAME
    )
    @IgnoreFields({
            @ClassField(source = ComponentWithTabsAndInnerClass.class, field = "field1"),
            @ClassField(source = ComponentWithTabsAndInnerClass.class, field = "field2"),
            @ClassField(source = ComponentWithTabsAndInnerClass.class, field = "dropdown"),
            @ClassField(source = ComponentWithTabsAndInnerClass.class, field = "fieldSet"),
            @ClassField(field = "localIgnored")
    })
    public static class IgnoreMembersImposedOnFieldSet extends ComponentWithTabsAndInnerClass {
        @FieldSet(title = "Field set example")
        @PlaceOnTab(LABEL_TAB_2)
        @Ignore(
            members = @ClassMember(name = "field6")
        ) // sourceClass value falls back to the annotated field's type
        FieldSetExample fieldSet;

        @DialogField
        @TextField
        String localIgnored;
    }

    @Dialog(
            name = TestConstants.DEFAULT_COMPONENT_NAME,
            title = TestConstants.DEFAULT_COMPONENT_NAME
    )
    @Ignore(
        members = {
            @ClassMember(source = ComponentWithTabsAndInnerClass.class, name = "field1"),
            @ClassMember(source = ComponentWithTabsAndInnerClass.class, name = "field2"),
            @ClassMember(source = ComponentWithTabsAndInnerClass.class, name = "dropdown"),
            @ClassMember(source = ComponentWithTabsAndInnerClass.FieldSetExample.class, name = "field6")
    })
    public static class IgnoreMembersImposedOnFieldSetClassLevel extends ComponentWithTabsAndInnerClass {}

    @Dialog(
            name = TestConstants.DEFAULT_COMPONENT_NAME,
            title = TestConstants.DEFAULT_COMPONENT_NAME
    )
    public static class IgnoreMembersInMultifield {
        @DialogField
        @MultiField(value = SampleMultifieldCut.class)
        private List<SampleMultifieldCut> links;

        @Ignore(
            members = {
                @ClassMember(source = SampleMultifieldBase.class, name = "checkbox"),
                @ClassMember(source = SampleMultifieldBase.class, name = "iconName")
        })
        private static class SampleMultifieldCut extends SampleMultifieldBase {}
    }

    @Dialog(
            name = TestConstants.DEFAULT_COMPONENT_NAME,
            title = TestConstants.DEFAULT_COMPONENT_NAME
    )
    public static class IgnoreMembersImposedOnMultifield {
        @DialogField
        @MultiField(value = SampleMultifieldBase.class)
        @Ignore(
            members = {
                @ClassMember(source = SampleMultifieldBase.class, name = "checkbox"),
                @ClassMember(name = "iconName")
        })
        private List<SampleMultifieldBase> links;
    }

    @Dialog(
            name = TestConstants.DEFAULT_COMPONENT_NAME,
            title = TestConstants.DEFAULT_COMPONENT_NAME
    )
    @Ignore(
        members = {
            @ClassMember(source = SampleMultifieldBase.class, name = "checkbox"),
            @ClassMember(source = SampleMultifieldBase.class, name = "iconName"),
    })
    public static class IgnoreMembersImposedOnMultifieldClassLevel {
        @DialogField
        @MultiField(value = SampleMultifieldBase.class)
        @Ignore(
            members = {
                @ClassMember(name = "additionalLabel"),
                @ClassMember(name = "additionalInfo"),
        })
        private List<SampleMultifieldExtension> links;
    }
}
