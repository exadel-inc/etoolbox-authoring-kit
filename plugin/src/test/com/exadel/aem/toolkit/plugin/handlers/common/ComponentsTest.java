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
package com.exadel.aem.toolkit.plugin.handlers.common;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.commons.lang3.StringUtils;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.plugin.handlers.common.cases.components.ComplexComponent1;
import com.exadel.aem.toolkit.plugin.handlers.common.cases.components.ComplexComponent2;
import com.exadel.aem.toolkit.plugin.handlers.common.cases.components.ComponentWithPanelsAsNestedClasses;
import com.exadel.aem.toolkit.plugin.handlers.common.cases.components.ComponentWithRichTextAndExternalClasses;
import com.exadel.aem.toolkit.plugin.handlers.common.cases.components.ComponentWithTabsAndInnerClass;
import com.exadel.aem.toolkit.plugin.handlers.common.cases.components.ComponentWithTabsAsNestedClasses;
import com.exadel.aem.toolkit.plugin.handlers.common.cases.components.ComponentWithoutDialog;
import com.exadel.aem.toolkit.plugin.handlers.common.cases.components.MultiColumnDialog;
import com.exadel.aem.toolkit.plugin.handlers.common.cases.viewpattern.component1.ComplexComponentHolder;
import com.exadel.aem.toolkit.plugin.maven.FileSystemRule;
import com.exadel.aem.toolkit.plugin.maven.PluginContextRenderingRule;
import com.exadel.aem.toolkit.plugin.maven.TestConstants;

public class ComponentsTest {

    private static final Logger LOG = LoggerFactory.getLogger(ComponentsTest.class);

    @ClassRule
    public static FileSystemRule fileSystemHost = new FileSystemRule();

    @Rule
    public PluginContextRenderingRule pluginContext = new PluginContextRenderingRule(fileSystemHost.getFileSystem());

    @Test
    public void testMultiColumnLayout() {
        pluginContext.test(MultiColumnDialog.class);
    }

    @Test
    public void testComponentWithRichTextAndExternalClasses() {
        pluginContext.test(ComponentWithRichTextAndExternalClasses.class);
    }

    @Test
    public void testDialogWithTabsAndInnerClass() {
        pluginContext.test(ComponentWithTabsAndInnerClass.class);
    }

    @Test
    public void testDialogWithTabsAsNestedClasses() {
        pluginContext.test(ComponentWithTabsAsNestedClasses.class);
    }

    @Test
    public void testDialogWithPanelsAsNestedClasses() {
        pluginContext.test(ComponentWithPanelsAsNestedClasses.class);
    }

    @Test
    public void testComplexComponent1() {
        pluginContext.test(ComplexComponent1.class);
    }

    @Test
    public void testComplexComponent2() {
        pluginContext.test(ComplexComponent2.class);
    }

    @Test
    public void testComponentWithoutDialog() {
        pluginContext.test(ComponentWithoutDialog.class);
    }

    @Test
    public void testComponentViewPattern() {
        Path targetPath = Paths.get(TestConstants.CONTENT_ROOT_PATH, "component/viewPattern/component1").toAbsolutePath();
        String outdatedContentXml = readFile(Paths.get(TestConstants.CONTENT_ROOT_PATH, "common/editConfig/.content.xml"));
        pluginContext.test(
            ComplexComponentHolder.class,
            targetPath,
            fileSystem -> writeFile(fileSystem.getPath(TestConstants.PACKAGE_ROOT_PATH, TestConstants.DEFAULT_COMPONENT_NAME, ".content.xml"), outdatedContentXml));
    }

    private static String readFile(Path path) {
        try {
            return String.join(StringUtils.EMPTY, Files.readAllLines(path));
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
        return StringUtils.EMPTY;
    }

    private static void writeFile(Path path, String content) {
        try(BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE)) {
            writer.write(content);
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }
}
