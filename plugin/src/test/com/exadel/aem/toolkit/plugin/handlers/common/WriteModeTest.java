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
import java.nio.file.Paths;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.exadel.aem.toolkit.plugin.exceptions.MissingResourceException;
import com.exadel.aem.toolkit.plugin.maven.FileSystemRule;
import com.exadel.aem.toolkit.plugin.maven.PluginContextRenderingRule;
import com.exadel.aem.toolkit.plugin.maven.TestConstants;
import com.exadel.aem.toolkit.plugin.maven.ThrowsPluginException;
import com.exadel.aem.toolkit.test.component.WriteModeTestCases;

public class WriteModeTest {

    @ClassRule
    public static FileSystemRule fileSystemHost = new FileSystemRule();

    @Rule
    public PluginContextRenderingRule pluginContext = new PluginContextRenderingRule(fileSystemHost.getFileSystem());

    @Test
    public void testComponentCreatingFolder() throws IOException {
        pluginContext.test(
            WriteModeTestCases.FolderCreatingComponent.class,
            TestConstants.NONEXISTENT_COMPONENT_NAME,
            Paths.get(TestConstants.CONTENT_ROOT_PATH, TestConstants.RESOURCE_FOLDER_COMPONENT, "createdFolder").toAbsolutePath());
        Files.delete(fileSystemHost
            .getFileSystem()
            .getPath(TestConstants.PACKAGE_ROOT_PATH, TestConstants.NONEXISTENT_COMPONENT_NAME));
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
