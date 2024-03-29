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
import static com.exadel.aem.toolkit.plugin.maven.TestConstants.RESOURCE_FOLDER_WIDGETS;

import com.exadel.aem.toolkit.plugin.handlers.widgets.cases.AccordionWidget;
import com.exadel.aem.toolkit.plugin.handlers.widgets.cases.AlertWidget;
import com.exadel.aem.toolkit.plugin.handlers.widgets.cases.AnchorButtonWidget;
import com.exadel.aem.toolkit.plugin.handlers.widgets.cases.ButtonGroupWidget;
import com.exadel.aem.toolkit.plugin.handlers.widgets.cases.ButtonWidget;
import com.exadel.aem.toolkit.plugin.handlers.widgets.cases.ColorFieldWidget;
import com.exadel.aem.toolkit.plugin.handlers.widgets.cases.DatePickerWidget;
import com.exadel.aem.toolkit.plugin.handlers.widgets.cases.FieldSetWidget;
import com.exadel.aem.toolkit.plugin.handlers.widgets.cases.FileUploadWidget;
import com.exadel.aem.toolkit.plugin.handlers.widgets.cases.FixedColumnsWidget;
import com.exadel.aem.toolkit.plugin.handlers.widgets.cases.HeadingWidget;
import com.exadel.aem.toolkit.plugin.handlers.widgets.cases.HiddenWidget;
import com.exadel.aem.toolkit.plugin.handlers.widgets.cases.HyperlinkWidget;
import com.exadel.aem.toolkit.plugin.handlers.widgets.cases.ImageUploadWidget;
import com.exadel.aem.toolkit.plugin.handlers.widgets.cases.IncludeWidget;
import com.exadel.aem.toolkit.plugin.handlers.widgets.cases.MultiFieldWidget;
import com.exadel.aem.toolkit.plugin.handlers.widgets.cases.MultipleAnnotatedWidget;
import com.exadel.aem.toolkit.plugin.handlers.widgets.cases.NestedCheckboxListWidget;
import com.exadel.aem.toolkit.plugin.handlers.widgets.cases.NumberFieldWidget;
import com.exadel.aem.toolkit.plugin.handlers.widgets.cases.PasswordWidget;
import com.exadel.aem.toolkit.plugin.handlers.widgets.cases.PathFieldWidget;
import com.exadel.aem.toolkit.plugin.handlers.widgets.cases.RadioGroupWidget;
import com.exadel.aem.toolkit.plugin.handlers.widgets.cases.RichTextEditorWidget;
import com.exadel.aem.toolkit.plugin.handlers.widgets.cases.SelectWidget;
import com.exadel.aem.toolkit.plugin.handlers.widgets.cases.TabsWidget;
import com.exadel.aem.toolkit.plugin.handlers.widgets.cases.TagFieldWidget;
import com.exadel.aem.toolkit.plugin.handlers.widgets.cases.TextAreaWidget;
import com.exadel.aem.toolkit.plugin.handlers.widgets.cases.TextFieldWidget;
import com.exadel.aem.toolkit.plugin.handlers.widgets.cases.TextWidget;
import com.exadel.aem.toolkit.plugin.maven.FileSystemRule;
import com.exadel.aem.toolkit.plugin.maven.PluginContextRenderingRule;

public class WidgetsTest {

    @ClassRule
    public static FileSystemRule fileSystemHost = new FileSystemRule();

    @Rule
    public PluginContextRenderingRule pluginContext = new PluginContextRenderingRule(fileSystemHost.getFileSystem());

    @Test
    public void testAccordion() {
        pluginContext.test(AccordionWidget.class, RESOURCE_FOLDER_WIDGETS, "accordion");
    }

    @Test
    public void testAlert() {
        pluginContext.test(AlertWidget.class);
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
        pluginContext.test(TabsWidget.class, RESOURCE_FOLDER_WIDGETS, "tabs");
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
        pluginContext.test(MultipleAnnotatedWidget.class, RESOURCE_FOLDER_WIDGETS, "multiple");
    }
}
