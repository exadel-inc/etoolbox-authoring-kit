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
package com.exadel.aem.toolkit.plugin.handlers.common.cases.components;

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.plugin.annotations.cases.CustomDialogAnnotationAuto;
import com.exadel.aem.toolkit.plugin.annotations.cases.CustomLegacyDialogAnnotation;
import com.exadel.aem.toolkit.plugin.annotations.cases.CustomNonMappingWidgetAnnotation;
import com.exadel.aem.toolkit.plugin.annotations.cases.CustomScopedNonMappingWidgetAnnotation;
import com.exadel.aem.toolkit.plugin.annotations.cases.CustomWidgetAnnotation;
import com.exadel.aem.toolkit.plugin.annotations.cases.CustomWidgetAnnotationAuto;
import com.exadel.aem.toolkit.plugin.annotations.cases.CustomWidgetAutoOrder;
import com.exadel.aem.toolkit.plugin.annotations.cases.DialogAnnotationForOrderingTest;
import com.exadel.aem.toolkit.plugin.annotations.cases.WidgetAnnotationForOrderingTest;

@AemComponent(
    path = "test-component",
    title = "test-component-dialog"
)
@Dialog
@CustomLegacyDialogAnnotation
@CustomDialogAnnotationAuto(
    field1 = "value1",
    field2 = 2
)
@DialogAnnotationForOrderingTest
@CustomScopedNonMappingWidgetAnnotation(customField = "dummy value")
@SuppressWarnings("unused")
public class CustomAnnotationsComponent {
    @DialogField
    @CustomWidgetAnnotation(customField = "Overridden value")
    @CustomNonMappingWidgetAnnotation
    String testCustomAnnotation;

    @DialogField
    @CustomWidgetAnnotation(customField = "Overridden value")
    @CustomScopedNonMappingWidgetAnnotation(customField = "dummy value")
    String testScopedCustomAnnotation;

    @DialogField
    @TextField
    @CustomWidgetAnnotation
    String testCustomAnnotationDefault;

    @DialogField
    @CustomWidgetAnnotationAuto(customField = "Overridden value")
    String testCustomAnnotationAuto;

    @DialogField
    @TextField
    @CustomWidgetAnnotationAuto
    String testCustomAnnotationAutoDefault;

    @CustomWidgetAutoOrder
    String getTestAutoOrder;

    @WidgetAnnotationForOrderingTest
    boolean getTestOrder;
}
