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

public abstract class DefaultTestBase {
    static final Logger LOG = LoggerFactory.getLogger("AEM Authoring Toolkit Unit Tests");

    private static final String KEYWORD_TEST = "Test";
    private static final String KEYWORD_DIALOG = "dialog";
    private static final String SUFFIX_PATTERN = "(Widget|Annotation)$";

    private static final String EXCEPTION_SETTING = "none";

    @Before
    public void setUp() {
        List<String> classpathElements = Arrays.asList(
                TestsConstants.PLUGIN_MODULE_TARGET,
                TestsConstants.API_MODULE_TARGET,
                TestsConstants.PLUGIN_MODULE_TEST_TARGET
        );
        PluginRuntime.initialize(classpathElements, StringUtils.EMPTY, getExceptionSetting());
    }

    void testComponent(Class<?> tested) {
        Path componentPathExpected = Paths.get(getResourceFolder(tested));
        try {
            Assert.assertTrue(TestHelper.doTest(tested.getName(), componentPathExpected));
        } catch (ClassNotFoundException ex) {
            LOG.error("Cannot initialize instance of class " + tested.getName(), ex);
        }
    }

    String getExceptionSetting() {
        return EXCEPTION_SETTING;
    }

    private String getResourceFolder(Class<?> tested) {
        String folderName = tested.getSimpleName().contains(KEYWORD_TEST)
                ?  tested.getSimpleName().replace(KEYWORD_TEST, KEYWORD_DIALOG)
                : KEYWORD_DIALOG + tested.getSimpleName();
        return TestsConstants.PATH_TO_EXPECTED_FILES + "\\" + RegExUtils.removePattern(folderName, SUFFIX_PATTERN);
    }
}
