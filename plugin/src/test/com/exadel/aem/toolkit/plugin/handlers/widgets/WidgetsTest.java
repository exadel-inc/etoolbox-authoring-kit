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
package com.exadel.aem.toolkit.plugin.handlers.widgets;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import static com.exadel.aem.toolkit.plugin.maven.TestConstants.RESOURCE_FOLDER_WIDGET;

import com.exadel.aem.toolkit.plugin.maven.FileSystemRule;
import com.exadel.aem.toolkit.plugin.maven.PluginContextRenderingRule;
import com.exadel.aem.toolkit.test.common.AttributesAnnotation;
import com.exadel.aem.toolkit.test.common.PropertiesAnnotation;
import com.exadel.aem.toolkit.test.custom.CustomAnnotations;
import com.exadel.aem.toolkit.test.widget.AccordionWidget;
import com.exadel.aem.toolkit.test.widget.AlertWidget;
import com.exadel.aem.toolkit.test.widget.AnchorButtonWidget;
import com.exadel.aem.toolkit.test.widget.ButtonGroupWidget;
import com.exadel.aem.toolkit.test.widget.ButtonWidget;
import com.exadel.aem.toolkit.test.widget.ColorFieldWidget;
import com.exadel.aem.toolkit.test.widget.DatePickerWidget;
import com.exadel.aem.toolkit.test.widget.FieldSetWidget;
import com.exadel.aem.toolkit.test.widget.FileUploadWidget;
import com.exadel.aem.toolkit.test.widget.FixedColumnsWidget;
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
import com.exadel.aem.toolkit.test.widget.PathFieldWidget;
import com.exadel.aem.toolkit.test.widget.RadioGroupWidget;
import com.exadel.aem.toolkit.test.widget.RichTextEditorWidget;
import com.exadel.aem.toolkit.test.widget.SelectWidget;
import com.exadel.aem.toolkit.test.widget.TabsWidget;
import com.exadel.aem.toolkit.test.widget.TagFieldWidget;
import com.exadel.aem.toolkit.test.widget.TextAreaWidget;
import com.exadel.aem.toolkit.test.widget.TextFieldWidget;
import com.exadel.aem.toolkit.test.widget.TextWidget;

public class WidgetsTest {

    @ClassRule
    public static FileSystemRule fileSystemHost = new FileSystemRule();

    @Rule
    public PluginContextRenderingRule pluginContext = new PluginContextRenderingRule(fileSystemHost.getFileSystem());

    @Test
    public void testAccordion() {
        pluginContext.test(AccordionWidget.class, RESOURCE_FOLDER_WIDGET, "accordion");
    }

    @Test
    public void testAlert() {
        pluginContext.test(AlertWidget.class);
    }

    @Test
    public void testAttributes() {
        pluginContext.test(AttributesAnnotation.class, RESOURCE_FOLDER_WIDGET, "graniteAttributes");
    }

    @Test
    public void testAnchorButton() {
        pluginContext.test(AnchorButtonWidget.class);
    }

    @Test
    public void testButton() {
        pluginContext.test(ButtonWidget.class);
    }

    @Test
    public void testButtonGroup() {
        pluginContext.test(ButtonGroupWidget.class);
    }

    @Test
    public void testColorFieldAndHtmlTag() {
        pluginContext.test(ColorFieldWidget.class);
    }

    @Test
    public void testCustom() {
        pluginContext.test(CustomAnnotations.class, RESOURCE_FOLDER_WIDGET, "custom");
    }

    @Test
    public void testCustomProperties() {
        pluginContext.test(PropertiesAnnotation.class, RESOURCE_FOLDER_WIDGET, "customProperties");
    }

    @Test
    public void testDatePicker() {
        pluginContext.test(DatePickerWidget.class);
    }

    @Test
    public void testFieldSet() {
        pluginContext.test(FieldSetWidget.class);
    }

    @Test
    public void testFileUpload() {
        pluginContext.test(FileUploadWidget.class);
    }

    @Test
    public void testFixedColumns() {
        pluginContext.test(FixedColumnsWidget.class);
    }

    @Test
    public void testHeading() {
        pluginContext.test(HeadingWidget.class);
    }

    @Test
    public void testHidden() {
        pluginContext.test(HiddenWidget.class);
    }

    @Test
    public void testHyperlink() {
        pluginContext.test(HyperlinkWidget.class);
    }

    @Test
    public void testImageUpload() {
        pluginContext.test(ImageUploadWidget.class);
    }

    @Test
    public void testInclude() {
        pluginContext.test(IncludeWidget.class);
    }

    @Test
    public void testMultiField() {
        pluginContext.test(MultiFieldWidget.class);
    }

    @Test
    public void testNestedCheckboxList() {
        pluginContext.test(NestedCheckboxListWidget.class);
    }

    @Test
    public void testNumberField() {
        pluginContext.test(NumberFieldWidget.class);
    }

    @Test
    public void testPassword() {
        pluginContext.test(PasswordWidget.class);
    }

    @Test
    public void testPathField() {
        pluginContext.test(PathFieldWidget.class);
    }

    @Test
    public void testRadioGroup() {
        pluginContext.test(RadioGroupWidget.class);
    }

    @Test
    public void testSelect() {
        pluginContext.test(SelectWidget.class);
    }

    @Test
    public void testText() {
        pluginContext.test(TextWidget.class);
    }

    @Test
    public void testTextArea() {
        pluginContext.test(TextAreaWidget.class);
    }

    @Test
    public void testRichTextEditor() {
        pluginContext.test(RichTextEditorWidget.class);
    }

    @Test
    public void testTabs() {
        pluginContext.test(TabsWidget.class, RESOURCE_FOLDER_WIDGET, "tabs");
    }

    @Test
    public void testTagField() {
        pluginContext.test(TagFieldWidget.class);
    }

    @Test
    public void testTextField() {
        pluginContext.test(TextFieldWidget.class);
    }

    @Test
    public void testWidgetAnnotatedWithMultiple() {
        pluginContext.test(MultipleAnnotatedWidget.class, RESOURCE_FOLDER_WIDGET, "multiple");
    }
}
