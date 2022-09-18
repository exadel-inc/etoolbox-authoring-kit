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
import java.util.function.Consumer;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.exadel.aem.toolkit.plugin.maven.TestConstants.RESOURCE_FOLDER_COMPONENT;
import static com.exadel.aem.toolkit.plugin.maven.TestConstants.RESOURCE_FOLDER_DEPENDSON;
import static com.exadel.aem.toolkit.plugin.maven.TestConstants.RESOURCE_FOLDER_WIDGETS;

public class PluginContextRenderingRule extends PluginContextRule {

    private static final Logger LOG = LoggerFactory.getLogger(PluginContextRenderingRule.class);

    private static final String INSTANTIATION_EXCEPTION_MESSAGE = "Could not start testing class {}";
    private static final String CLEANUP_EXCEPTION_MESSAGE = "Could not complete testing class {}";

    private static final String EXCEPTION_MISSING_MESSAGE = "Exception of type %s was expected, none thrown";
    private static final String EXCEPTION_WRONG_TYPE_MESSAGE = "Exception of type %s was expected but actual exception was %s";
    private static final String EXCEPTION_WRONG_DETAILS_MESSAGE = "Exception with a message containing \"%s\" was expected but actual message was \"%s\"";

    private static final String KEYWORD_DEPENDSON = "DependsOn";
    private static final String KEYWORD_WIDGET = "Widget";

    private static final String SUFFIX_PATTERN = "(Widget|Annotation)$";

    private final FileSystem fileSystem;

    public PluginContextRenderingRule(FileSystem fileSystem) {
        super();
        this.fileSystem = fileSystem;
    }

    public void test(Class<?> component) {
        String subfolderName = RESOURCE_FOLDER_COMPONENT;
        if (component.getSimpleName().endsWith(KEYWORD_WIDGET)) {
            subfolderName = RESOURCE_FOLDER_WIDGETS;
        } else if (component.getSimpleName().startsWith(KEYWORD_DEPENDSON)) {
            subfolderName = RESOURCE_FOLDER_DEPENDSON;
        }
        test(component,
            subfolderName,
            StringUtils.uncapitalize(RegExUtils.removePattern(component.getSimpleName(), SUFFIX_PATTERN)));
    }

    public void test(Class<?> component, String... pathElements) {
        test(component, null, Paths.get(TestConstants.CONTENT_ROOT_PATH, pathElements).toAbsolutePath(), null);
    }

    @SuppressWarnings("SameParameterValue")
    public void test(Class<?> component, String createdFilesPath, Path sampleFilesPath) {
        test(component, createdFilesPath, sampleFilesPath, null);
    }

    @SuppressWarnings("SameParameterValue")
    public void test(Class<?> component, Path sampleFilesPath, Consumer<FileSystem> preparation) {
        test(component, null, sampleFilesPath, preparation);
    }

    private void test(Class<?> component, String createdFilesPath, Path sampleFilesPath, Consumer<FileSystem> preparation) {
        if (preparation != null) {
            preparation.accept(fileSystem);
        }
        Path effectivePath = createdFilesPath != null
            ? fileSystem.getPath(TestConstants.PACKAGE_ROOT_PATH + createdFilesPath)
            : null;
        try {
            boolean result = FileRenderingUtil.doRenderingTest(
                fileSystem,
                component.getName(),
                effectivePath,
                sampleFilesPath);
            Assert.assertTrue(result);
        } catch (ClassNotFoundException cnfEx) {
            LOG.error(INSTANTIATION_EXCEPTION_MESSAGE, component.getName(), cnfEx);
        } catch (IOException ioEx) {
            LOG.error(CLEANUP_EXCEPTION_MESSAGE, component.getName(), ioEx);
        }
    }

    public void testThrows(Class<?> component, Class<? extends Exception> exceptionType, String... messages) {
        try {
            test(component);
            throw new AssertionError(String.format(EXCEPTION_MISSING_MESSAGE, exceptionType));
        } catch (Throwable e) {
            Assert.assertTrue(
                String.format(EXCEPTION_WRONG_TYPE_MESSAGE, exceptionType, e.getClass()),
                e.getClass().equals(exceptionType) || (e.getCause() != null && e.getCause().getClass().equals(exceptionType)));
            for (String message : ArrayUtils.nullToEmpty(messages)) {
                Assert.assertTrue(
                    String.format(EXCEPTION_WRONG_DETAILS_MESSAGE, message, e.getMessage()),
                    e.getMessage().contains(message));
            }
        }
    }
}
