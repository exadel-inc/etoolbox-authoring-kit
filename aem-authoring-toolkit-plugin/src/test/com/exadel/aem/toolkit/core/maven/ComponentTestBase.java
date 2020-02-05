package com.exadel.aem.toolkit.core.maven;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import org.junit.Assert;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.core.util.TestHelper;
import com.exadel.aem.toolkit.core.util.TestsConstants;

public abstract class ComponentTestBase {
    private static final Logger LOG = LoggerFactory.getLogger(DialogsTest.class);

    private static final String KEYWORD_TEST = "Test";
    private static final String KEYWORD_DIALOG = "dialog";
    private static final String SUFFIX_PATTERN = "(Widget|Annotation)$";

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
        Path componentPathExpected = Paths.get(getResourceFolder(tested));
        try {
            Assert.assertTrue(TestHelper.doTest(tested.getName(), componentPathExpected));
        } catch (ClassNotFoundException ex) {
            LOG.error("Cannot initialize instance of class " + tested.getName(), ex);
        }
    }

    private String getResourceFolder(Class<?> tested) {
        String folderName = tested.getSimpleName().contains(KEYWORD_TEST)
                ?  tested.getSimpleName().replace(KEYWORD_TEST, KEYWORD_DIALOG)
                : KEYWORD_DIALOG + tested.getSimpleName();
        return TestsConstants.PATH_TO_EXPECTED_FILES + "\\" + RegExUtils.removePattern(folderName, SUFFIX_PATTERN);
    }
}
