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

package com.exadel.aem.toolkit.core.maven;

import org.junit.Test;

import com.exadel.aem.toolkit.test.common.AttributesAnnotation;
import com.exadel.aem.toolkit.test.custom.CustomAnnotations;
import com.exadel.aem.toolkit.test.widget.DatePickerWidget;
import com.exadel.aem.toolkit.test.common.EditConfigAnnotation;
import com.exadel.aem.toolkit.test.widget.FileUploadWidget;
import com.exadel.aem.toolkit.test.widget.ImageUploadWidget;
import com.exadel.aem.toolkit.test.widget.MultiFieldWidget;
import com.exadel.aem.toolkit.test.widget.NestedCheckboxListWidget;
import com.exadel.aem.toolkit.test.common.PropertiesAnnotation;
import com.exadel.aem.toolkit.test.widget.NumberFieldWidget;
import com.exadel.aem.toolkit.test.widget.RadioGroupWidget;
import com.exadel.aem.toolkit.test.widget.RichTextEditorWidget;
import com.exadel.aem.toolkit.test.widget.Tabs;
import com.exadel.aem.toolkit.test.widget.AlertWidget;
import com.exadel.aem.toolkit.test.widget.SelectWidget;
import com.exadel.aem.toolkit.test.widget.TextAreaWidget;

public class WidgetsTest extends DefaultTestBase {

    @Test
    public void testAlert() {
        testComponent(AlertWidget.class);
    }

    @Test
    public void testAttributes() {
        testComponent(AttributesAnnotation.class);
    }

    @Test
    public void testDatePicker() {
        testComponent(DatePickerWidget.class);
    }

    @Test
    public void testDialogProperties() {
        testComponent(PropertiesAnnotation.class);
    }

    @Test
    public void testEditConfig() {
        testComponent(EditConfigAnnotation.class);
    }

    @Test
    public void testFileUpload() {
        testComponent(FileUploadWidget.class);
    }

    @Test
    public void testImageUpload() {
        testComponent(ImageUploadWidget.class);
    }

    @Test
    public void testMultiField() {
        testComponent(MultiFieldWidget.class);
    }

    @Test
    public void testNestedCheckboxList() {
        testComponent(NestedCheckboxListWidget.class);
    }

    @Test
    public void testNumberField() {
        testComponent(NumberFieldWidget.class);
    }

    @Test
    public void testRadioGroup() {
        testComponent(RadioGroupWidget.class);
    }

    @Test
    public void testSelect() {
        testComponent(SelectWidget.class);
    }

    @Test
    public void testTextArea() {
        testComponent(TextAreaWidget.class);
    }

    @Test
    public void testRichTextEditor() {
        testComponent(RichTextEditorWidget.class);
    }

    @Test
    public void testTabs() {
        testComponent(Tabs.class);
    }

    @Test
    public void testCustom() {
        testComponent(CustomAnnotations.class);
    }
}
