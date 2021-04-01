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

import java.util.List;

import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnRef;
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;
import com.exadel.aem.toolkit.api.annotations.widgets.MultiField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.Multiple;
import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Attribute;
import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Data;
import com.exadel.aem.toolkit.api.annotations.widgets.property.Property;
import com.exadel.aem.toolkit.test.custom.annotation.CustomLegacyDialogAnnotation;
import com.exadel.aem.toolkit.test.custom.annotation.CustomWidgetAnnotationAuto;

import static com.exadel.aem.toolkit.plugin.utils.TestConstants.DEFAULT_COMPONENT_NAME;

@AemComponent(
        path = DEFAULT_COMPONENT_NAME,
        title = "Dialog with Multiple-Annotated Fields"
)
@Dialog
@CustomLegacyDialogAnnotation
@SuppressWarnings("unused")
public class MultipleAnnotatedWidget {
    @DialogField(
            label = "Multiple TextFields",
            name = "overriddenName",
            required = true
    )
    @TextField(
            emptyText = "empty text",
            value = "default value"
    )
    @Attribute(
            id = "text1",
            data = @Data(name = "key", value = "value")
    )
    @Multiple
    @DependsOnRef
    @Property(name="customProperty1", value = "custom value 1")
    @Property(name="customProperty2", value = "custom value 2")
    String text1;

    @DialogField(
            label = "Nested FieldSet",
            required = true
    )
    @FieldSet(namePrefix = "my")
    @Multiple
    @Property(name="customProperty", value = "custom value")
    private List<NestedFieldSet> nestedFieldSet;

    @DialogField(
            label = "Nested Multifield"
    )
    @MultiField
    @Multiple
    @DependsOnRef
    @Property(name="customProperty", value = "custom value")
    private NestedFieldSet nestedMultifield;


    private static class NestedFieldSet {
        @DialogField(label = "Nested Text 1")
        @TextField
        private String nestedText1;

        @DialogField(label = "Nested Text 2")
        @TextField
        private String nestedText2;
    }

    @DialogField
    @CustomWidgetAnnotationAuto(customField = "Custom!")
    @Multiple
    String customAnnotation;

}
