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

import com.exadel.aem.toolkit.core.util.TestConstants;
import com.exadel.aem.toolkit.core.util.writer.TestXmlWriterHelper;

import static com.exadel.aem.toolkit.core.util.TestConstants.RESOURCE_FOLDER_COMMON;
import static com.exadel.aem.toolkit.core.util.TestConstants.RESOURCE_FOLDER_COMPONENT;
import static com.exadel.aem.toolkit.core.util.TestConstants.RESOURCE_FOLDER_DEPENDSON;
import static com.exadel.aem.toolkit.core.util.TestConstants.RESOURCE_FOLDER_WIDGET;

public abstract class DefaultTestBase {
    static final Logger LOG = LoggerFactory.getLogger("AEM Authoring Toolkit Unit Tests");

    private static final String KEYWORD_ANNOTATION = "Annotation";
    private static final String KEYWORD_DEPENDSON = "DependsOn";
    private static final String KEYWORD_WIDGET = "Widget";

    private static final String SUFFIX_PATTERN = "(Widget|Annotation)$";

    private static final String EXCEPTION_SETTING = "none";

    @Before
    public void setUp() {
        List<String> classpathElements = Arrays.asList(
                TestConstants.PLUGIN_MODULE_TARGET,
                TestConstants.API_MODULE_TARGET,
                TestConstants.PLUGIN_MODULE_TEST_TARGET
        );
        PluginRuntime.contextBuilder()
                .classPathElements(classpathElements)
                .packageBase(StringUtils.EMPTY)
                .terminateOn(getExceptionSetting())
                .build();
    }

    void test(Class<?> testable) {
        String subfolderName = RESOURCE_FOLDER_COMPONENT;
        if (testable.getSimpleName().endsWith(KEYWORD_WIDGET)) {
            subfolderName = RESOURCE_FOLDER_WIDGET;
        } else if (testable.getSimpleName().startsWith(KEYWORD_DEPENDSON)) {
            subfolderName = RESOURCE_FOLDER_DEPENDSON;
        } else if (testable.getSimpleName().endsWith(KEYWORD_ANNOTATION)) {
            subfolderName = RESOURCE_FOLDER_COMMON;
        }
        test(testable,
                subfolderName,
                StringUtils.uncapitalize(RegExUtils.removePattern(testable.getSimpleName(), SUFFIX_PATTERN)));
    }

    void test(Class<?> testable, String... pathElements) {
        Path pathToExpectedContent = Paths.get(TestConstants.EXPECTED_CONTENT_ROOT_PATH, pathElements);
        try {
            Assert.assertTrue(TestXmlWriterHelper.doTest(testable.getName(), pathToExpectedContent));
        } catch (ClassNotFoundException ex) {
            LOG.error("Cannot initialize instance of class " + testable.getName(), ex);
        }
    }

    String getExceptionSetting() {
        return EXCEPTION_SETTING;
    }
}
