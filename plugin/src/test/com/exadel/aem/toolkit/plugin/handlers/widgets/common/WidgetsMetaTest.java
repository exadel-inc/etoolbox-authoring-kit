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
package com.exadel.aem.toolkit.plugin.handlers.widgets.common;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import static com.exadel.aem.toolkit.plugin.maven.TestConstants.RESOURCE_FOLDER_WIDGETS;

import com.exadel.aem.toolkit.plugin.handlers.widgets.common.cases.AttributesAnnotation;
import com.exadel.aem.toolkit.plugin.handlers.widgets.common.cases.OptionProviderAnnotation;
import com.exadel.aem.toolkit.plugin.handlers.widgets.common.cases.PropertiesAnnotation;
import com.exadel.aem.toolkit.plugin.maven.FileSystemRule;
import com.exadel.aem.toolkit.plugin.maven.PluginContextRenderingRule;

public class WidgetsMetaTest {

    @ClassRule
    public static FileSystemRule fileSystemHost = new FileSystemRule();

    @Rule
    public PluginContextRenderingRule pluginContext = new PluginContextRenderingRule(fileSystemHost.getFileSystem());

    @Test
    public void testAttributes() {
        pluginContext.test(AttributesAnnotation.class, RESOURCE_FOLDER_WIDGETS, "graniteAttributes");
    }

    @Test
    public void testCustomProperties() {
        pluginContext.test(PropertiesAnnotation.class, RESOURCE_FOLDER_WIDGETS, "customProperties");
    }

    @Test
    public void testOptionProvider() {
        pluginContext.test(OptionProviderAnnotation.class, RESOURCE_FOLDER_WIDGETS, "optionSource");
    }
}
