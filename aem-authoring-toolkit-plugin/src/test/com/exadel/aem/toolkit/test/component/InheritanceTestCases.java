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

import com.exadel.aem.toolkit.api.annotations.main.ClassField;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.ReplaceFields;
import com.exadel.aem.toolkit.core.util.TestConstants;

@SuppressWarnings("unused")
public class InheritanceTestCases {

    private static class InheritanceBase {
        @DialogField(
                label = "Base label",
                required = true,
                ranking = 2
        )
        @TextField
        private String text;

        @DialogField(label = "Base label 2")
        @TextField
        @ReplaceFields(@ClassField(source = InheritanceOverride.class, field = "text2")) // "downward" replacement (possible, but not recommended)
        private String text2;
    }

    private static class InheritanceInterim extends InheritanceBase {
        @DialogField(
                label = "Interim label",
                required = true,
                ranking = 1
        )
        @TextField
        @ReplaceFields({
                @ClassField(source = InheritanceBase.class, field = "text"), // regular "upward" replacement
                @ClassField(source = InheritanceBase.class, field = "otherText") // will be ignored as a non-existing field
        })
        private String text;
    }

    @Dialog(
            name = TestConstants.DEFAULT_COMPONENT_NAME,
            title = TestConstants.DEFAULT_COMPONENT_TITLE,
            componentGroup = TestConstants.DEFAULT_COMPONENT_GROUP,
            resourceSuperType = TestConstants.DEFAULT_COMPONENT_SUPERTYPE,
            disableTargeting = true,
            layout = DialogLayout.FIXED_COLUMNS
    )
    public static class InheritanceOverride extends InheritanceInterim {
        @DialogField(
                label = "Override label",
                required = true
        )
        @TextField
        @ReplaceFields({
                @ClassField(source = InheritanceBase.class, field = "text"),
                @ClassField(source = InheritanceInterim.class, field = "text")
        })
        private String text;

        @DialogField(
                name = "./fieldset",
                ranking = 3
        )
        @FieldSet(title = "Fieldset")
        private FieldsetOverride fieldsetOverride;

        @DialogField(
                label = "Override label 2",
                ranking = 4
        )
        @TextField
        private String text2;
    }

    private static class FieldsetBase {
        @DialogField(
                name = "fieldsetBaseText",
                label = "Fieldset base label",
                ranking = 1
        )
        @TextField
        private String fieldsetText;
    }

    private static class FieldsetInterim extends FieldsetBase {
        @DialogField(
                name = "fieldsetInterimText3",
                label = "Fieldset interim label 3",
                ranking = 3
        )
        @TextField
        private String fieldsetText3;

        @DialogField(
                name = "fieldsetInterimText",
                label = "Fieldset interim label",
                ranking = 2
        )
        @TextField
        private String fieldsetText2;
    }

    private static class FieldsetOverride extends FieldsetInterim {
        @DialogField(
                name = "fieldsetOverrideText",
                label = "Fieldset override label",
                ranking = 3 // will be ignored because this one is placed by the replace target placement
        )
        @TextField
        @ReplaceFields({
                @ClassField(source = FieldsetOverride.class, field = "fieldsetText"), // will be ignored as "self-reference"
                @ClassField(source = FieldsetBase.class, field = "fieldsetText"),
        })
        private String fieldsetText;
    }

    private static class DuplicateBase {
        @DialogField
        @TextField
        private String text1;

        @DialogField(ranking = 1)
        @TextField
        private String text2;
    }

    @Dialog(
            name = TestConstants.DEFAULT_COMPONENT_NAME,
            title = TestConstants.DEFAULT_COMPONENT_TITLE
    )
    public static class DuplicateOverride extends DuplicateBase {
        @DialogField
        @TextField
        private String text1; // will not cause an exception because placed underneath the field from superclass by order

        @DialogField
        @TextField
        private String text2; // will cause and exception because placed above the field from superclass by order
    }
}

