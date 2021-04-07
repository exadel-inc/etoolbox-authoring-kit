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
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.plugin.utils.DialogConstants;
import com.exadel.aem.toolkit.plugin.utils.FileSystemHelper;
import com.exadel.aem.toolkit.plugin.utils.TestConstants;
import com.exadel.aem.toolkit.plugin.writers.TestXmlUtility;

import static com.exadel.aem.toolkit.plugin.utils.TestConstants.RESOURCE_FOLDER_COMMON;
import static com.exadel.aem.toolkit.plugin.utils.TestConstants.RESOURCE_FOLDER_COMPONENT;
import static com.exadel.aem.toolkit.plugin.utils.TestConstants.RESOURCE_FOLDER_DEPENDSON;
import static com.exadel.aem.toolkit.plugin.utils.TestConstants.RESOURCE_FOLDER_WIDGET;

public abstract class DefaultTestBase {
    static final Logger LOG = LoggerFactory.getLogger("EToolbox Authoring Kit Unit Tests");

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
                .terminateOn(DialogConstants.VALUE_NONE)
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
        test(testable, Paths.get(TestConstants.CONTENT_ROOT_PATH, pathElements).toAbsolutePath(), null);
    }

    void test(Class<?> testable, Path path, Consumer<FileSystem> preparation) {
        if (preparation != null) {
            preparation.accept(fileSystemHelper.getFileSystem());
        }
        try {
            boolean result = TestXmlUtility.doTest(fileSystemHelper.getFileSystem(), testable.getName(), path);
            Assert.assertTrue(result);
        } catch (ClassNotFoundException cnfEx) {
            LOG.error(INSTANTIATION_EXCEPTION_MESSAGE + testable.getName(), cnfEx);
        } catch (IOException ioEx) {
            LOG.error(CLEANUP_EXCEPTION_MESSAGE + testable.getName(), ioEx);
        }
    }

}
