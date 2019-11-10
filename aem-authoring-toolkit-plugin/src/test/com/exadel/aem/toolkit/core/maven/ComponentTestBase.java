package com.exadel.aem.toolkit.core.maven;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.junit.Assert;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.core.util.TestHelper;
import com.exadel.aem.toolkit.core.util.TestsConstants;

public abstract class ComponentTestBase {
    private static final Logger LOG = LoggerFactory.getLogger(DialogsTest.class);

    @Before
    public void setUp() {
        List<String> classpathElements = Arrays.asList(
                TestsConstants.PLUGIN_MODULE_TARGET,
                TestsConstants.API_MODULE_TARGET,
                TestsConstants.PLUGIN_MODULE_TEST_TARGET
        );
        PluginRuntime.initialize(classpathElements, StringUtils.EMPTY,"all");
    }

    void testComponent(Class<?> tested) {
        try {
            String className = tested.getSimpleName();
            Path componentPathExpected = Paths.get(getResourceFolder(tested));
            Assert.assertTrue(TestHelper.doTest(className, componentPathExpected));
        } catch (ClassNotFoundException ex) {
            LOG.error("Cannot find class " + tested.getName(), ex);
        }
    }

    private String getResourceFolder(Class<?> tested) {
        return TestsConstants.PATH_TO_EXPECTED_FILES + "\\" + tested.getSimpleName().replace("Test", "dialog");
    }
}
