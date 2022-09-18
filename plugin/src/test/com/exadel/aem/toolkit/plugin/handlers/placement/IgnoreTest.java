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

import com.exadel.aem.toolkit.plugin.base.FileSystemRule;
import com.exadel.aem.toolkit.plugin.base.TestConstants;
import com.exadel.aem.toolkit.plugin.maven.PluginContextRenderingRule;
import com.exadel.aem.toolkit.test.component.IgnoreMembersTestCases;
import com.exadel.aem.toolkit.test.component.IgnoreSectionsTestCases;

public class IgnoreTest {

    private static final String LOCAL_RESOURCE_FOLDER_NAME = "ignore";

    @ClassRule
    public static FileSystemRule fileSystemHost = new FileSystemRule();

    @Rule
    public PluginContextRenderingRule pluginContext = new PluginContextRenderingRule(fileSystemHost.getFileSystem());

    @Test
    public void testFixedColumnsLayout() {
        pluginContext.test(IgnoreMembersTestCases.IgnoreMembersFixedColumnsLayout.class,
            TestConstants.RESOURCE_FOLDER_COMMON,
            LOCAL_RESOURCE_FOLDER_NAME,
            getFolderName(IgnoreMembersTestCases.IgnoreMembersFixedColumnsLayout.class));
    }

    @Test
    public void testTabsLayout() {
        pluginContext.test(IgnoreMembersTestCases.IgnoreMembersTabsLayout.class,
            TestConstants.RESOURCE_FOLDER_COMMON,
            LOCAL_RESOURCE_FOLDER_NAME,
            getFolderName(IgnoreMembersTestCases.IgnoreMembersTabsLayout.class));
    }

    @Test
    public void testAccordionLayout() {
        pluginContext.test(IgnoreSectionsTestCases.IgnoreMemberAndSectionAccordionLayout.class,
            TestConstants.RESOURCE_FOLDER_COMMON,
            LOCAL_RESOURCE_FOLDER_NAME,
            getFolderName(IgnoreSectionsTestCases.IgnoreMemberAndSectionAccordionLayout.class));
    }

    @Test
    public void testFieldSet1() {
        pluginContext.test(IgnoreMembersTestCases.IgnoreMembersInFieldSet.class,
            TestConstants.RESOURCE_FOLDER_COMMON,
            LOCAL_RESOURCE_FOLDER_NAME,
            getFolderName(IgnoreMembersTestCases.IgnoreMembersInFieldSet.class));
    }

    @Test
    public void testFieldSet2() {
        pluginContext.test(IgnoreMembersTestCases.IgnoreMembersImposedOnFieldSet.class,
            TestConstants.RESOURCE_FOLDER_COMMON,
            LOCAL_RESOURCE_FOLDER_NAME,
            getFolderName(IgnoreMembersTestCases.IgnoreMembersInFieldSet.class));
    }

    @Test
    public void testFieldSet3() {
        pluginContext.test(IgnoreMembersTestCases.IgnoreMembersImposedOnFieldSetClassLevel.class,
            TestConstants.RESOURCE_FOLDER_COMMON,
            LOCAL_RESOURCE_FOLDER_NAME,
            getFolderName(IgnoreMembersTestCases.IgnoreMembersInFieldSet.class));
    }

    @Test
    public void testMutlifield1() {
        pluginContext.test(IgnoreMembersTestCases.IgnoreMembersInMultifield.class,
            TestConstants.RESOURCE_FOLDER_COMMON,
            LOCAL_RESOURCE_FOLDER_NAME,
            getFolderName(IgnoreMembersTestCases.IgnoreMembersInMultifield.class));
    }

    @Test
    public void testMutlifield2() {
        pluginContext.test(IgnoreMembersTestCases.IgnoreMembersImposedOnMultifield.class,
            TestConstants.RESOURCE_FOLDER_COMMON,
            LOCAL_RESOURCE_FOLDER_NAME,
            getFolderName(IgnoreMembersTestCases.IgnoreMembersInMultifield.class));
    }

    @Test
    public void testMutlifield3() {
        pluginContext.test(IgnoreMembersTestCases.IgnoreMembersImposedOnMultifieldClassLevel.class,
            TestConstants.RESOURCE_FOLDER_COMMON,
            LOCAL_RESOURCE_FOLDER_NAME,
            getFolderName(IgnoreMembersTestCases.IgnoreMembersInMultifield.class));
    }

    @Test
    public void testNoMissingSectionErrorWhenSectionIgnored() {
        pluginContext.test(IgnoreSectionsTestCases.IgnoreSection.class,
            TestConstants.RESOURCE_FOLDER_COMMON,
            LOCAL_RESOURCE_FOLDER_NAME,
            getFolderName(IgnoreSectionsTestCases.IgnoreSection.class));

    }

    private static String getFolderName(Class<?> testClass) {
        return StringUtils.uncapitalize(testClass.getSimpleName());
    }
}
