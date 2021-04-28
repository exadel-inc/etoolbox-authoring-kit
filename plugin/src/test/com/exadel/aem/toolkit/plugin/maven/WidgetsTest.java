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
package com.exadel.aem.toolkit.plugin.maven;

import org.junit.Test;

import com.exadel.aem.toolkit.test.common.AttributesAnnotation;
import com.exadel.aem.toolkit.test.common.PropertiesAnnotation;
import com.exadel.aem.toolkit.test.component.PlacementTestCases;
import com.exadel.aem.toolkit.test.custom.CustomAnnotations;
import com.exadel.aem.toolkit.test.widget.AccordionWidget;
import com.exadel.aem.toolkit.test.widget.AlertWidget;
import com.exadel.aem.toolkit.test.widget.AnchorButtonWidget;
import com.exadel.aem.toolkit.test.widget.ButtonWidget;
import com.exadel.aem.toolkit.test.widget.ColorFieldWidget;
import com.exadel.aem.toolkit.test.widget.DatePickerWidget;
import com.exadel.aem.toolkit.test.widget.FieldSetWidget;
import com.exadel.aem.toolkit.test.widget.FileUploadWidget;
import com.exadel.aem.toolkit.test.widget.HeadingWidget;
import com.exadel.aem.toolkit.test.widget.HiddenWidget;
import com.exadel.aem.toolkit.test.widget.HyperlinkWidget;
import com.exadel.aem.toolkit.test.widget.ImageUploadWidget;
import com.exadel.aem.toolkit.test.widget.IncludeWidget;
import com.exadel.aem.toolkit.test.widget.MultiFieldWidget;
import com.exadel.aem.toolkit.test.widget.MultipleAnnotatedWidget;
import com.exadel.aem.toolkit.test.widget.NestedCheckboxListWidget;
import com.exadel.aem.toolkit.test.widget.NumberFieldWidget;
import com.exadel.aem.toolkit.test.widget.PasswordWidget;
import com.exadel.aem.toolkit.test.widget.RadioGroupWidget;
import com.exadel.aem.toolkit.test.widget.RichTextEditorWidget;
import com.exadel.aem.toolkit.test.widget.SelectWidget;
import com.exadel.aem.toolkit.test.widget.TabsWidget;
import com.exadel.aem.toolkit.test.widget.TagFieldWidget;
import com.exadel.aem.toolkit.test.widget.TextAreaWidget;
import com.exadel.aem.toolkit.test.widget.TextWidget;

import static com.exadel.aem.toolkit.plugin.utils.TestConstants.RESOURCE_FOLDER_WIDGET;

public class WidgetsTest extends DefaultTestBase {

    @Test
    public void testAccordion() {
        test(AccordionWidget.class, RESOURCE_FOLDER_WIDGET, "accordion");
    }

    @Test
    public void testAlert() {
        test(AlertWidget.class);
    }

    @Test
    public void testAttributes() {
        test(AttributesAnnotation.class, RESOURCE_FOLDER_WIDGET, "graniteAttributes");
    }

    @Test
    public void testAnchorButton() {
        test(AnchorButtonWidget.class);
    }

    @Test
    public void testButton() {
        test(ButtonWidget.class);
    }

    @Test
    public void testColorFieldAndHtmlTag() {
        test(ColorFieldWidget.class);
    }

    @Test
    public void testCustom() {
        test(CustomAnnotations.class, RESOURCE_FOLDER_WIDGET, "custom");
    }

    @Test
    public void testCustomProperties() {
        test(PropertiesAnnotation.class, RESOURCE_FOLDER_WIDGET, "customProperties");
    }

    @Test
    public void testDatePicker() {
        test(DatePickerWidget.class);
    }

    @Test
    public void testFieldSet() {
        test(FieldSetWidget.class);
    }

    @Test
    public void testFileUpload() {
        test(FileUploadWidget.class);
    }

    @Test
    public void testHeading() {
        test(HeadingWidget.class);
    }

    @Test
    public void testHidden() {
        test(HiddenWidget.class);
    }

    @Test
    public void testHyperlink() {
        test(HyperlinkWidget.class);
    }

    @Test
    public void testImageUpload() {
        test(ImageUploadWidget.class);
    }

    @Test
    public void testInclude() {
        test(IncludeWidget.class);
    }

    @Test
    public void testMultiField() {
        test(MultiFieldWidget.class);
    }

    @Test
    public void testNestedCheckboxList() {
        test(NestedCheckboxListWidget.class);
    }

    @Test
    public void testNumberField() {
        test(NumberFieldWidget.class);
    }

    @Test
    public void testPassword() {
        test(PasswordWidget.class);
    }

    @Test
    public void testPlacement1() {
        test(PlacementTestCases.Test1.class, "widget/place1");
    }

    @Test
    public void testPlacement2() {
        test(PlacementTestCases.Test2.class, "widget/place2");
    }

    @Test
    public void testRadioGroup() {
        test(RadioGroupWidget.class);
    }

    @Test
    public void testSelect() {
        test(SelectWidget.class);
    }

    @Test
    public void testText() {
        test(TextWidget.class);
    }

    @Test
    public void testTextArea() {
        test(TextAreaWidget.class);
    }

    @Test
    public void testRichTextEditor() {
        test(RichTextEditorWidget.class);
    }

    @Test
    public void testTabs() {
        test(TabsWidget.class, RESOURCE_FOLDER_WIDGET, "tabs");
    }

    @Test
    public void testTagField() {
        test(TagFieldWidget.class);
    }

    @Test
    public void testWidgetAnnotatedWithMultiple() {
        test(MultipleAnnotatedWidget.class, RESOURCE_FOLDER_WIDGET, "multiple");
    }
}
