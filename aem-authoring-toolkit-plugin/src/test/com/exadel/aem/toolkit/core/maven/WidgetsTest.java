package com.exadel.aem.toolkit.core.maven;

import com.exadel.aem.toolkit.TestAlert;
import com.exadel.aem.toolkit.TestAttributes;
import com.exadel.aem.toolkit.TestCustomAnnotations;
import com.exadel.aem.toolkit.TestDatePicker;
import com.exadel.aem.toolkit.TestEditConfig;
import com.exadel.aem.toolkit.TestFieldSet;
import com.exadel.aem.toolkit.TestFileUpload;
import com.exadel.aem.toolkit.TestMultiField;
import com.exadel.aem.toolkit.TestNestedCheckboxList;
import com.exadel.aem.toolkit.TestProperties;
import com.exadel.aem.toolkit.TestRadioGroup;
import com.exadel.aem.toolkit.TestSelect;
import com.exadel.aem.toolkit.TestTabs;
import org.junit.Test;

public class WidgetsTest extends ComponentTestBase {

    @Test
    public void testAlert() {
        testComponent(TestAlert.class);
    }

    @Test
    public void testAttributes() {
        testComponent(TestAttributes.class);
    }

    @Test
    public void testDatePicker() {
        testComponent(TestDatePicker.class);
    }

    @Test
    public void testDialogProperties() {
        testComponent(TestProperties.class);
    }

    @Test
    public void testEditConfig() {
        testComponent(TestEditConfig.class);
    }

    @Test
    public void testFieldSet() {
        testComponent(TestFieldSet.class);
    }

    @Test
    public void testFileUpload() {
        testComponent(TestFileUpload.class);
    }

    @Test
    public void testImageUpload() {
        testComponent(TestFileUpload.class);
    }

    @Test
    public void testMultiField() {
        testComponent(TestMultiField.class);
    }

    @Test
    public void testNestedCheckboxList() {
        testComponent(TestNestedCheckboxList.class);
    }

    @Test
    public void testRadioGroup() {
        testComponent(TestRadioGroup.class);
    }

    @Test
    public void testSelect() {
        testComponent(TestSelect.class);
    }

    @Test
    public void testTabs() {
        testComponent(TestTabs.class);
    }

    @Test
    public void testCustom() {
        testComponent(TestCustomAnnotations.class);
    }
}
