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

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import static com.exadel.aem.toolkit.plugin.maven.TestConstants.RESOURCE_FOLDER_WIDGETS;

import com.exadel.aem.toolkit.plugin.handlers.common.cases.components.ComplexComponent1;
import com.exadel.aem.toolkit.plugin.handlers.common.cases.components.ComplexComponent2;
import com.exadel.aem.toolkit.plugin.handlers.common.cases.components.ComponentWithPanelsAsNestedClasses;
import com.exadel.aem.toolkit.plugin.handlers.common.cases.components.ComponentWithRichTextAndExternalClasses;
import com.exadel.aem.toolkit.plugin.handlers.common.cases.components.ComponentWithTabsAndInnerClass;
import com.exadel.aem.toolkit.plugin.handlers.common.cases.components.ComponentWithTabsAsNestedClasses;
import com.exadel.aem.toolkit.plugin.handlers.common.cases.components.ComponentWithoutDialog;
import com.exadel.aem.toolkit.plugin.handlers.common.cases.components.CustomAnnotationsComponent;
import com.exadel.aem.toolkit.plugin.handlers.common.cases.components.MultiColumnDialog;
import com.exadel.aem.toolkit.plugin.handlers.common.cases.components.ScriptedComponent;
import com.exadel.aem.toolkit.plugin.handlers.common.cases.components.viewpattern.component1.ComplexComponentHolder;
import com.exadel.aem.toolkit.plugin.maven.FileSystemRule;
import com.exadel.aem.toolkit.plugin.maven.FileUtil;
import com.exadel.aem.toolkit.plugin.maven.PluginContextRenderingRule;
import com.exadel.aem.toolkit.plugin.maven.TestConstants;
import com.exadel.aem.toolkit.plugin.maven.ThrowsPluginException;

public class ComponentsTest {

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
    @ThrowsPluginException
    public void testComplexComponent2() {
        pluginContext.test(ComplexComponent2.class);
    }

    @Test
    public void testCustomAnnotationsComponent() {
        pluginContext.test(CustomAnnotationsComponent.class, RESOURCE_FOLDER_WIDGETS, "custom");
    }

    @Test
    public void testComponentWithoutDialog() {
        pluginContext.test(ComponentWithoutDialog.class);
    }

    @Test
    public void testComponentViewPattern() {
        Path targetPath = Paths.get(TestConstants.CONTENT_ROOT_PATH, "handlers/common/components/viewPattern/component1").toAbsolutePath();
        String outdatedContentXml = FileUtil.readFile(Paths.get(TestConstants.CONTENT_ROOT_PATH, "handlers/common/editConfig/.content.xml"));
        pluginContext.test(
            ComplexComponentHolder.class,
            targetPath,
            fileSystem -> FileUtil.writeFile(fileSystem.getPath(TestConstants.PACKAGE_ROOT_PATH, TestConstants.DEFAULT_COMPONENT_NAME, ".content.xml"), outdatedContentXml));
    }

    @Test
    public void testScriptingSupport() {
        pluginContext.test(ScriptedComponent.class);
    }
}
