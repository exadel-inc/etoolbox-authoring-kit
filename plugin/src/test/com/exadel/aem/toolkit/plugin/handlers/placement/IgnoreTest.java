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

import org.apache.commons.lang3.StringUtils;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.exadel.aem.toolkit.plugin.handlers.placement.cases.ignore.IgnoreMembersTestCases;
import com.exadel.aem.toolkit.plugin.handlers.placement.cases.ignore.IgnoreSectionsTestCases;
import com.exadel.aem.toolkit.plugin.maven.FileSystemRule;
import com.exadel.aem.toolkit.plugin.maven.PluginContextRenderingRule;

public class IgnoreTest {

    private static final String RESOURCE_FOLDER = "handlers/placement/ignore";

    @ClassRule
    public static FileSystemRule fileSystemHost = new FileSystemRule();

    @Rule
    public PluginContextRenderingRule pluginContext = new PluginContextRenderingRule(fileSystemHost.getFileSystem());

    @Test
    public void testFixedColumnsLayout() {
        pluginContext.test(IgnoreMembersTestCases.IgnoreMembersFixedColumnsLayout.class,
            RESOURCE_FOLDER,
            getFolderName(IgnoreMembersTestCases.IgnoreMembersFixedColumnsLayout.class));
    }

    @Test
    public void testTabsLayout() {
        pluginContext.test(IgnoreMembersTestCases.IgnoreMembersTabsLayout.class,
            RESOURCE_FOLDER,
            getFolderName(IgnoreMembersTestCases.IgnoreMembersTabsLayout.class));
    }

    @Test
    public void testAccordionLayout() {
        pluginContext.test(IgnoreSectionsTestCases.IgnoreMemberAndSectionAccordionLayout.class,
            RESOURCE_FOLDER,
            getFolderName(IgnoreSectionsTestCases.IgnoreMemberAndSectionAccordionLayout.class));
    }

    @Test
    public void testFieldSet1() {
        pluginContext.test(IgnoreMembersTestCases.IgnoreMembersInFieldSet.class,
            RESOURCE_FOLDER,
            getFolderName(IgnoreMembersTestCases.IgnoreMembersInFieldSet.class));
    }

    @Test
    public void testFieldSet2() {
        pluginContext.test(IgnoreMembersTestCases.IgnoreMembersImposedOnFieldSet.class,
            RESOURCE_FOLDER,
            getFolderName(IgnoreMembersTestCases.IgnoreMembersInFieldSet.class));
    }

    @Test
    public void testFieldSet3() {
        pluginContext.test(IgnoreMembersTestCases.IgnoreMembersImposedOnFieldSetClassLevel.class,
            RESOURCE_FOLDER,
            getFolderName(IgnoreMembersTestCases.IgnoreMembersInFieldSet.class));
    }

    @Test
    public void testMutlifield1() {
        pluginContext.test(IgnoreMembersTestCases.IgnoreMembersInMultifield.class,
            RESOURCE_FOLDER,
            getFolderName(IgnoreMembersTestCases.IgnoreMembersInMultifield.class));
    }

    @Test
    public void testMutlifield2() {
        pluginContext.test(IgnoreMembersTestCases.IgnoreMembersImposedOnMultifield.class,
            RESOURCE_FOLDER,
            getFolderName(IgnoreMembersTestCases.IgnoreMembersInMultifield.class));
    }

    @Test
    public void testMutlifield3() {
        pluginContext.test(IgnoreMembersTestCases.IgnoreMembersImposedOnMultifieldClassLevel.class,
            RESOURCE_FOLDER,
            getFolderName(IgnoreMembersTestCases.IgnoreMembersInMultifield.class));
    }

    @Test
    public void testNoMissingSectionErrorWhenSectionIgnored() {
        pluginContext.test(IgnoreSectionsTestCases.IgnoreSection.class,
            RESOURCE_FOLDER,
            getFolderName(IgnoreSectionsTestCases.IgnoreSection.class));

    }

    private static String getFolderName(Class<?> testClass) {
        return StringUtils.uncapitalize(testClass.getSimpleName());
    }
}
