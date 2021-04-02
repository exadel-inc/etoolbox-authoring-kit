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
package com.exadel.aem.toolkit.test.widget;

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.Checkbox;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;
import com.exadel.aem.toolkit.api.annotations.widgets.Hidden;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;

import static com.exadel.aem.toolkit.plugin.utils.TestConstants.DEFAULT_COMPONENT_NAME;

@AemComponent(
    path = DEFAULT_COMPONENT_NAME,
    title = "FieldSet Widget Dialog"
)
@Dialog
@SuppressWarnings("unused")
public class FieldSetWidget {
    @DialogField(
            label="Fieldset 1",
            description = "Fieldset definition with source class specified"
    )
    @FieldSet(value = SampleFieldSet.class, namePostfix = "21")
    String fieldSet1;

    @DialogField(
        label="Fieldset 2",
        description = "Fieldset definition with implicit source class"
    )
    @FieldSet(namePrefix = "second_")
    SampleFieldSetDescendant fieldSet2;

    private static class SampleFieldSet {
        @DialogField(
            label = "Field 1 Label",
            description = "Field 1 description"
        )
        @TextField
        String textField;

        @DialogField(
            label = "Field 2 Label",
            description = "Field 2 description"
        )
        @Checkbox
        String checkboxField;

        @DialogField(name = "textField@Delete?!")
        @Hidden
        String textFieldEraser;
    }

    private static class SampleFieldSetDescendant extends SampleFieldSet {
        @DialogField(
            label = "Field 3 Label",
            description = "Field 3 description"
        )
        @TextField
        String extraField;
    }
}
