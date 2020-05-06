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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.api.annotations.meta.DialogWidgetAnnotation;
import com.exadel.aem.toolkit.api.annotations.meta.IgnoreValue;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyName;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Attribute;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Select;

import static com.exadel.aem.toolkit.core.util.TestConstants.DEFAULT_COMPONENT_NAME;
import static com.exadel.aem.toolkit.core.util.TestConstants.DEFAULT_COMPONENT_TITLE;

@Dialog(
        name = DEFAULT_COMPONENT_NAME,
        title = DEFAULT_COMPONENT_TITLE,
        layout = DialogLayout.FIXED_COLUMNS
)
@SuppressWarnings({"unused", "deprecation"})
public class CompatibilityTestComponent {

    @DialogField(label="ACS List Options")
    @Select(
            acsListPath = "/path/to/acs/list",
            acsListResourceType = "acs/list/resource/type",
            addNoneOption = true
    )
    @Attribute(clas = "deprecated-class-assignment")
    private String acsListOption;


    @DialogField(label="Custom Widget")
    @CustomCompatibilityAnnotation(ignorableValue = "ignore this")
    private int customWidget;

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @DialogWidgetAnnotation(source = "customCompatibilityAnnotation")
    @ResourceType("test-components/form/customfield")
    @PropertyMapping
    @SuppressWarnings({"unused","deprecation"})
    public @interface CustomCompatibilityAnnotation {
        @PropertyName("custom")
        String customField() default "Custom annotation field";

        @IgnoreValue("ignore this")
        String ignorableValue();
    }
}


