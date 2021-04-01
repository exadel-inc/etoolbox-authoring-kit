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

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.ClassMember;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.Ignore;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.Replace;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Option;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Select;
import com.exadel.aem.toolkit.api.markers._Super;
import com.exadel.aem.toolkit.plugin.utils.TestConstants;

@SuppressWarnings("unused")
public class InheritanceTestCases {

    private static class Grandparent {
        @DialogField(
            label = "Grandparent",
            ranking = 2
        )
        @TextField
        private String text;

        @DialogField(
            label = "Grandparent",
            ranking = 1
        )
        @TextField
        @Replace(
            // "Downward" replacement (technically possible but discouraged because breaks dependency inversion)
            @ClassMember(source = Child.class, value = "text2")
        )
        private String text2;

        @DialogField(
            label = "Grandparent",
            ranking = 5
        )
        @TextField
        private String text3; // Will not be rendered, because explicitly ignored in the child class
    }

    private static class Parent extends Grandparent {
        @DialogField(
            label = "Parent",
            required = true,
            ranking = 1
        )
        @TextField
        @Replace(
            @ClassMember(source = Grandparent.class, value = "text")
        )
        private String text;

        @DialogField(
            label = "Parent",
            required = true
        )
        @TextField
        @Replace(
            // Will override Grandparent#text2 and should have been overridden by Child#text2 but will remain in place
            // because the latter is in its own turn replaced by the Grandparent#text2, and when it comes to rendering
            // this field, there's no Child#text2 anymore
            @ClassMember(source = _Super.class)
        )
        private String text2;

        @DialogField(label = "Parent")
        @TextField
        private String text3; // Will not be rendered, because explicitly ignored in the child class

        @DialogField(
            label = "Parent",
            ranking = 4
        )
        @TextField
        @Replace(
            // Will not replace anything, because is considered "pointing" at itself
            // Will be placed above #text2 because having some ranking but originating from a superclass
            @ClassMember(value = "text4")
        )
        private String text4;
    }

    @AemComponent(
        path = TestConstants.DEFAULT_COMPONENT_NAME,
        resourceSuperType = TestConstants.DEFAULT_COMPONENT_SUPERTYPE,
        title = TestConstants.DEFAULT_COMPONENT_TITLE
    )
    @Dialog(title = TestConstants.DEFAULT_COMPONENT_TITLE)
    @Ignore(
        members = {
            @ClassMember(source = _Super.class, value = "text3"),
            @ClassMember(source = Grandparent.class, value = "text3")
    })
    public static class Child extends Parent {
        @DialogField(name = "./fieldset")
        @FieldSet(title = "Fieldset")
        private FieldsetChild fieldsetChild;

        @DialogField(
            label = "Child",
            ranking = 4,
            required = true
        )
        @TextField
        // Replaced by Grandparent#text2
        private String text2;

    }

    private static class FieldsetGrandparent {
        @DialogField(
            label = "Fieldset grandparent",
            ranking = 1
        )
        @TextField
        private String fieldsetText;

        @DialogField(
            label = "Fieldset grandparent"
        )
        @TextField
        private String fieldsetText3;
    }

    private static class FieldsetParent extends FieldsetGrandparent {
        @DialogField(
            label = "Fieldset parent",
            ranking = 3 // will not be effective because overridden by FieldsetChild#fiseldsetText3
        )
        @TextField
        // Will override the same-named parent "as is"
        private String fieldsetText3;

        @DialogField(
            label = "Fieldset parent",
            ranking = 2
        )
        @TextField
        private String fieldsetText2;

        @DialogField(
            label = "Fieldset parent",
            ranking = 4
        )
        @Select(
            options = {
                @Option(text = "Default option", value = ""),
                @Option(text = "Parent option 1", value = "option-1"),
                @Option(text = "Parent option 2", value = "option 2")
            }
        )
        private String fieldsetSelect;
    }

    private static class FieldsetChild extends FieldsetParent {
        @DialogField(
            label = "Fieldset child"
        )
        @TextField
        @Replace(
            @ClassMember(source = FieldsetGrandparent.class)
        )
        private String fieldsetText;

        @DialogField(
            label = "Fieldset child",
            ranking = 3
        )
        @TextField
        private String fieldsetText3;

        @DialogField(
            label = "Fieldset child",
            ranking = 4
        )

        @Select(
            options = {
                @Option(text = "Child option 1", value = "option-1"),
                @Option(text = "Child option 2", value = "option 2")
            }
        )
        private String fieldsetSelect;
    }
}
