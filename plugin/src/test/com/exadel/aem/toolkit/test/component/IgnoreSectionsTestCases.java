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

import com.exadel.aem.toolkit.api.annotations.layouts.Accordion;
import com.exadel.aem.toolkit.api.annotations.layouts.AccordionPanel;
import com.exadel.aem.toolkit.api.annotations.layouts.Place;
import com.exadel.aem.toolkit.api.annotations.layouts.Tab;
import com.exadel.aem.toolkit.api.annotations.layouts.Tabs;
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.ClassMember;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.Ignore;
import com.exadel.aem.toolkit.plugin.utils.TestConstants;
import com.exadel.aem.toolkit.test.widget.AccordionWidget;

@SuppressWarnings("unused")
public class IgnoreSectionsTestCases {

    @AemComponent(
        path = TestConstants.DEFAULT_COMPONENT_NAME,
        title = TestConstants.DEFAULT_COMPONENT_NAME
    )
    @Dialog
    @Ignore(
        sections = "Basic2",
        members = @ClassMember(source = AccordionWidget.class, value = "field1")
    )
    public static class IgnoreMemberAndSectionAccordionLayout extends AccordionWidget {
        @DialogField
        @TextField
        private String field2;
    }


    private static class IgnoreSectionParent {
    }

    @AemComponent(
        path = TestConstants.DEFAULT_COMPONENT_NAME,
        title = TestConstants.DEFAULT_COMPONENT_NAME
    )
    @Dialog
    @Tabs(@Tab(title = "Main"))
    @Ignore(sections = "Panel 2")
    public static class IgnoreSection {

        @Accordion(@AccordionPanel(title = "Panel 1"))
        @Ignore(sections = "Panel 3")
        private Child fieldSet;

        private static class Parent {
            @DialogField
            @TextField
            @Place("Panel 1")
            private String parent;
        }

        private static class Child extends Parent {
            @DialogField
            @TextField
            @Place("Panel 2") // Will not produce an exception because belongs to a section legitimately ignored
            private String child1;

            @DialogField
            @TextField
            @Place("Panel 3") // Will not produce an exception because belongs to a section legitimately ignored
            private String child2;
        }
    }
}
