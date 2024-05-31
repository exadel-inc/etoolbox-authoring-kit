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
package com.exadel.aem.toolkit.plugin.handlers.widgets.cases;

import java.util.List;

import static com.exadel.aem.toolkit.plugin.maven.TestConstants.DEFAULT_COMPONENT_NAME;

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.Checkbox;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;
import com.exadel.aem.toolkit.api.annotations.widgets.Hidden;
import com.exadel.aem.toolkit.api.annotations.widgets.MultiField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.Multiple;

@AemComponent(
    path = DEFAULT_COMPONENT_NAME,
    title = "FieldSet Widget Dialog"
)
@Dialog
@SuppressWarnings("unused")
public class FieldSetWidget {

    @FieldSet(value = SampleFieldSet.class, namePostfix = "21")
    private String fieldSet1;

    @FieldSet(namePrefix = "second_")
    private SampleFieldSetDescendant fieldSet2;

    @FieldSet(namePrefix = "node1_")
    private SampleFieldSetWithSimpleMultifield fieldSet3;

    @FieldSet(namePrefix = "node2_")
    private SampleFieldSetWithComplexMultifield fieldSet4;

    @FieldSet(namePrefix = "node3/")
    @Multiple
    private SampleFieldSetWithSimpleMultifield fieldSet5;

    @DialogField
    @FieldSet(namePrefix = "node4/")
    @Multiple
    private SampleFieldSetWithComplexMultifield fieldSet6;

    @FieldSet(namePrefix = "node5/")
    private SampleFieldSetWithDeepMultifield fieldSet7;

    /* ----------------
       Sample fieldsets
       ---------------- */

    private static class SampleFieldSet {
        @DialogField(
            label = "Field 1 Label",
            description = "Field 1 description"
        )
        @TextField
        private String textField;

        @DialogField(
            label = "Field 2 Label",
            description = "Field 2 description"
        )
        @Checkbox
        private String checkboxField;

        @DialogField(name = "textField@Delete?!")
        @Hidden
        private String textFieldEraser;
    }

    private static class SampleFieldSetDescendant extends SampleFieldSet {
        @DialogField(
            label = "Field 3 Label",
            description = "Field 3 description"
        )
        @TextField
        private String extraField;
    }

    private static class SampleFieldSetWithSimpleMultifield {
        @DialogField
        @TextField
        private String fieldsetTitle;

        @DialogField
        @MultiField
        private List<SimpleMultifieldItem> multifieldItems;
    }

    private static class SampleFieldSetWithComplexMultifield {
        @DialogField
        @TextField
        private String fieldsetTitle;

        @DialogField
        @MultiField
        private List<ComplexMultifieldItem> multifieldItems;
    }

    private static class SampleFieldSetWithDeepMultifield {
        @DialogField
        @TextField
        private String fieldsetTitle;

        @DialogField
        @MultiField
        private List<MultifieldItemWithNestedFieldSet> multifieldItems;
    }

    /* -----------------------
       Sample multifield items
       ----------------------- */

    private static class SimpleMultifieldItem {
        @DialogField
        @TextField
        private String multifieldTitle;
    }

    private static class ComplexMultifieldItem {
        @DialogField
        @TextField
        private String title;

        @DialogField
        @TextField
        private String description;
    }

    private static class MultifieldItemWithNestedFieldSet {
        @DialogField
        @TextField
        private String title;

        @DialogField
        @FieldSet(namePrefix = "node6_")
        private SimpleMultifieldItem nestedFieldSet;
    }

}
