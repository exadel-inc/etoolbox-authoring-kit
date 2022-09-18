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

import com.exadel.aem.toolkit.plugin.base.FileSystemRule;
import com.exadel.aem.toolkit.plugin.base.TestConstants;
import com.exadel.aem.toolkit.plugin.maven.PluginContextRenderingRule;
import com.exadel.aem.toolkit.test.component.ForceIgnoreFreshnessTestCases;

public class IgnoreFreshnessTest {

    private static final String FOLDER_IGNORE_FRESHNESS = "forceIgnoreFreshness";

    @ClassRule
    public static FileSystemRule fileSystemHost = new FileSystemRule();

    @Rule
    public PluginContextRenderingRule pluginContext = new PluginContextRenderingRule(fileSystemHost.getFileSystem());

    @Test
    public void testForceIgnoreFreshness() {
        pluginContext.test(ForceIgnoreFreshnessTestCases.SimpleDialog.class,
            TestConstants.RESOURCE_FOLDER_COMPONENT,
            FOLDER_IGNORE_FRESHNESS,
            "simple");
        pluginContext.test(ForceIgnoreFreshnessTestCases.TabbedDialog.class,
            TestConstants.RESOURCE_FOLDER_COMPONENT,
            FOLDER_IGNORE_FRESHNESS,
            "tabbed");
        pluginContext.test(ForceIgnoreFreshnessTestCases.AccordionDialog.class,
            TestConstants.RESOURCE_FOLDER_COMPONENT,
            FOLDER_IGNORE_FRESHNESS,
            "accordion");
    }
}
