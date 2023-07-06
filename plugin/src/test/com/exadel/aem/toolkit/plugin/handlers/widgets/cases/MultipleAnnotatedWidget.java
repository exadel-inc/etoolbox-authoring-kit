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
import com.exadel.aem.toolkit.api.annotations.widgets.autocomplete.Autocomplete;
import com.exadel.aem.toolkit.api.annotations.widgets.autocomplete.AutocompleteDatasource;
import com.exadel.aem.toolkit.api.annotations.widgets.property.Property;
import com.exadel.aem.toolkit.api.annotations.widgets.radio.RadioButton;
import com.exadel.aem.toolkit.api.annotations.widgets.radio.RadioGroup;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RichTextEditor;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RteFeatures;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.Style;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Option;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Select;
import com.exadel.aem.toolkit.plugin.annotations.cases.CustomLegacyDialogAnnotation;
import com.exadel.aem.toolkit.plugin.annotations.cases.CustomWidgetAnnotation;
import com.exadel.aem.toolkit.plugin.annotations.cases.CustomWidgetAnnotationAuto;

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
        required = true,
        wrapperClass = "some-class"
    )
    @TextField(
        emptyText = "empty text",
        value = "default value"
    )
    @Attribute(id = "text1", data = @Data(name = "key", value = "value"))
    @CustomWidgetAnnotation
    @Multiple
    @DependsOnRef
    @Property(name = "customProperty1", value = "custom value 1")
    @Property(name = "customProperty2", value = "custom value 2")
    String text1;

    @DialogField(label = "Multiple SelectFields")
    @Select(
        options = {
            @Option(
                selected = true,
                text = "first",
                value = "first"
            ),
            @Option(
                text = "second",
                value = "second"
            ),
            @Option(
                text = "third",
                value = "third"
            )
        },
        emptyText = "Please select")
    @Multiple
    String multipleSelect;

    @DialogField(label = "Multiple RadioGroups")
    @RadioGroup(buttons = {
        @RadioButton(text = "first", value = "first", checked = true),
        @RadioButton(text = "second", value = "second"),
        @RadioButton(text = "third", value = "third")
    })
    @Multiple
    String multipleRadioGroup;

    @DialogField(label = "Multiple AutoCompletes")
    @Autocomplete(datasource = @AutocompleteDatasource(namespaces = "ns"))
    @Multiple
    String multipleAutoCompletes;

    @DialogField(label = "Multiple RTEs")
    @RichTextEditor(
        features = {
            RteFeatures.LINKS_MODIFYLINK,
            RteFeatures.LINKS_UNLINK,
            RteFeatures.Popovers.STYLES,
        },
        styles = @Style(cssName = "italic", text = "Italic")
    )
    @Multiple
    private String multipleRtes;

    @DialogField(label = "Nested FieldSet", required = true)
    @FieldSet(namePrefix = "my")
    @Multiple
    @Property(name = "customProperty", value = "custom value")
    private List<NestedFieldSet> nestedFieldSet;

    @DialogField(label = "Nested Multifield")
    @MultiField(deleteHint = false, typeHint = "typeHint")
    @Multiple
    @DependsOnRef
    @Property(name = "customProperty", value = "custom value")
    private NestedFieldSet nestedMultifield;

    @DialogField
    @CustomWidgetAnnotationAuto(customField = "Custom!")
    @Multiple
    String customAnnotation;

    private static class NestedFieldSet {
        @DialogField(label = "Nested Text 1")
        @TextField
        private String nestedText1;

        @DialogField(label = "Nested Text 2")
        @TextField
        private String nestedText2;
    }
}
