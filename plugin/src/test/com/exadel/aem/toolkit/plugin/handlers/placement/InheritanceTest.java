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
package com.exadel.aem.toolkit.plugin.handlers.placement;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.exadel.aem.toolkit.plugin.exceptions.InvalidLayoutException;
import com.exadel.aem.toolkit.plugin.handlers.placement.cases.inheritance.InheritanceExceptionTestCases;
import com.exadel.aem.toolkit.plugin.handlers.placement.cases.inheritance.InheritanceTestCases;
import com.exadel.aem.toolkit.plugin.maven.FileSystemRule;
import com.exadel.aem.toolkit.plugin.maven.PluginContextRenderingRule;
import com.exadel.aem.toolkit.plugin.maven.ThrowsPluginException;

public class InheritanceTest {

    @ClassRule
    public static FileSystemRule fileSystemHost = new FileSystemRule();

    @Rule
    public PluginContextRenderingRule pluginContext = new PluginContextRenderingRule(fileSystemHost.getFileSystem());

    @Test
    public void testInheritanceOverride() {
        pluginContext.test(InheritanceTestCases.Child.class, "handlers/placement/inheritance");
    }

    @Test
    @ThrowsPluginException
    public void testThrowsOnShadowingResourceType() {
        pluginContext.testThrows(
            InheritanceExceptionTestCases.Child2.class,
            InvalidLayoutException.class,
            "Field named \"text1\" in class \"Child2\"");
    }

    @Test
    @ThrowsPluginException
    public void testThrowsOnDuplicateFields() {
        pluginContext.testThrows(
            InheritanceExceptionTestCases.Child.class,
            InvalidLayoutException.class,
            "Field named \"text2\" in class \"Child\"");
    }
}
