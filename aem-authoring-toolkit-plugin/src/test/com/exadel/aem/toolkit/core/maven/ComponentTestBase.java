package com.exadel.aem.toolkit.core.maven;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.TestDependsOnRequired;
import com.exadel.aem.toolkit.TestDependsOnSetFragmentReference;
import com.exadel.aem.toolkit.core.util.PackageWriter;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;
import com.exadel.aem.toolkit.core.util.TestDialogHelper;
import com.exadel.aem.toolkit.core.util.TestsConstants;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PackageWriter.class, PluginReflectionUtility.class, PluginRuntime.class})
public abstract class ComponentTestBase {
    private static final Logger LOG = LoggerFactory.getLogger(DialogsTest.class);

    @Before
    public void setUp() {
        List<String> classpathElements = new ArrayList<>();
        classpathElements.add(TestsConstants.TESTCASE_PACKAGE);
        PluginRuntime.initialize(classpathElements, "all");
    }

    void testComponent(Class<?> tested) {
        try {
            String className = tested.getSimpleName();
            Path componentPathExpected = Paths.get(getResourceFolder(tested));
            Assert.assertTrue(TestDialogHelper.testDialogAnnotation(className, componentPathExpected));
        } catch (ClassNotFoundException ex) {
            LOG.error("Cannot find class " + tested.getName(), ex);
        }
    }

    private String getResourceFolder(Class<?> tested) {
        return TestsConstants.PATH_TO_EXPECTED_FILES + "\\" + tested.getSimpleName().replace("Test", "dialog");
    }
}
