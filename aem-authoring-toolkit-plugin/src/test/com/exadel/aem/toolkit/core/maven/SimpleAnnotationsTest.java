package com.exadel.aem.toolkit.core.maven;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import com.exadel.aem.toolkit.TestAttributes;
import com.exadel.aem.toolkit.TestCustomAnnotations;
import com.exadel.aem.toolkit.TestCustomHandler;
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
import com.exadel.aem.toolkit.api.handlers.DialogWidgetHandler;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;
import com.exadel.aem.toolkit.core.util.TestsConstants;

import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

public class SimpleAnnotationsTest extends ComponentTestBase {


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
        List<String> classpathElements = new ArrayList<>();
        classpathElements.add(TestsConstants.TESTCASE_PACKAGE);

        List<DialogWidgetHandler> customHandlers = new ArrayList<>();
        customHandlers.add(new TestCustomHandler());

        spy(PluginRuntime.class);
        PluginRuntimeContext pluginRuntimeContext = Mockito.spy(new LoadedRuntimeContext(classpathElements, "all)"));
        PluginReflectionUtility pluginReflectionUtility = Mockito.spy(PluginReflectionUtility.class);

        when(PluginRuntime.context()).thenReturn(pluginRuntimeContext);
        doReturn(pluginReflectionUtility).when(pluginRuntimeContext).getReflectionUtility();
        doReturn(customHandlers).when(pluginReflectionUtility).getCustomDialogComponentHandlers();

        testComponent(TestCustomAnnotations.class);
    }
}
