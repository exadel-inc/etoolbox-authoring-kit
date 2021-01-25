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

package com.exadel.aem.toolkit.plugin.maven;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.plugin.util.FileSystemHelper;
import com.exadel.aem.toolkit.plugin.util.TestConstants;
import com.exadel.aem.toolkit.plugin.util.writer.TestXmlUtility;

import static com.exadel.aem.toolkit.plugin.util.TestConstants.RESOURCE_FOLDER_COMMON;
import static com.exadel.aem.toolkit.plugin.util.TestConstants.RESOURCE_FOLDER_COMPONENT;
import static com.exadel.aem.toolkit.plugin.util.TestConstants.RESOURCE_FOLDER_DEPENDSON;
import static com.exadel.aem.toolkit.plugin.util.TestConstants.RESOURCE_FOLDER_WIDGET;

public abstract class DefaultTestBase {
    static final Logger LOG = LoggerFactory.getLogger("AEM Authoring Toolkit Unit Tests");

    static final List<String> CLASSPATH_ELEMENTS = Arrays.asList(
        TestConstants.PLUGIN_MODULE_TARGET,
        TestConstants.API_MODULE_TARGET,
        TestConstants.PLUGIN_MODULE_TEST_TARGET
    );

    static final String INSTANTIATION_EXCEPTION_MESSAGE = "Could not initialize instance of class ";
    static final String CLEANUP_EXCEPTION_MESSAGE = "Could not finalize testing class ";

    private static final String KEYWORD_ANNOTATION = "Annotation";
    private static final String KEYWORD_DEPENDSON = "DependsOn";
    private static final String KEYWORD_WIDGET = "Widget";

    private static final String SUFFIX_PATTERN = "(Widget|Annotation)$";

    private static FileSystemHelper fileSystemHelper;

    @BeforeClass
    public static void setUp() {
        fileSystemHelper = new FileSystemHelper();
        PluginRuntime.contextBuilder()
                .classPathElements(CLASSPATH_ELEMENTS)
                .packageBase(StringUtils.EMPTY)
                .terminateOn("none")
                .build();
    }

    @AfterClass
    public static void finalizeAll() throws IOException {
        fileSystemHelper.close();
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
        Path pathToExpectedContent = Paths.get(TestConstants.CONTENT_ROOT_PATH, pathElements).toAbsolutePath();
        try {
            boolean result = TestXmlUtility.doTest(fileSystemHelper.getFileSystem(), testable.getName(), pathToExpectedContent);
            Assert.assertTrue(result);
        } catch (ClassNotFoundException cnfe) {
            LOG.error(INSTANTIATION_EXCEPTION_MESSAGE + testable.getName(), cnfe);
        } catch (IOException ioe) {
            LOG.error(CLEANUP_EXCEPTION_MESSAGE + testable.getName(), ioe);
        }
    }
}
