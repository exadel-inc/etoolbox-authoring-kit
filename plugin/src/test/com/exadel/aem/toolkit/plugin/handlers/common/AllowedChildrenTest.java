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

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.exadel.aem.toolkit.plugin.maven.FileSystemRule;
import com.exadel.aem.toolkit.plugin.maven.PluginContextRenderingRule;
import com.exadel.aem.toolkit.test.common.AllowedChildrenAnnotation;

public class AllowedChildrenTest {

    @ClassRule
    public static FileSystemRule fileSystemHost = new FileSystemRule();

    @Rule
    public PluginContextRenderingRule pluginContext = new PluginContextRenderingRule(fileSystemHost.getFileSystem());

    @Test
    public void testSimpleContainer() {
        pluginContext.test(AllowedChildrenAnnotation.SimpleContainer.class, "common/allowedChildren/simple");
    }

    @Test
    public void testClassBasedContainer() {
        pluginContext.test(AllowedChildrenAnnotation.ClassBasedContainer.class, "common/allowedChildren/classBased");
    }

    @Test
    public void testMixedContainer() {
        pluginContext.test(AllowedChildrenAnnotation.MixedContainer.class, "common/allowedChildren/mixed");
    }

    @Test
    public void testContainerWithChildEditConfig() {
        pluginContext.test(AllowedChildrenAnnotation.ContainerChildEditConfig.class, "common/allowedChildren/childEditConfig");
    }

    @Test
    public void testContainerWithEditConfig() {
        pluginContext.test(AllowedChildrenAnnotation.ContainerEditConfig.class, "common/allowedChildren/editConfig");
    }
}
