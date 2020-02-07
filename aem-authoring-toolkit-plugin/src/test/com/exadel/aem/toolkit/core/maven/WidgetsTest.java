package com.exadel.aem.toolkit.core.maven;

import org.junit.Test;

import com.exadel.aem.toolkit.api.annotations.widgets.textarea.TextArea;
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
    public void testTabs() {
        testComponent(Tabs.class);
    }

    @Test
    public void testCustom() {
        testComponent(CustomAnnotations.class);
    }
}
