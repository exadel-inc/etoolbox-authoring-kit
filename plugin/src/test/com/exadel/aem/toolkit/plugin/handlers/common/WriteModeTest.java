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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.exadel.aem.toolkit.plugin.exceptions.MissingResourceException;
import com.exadel.aem.toolkit.plugin.handlers.common.cases.WriteModeTestCases;
import com.exadel.aem.toolkit.plugin.maven.FileSystemRule;
import com.exadel.aem.toolkit.plugin.maven.FileUtil;
import com.exadel.aem.toolkit.plugin.maven.PluginContextRenderingRule;
import com.exadel.aem.toolkit.plugin.maven.TestConstants;
import com.exadel.aem.toolkit.plugin.maven.ThrowsPluginException;

public class WriteModeTest {

    @ClassRule
    public static FileSystemRule fileSystemHost = new FileSystemRule();

    @Rule
    public PluginContextRenderingRule pluginContext = new PluginContextRenderingRule(fileSystemHost.getFileSystem());

    @Test
    public void testCanCreateFolder() throws IOException {
        pluginContext.test(
            WriteModeTestCases.FolderCreatingComponent.class,
            TestConstants.NONEXISTENT_COMPONENT_NAME,
            Paths.get(TestConstants.CONTENT_ROOT_PATH, "handlers/common/writeMode/new").toAbsolutePath());
        Files.delete(fileSystemHost
            .getFileSystem()
            .getPath(TestConstants.PACKAGE_ROOT_PATH, TestConstants.NONEXISTENT_COMPONENT_NAME));
    }

    @Test
    public void testCanCleanUpFolder() {
        Path wrongContentXmlPath = Paths.get(
            TestConstants.CONTENT_ROOT_PATH,
            "handlers/common/components/generic/complexComponent1/.content.xml");
        String wrongContentXmlContent = FileUtil.readFile(wrongContentXmlPath);

        Path editConfigPath = Paths.get(
            TestConstants.CONTENT_ROOT_PATH,
            "handlers/common/editConfig/_cq_editConfig.xml");
        String editConfigContent = FileUtil.readFile(editConfigPath);

        pluginContext.test(
            WriteModeTestCases.FolderCleaningComponent.class,
            Paths.get(TestConstants.CONTENT_ROOT_PATH, "handlers/common/writeMode/open").toAbsolutePath(),
            fileSystem -> {
                FileUtil.writeFile(
                    fileSystem.getPath(TestConstants.PACKAGE_ROOT_PATH, TestConstants.DEFAULT_COMPONENT_NAME, ".content.xml"),
                    wrongContentXmlContent);
                FileUtil.writeFile(
                    fileSystem.getPath(TestConstants.PACKAGE_ROOT_PATH, TestConstants.DEFAULT_COMPONENT_NAME, "_cq_editConfig.xml"),
                    editConfigContent);
            });
    }

    @Test
    public void testCanMergeExistingXml() {
        pluginContext.test(
            WriteModeTestCases.MergingComponent.class,
            Paths.get(TestConstants.CONTENT_ROOT_PATH, "handlers/common/writeMode/merge").toAbsolutePath(),
            fileSystem -> {
                FileUtil.writeFile(
                    fileSystem.getPath(TestConstants.PACKAGE_ROOT_PATH, TestConstants.DEFAULT_COMPONENT_NAME, ".content.xml"),
                    "<jcr:root xmlns:jcr=\"http://www.jcp.org/jcr/1.0\" disableTargeting=\"{Boolean}true\" isContainer=\"{Boolean}true\"/>");
                FileUtil.writeFile(
                    fileSystem.getPath(TestConstants.PACKAGE_ROOT_PATH, TestConstants.DEFAULT_COMPONENT_NAME, "_cq_dialog.xml"),
                    "<jcr:root xmlns:jcr=\"http://www.jcp.org/jcr/1.0\" jcr:title=\"Test Component Alt\" width=\"{Double}1000.0\"><content><items><column><items><text required=\"{Boolean}true\" emptyText=\"Empty\"/></items></column></items></content></jcr:root>");
                FileUtil.writeFile(
                    fileSystem.getPath(TestConstants.PACKAGE_ROOT_PATH, TestConstants.DEFAULT_COMPONENT_NAME, "_cq_editConfig.xml"),
                    "<jcr:root xmlns:jcr=\"http://www.jcp.org/jcr/1.0\" xmlns:nt=\"http://www.jcp.org/jcr/nt/1.0\" xmlns:cq=\"http://www.day.com/jcr/cq/1.0\" xmlns:sling=\"http://sling.apache.org/jcr/sling/1.0\" emptyText=\"Empty\">" +
                        "<cq:inplaceEditing jcr:primaryType=\"nt:unstructured\" sling:resourceType=\"cq:InplaceEditingConfig\" editorType=\"text\"><config editElementQuery=\".editable-header\" jcr:primaryType=\"nt:unstructured\" propertyName=\"./header\"/></cq:inplaceEditing>" +
                        "</jcr:root>");
                FileUtil.writeFile(
                    fileSystem.getPath(TestConstants.PACKAGE_ROOT_PATH, TestConstants.DEFAULT_COMPONENT_NAME, "_cq_childEditConfig.xml"),
                    "<jcr:root xmlns:jcr=\"http://www.jcp.org/jcr/1.0\" xmlns:cq=\"http://www.day.com/jcr/cq/1.0\"><cq:listeners aftercopy=\"REFRESH_SELF\"/></jcr:root>");
            });
    }

    @Test
    @ThrowsPluginException
    public void testThrowsOnMissingPath() {
        pluginContext.testThrows(
            WriteModeTestCases.FolderMissingComponent.class,
            MissingResourceException.class,
            "not present in the package or cannot be written to");
    }
}
