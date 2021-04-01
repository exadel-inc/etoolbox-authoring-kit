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
package com.exadel.aem.toolkit.plugin.maven;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.exadel.aem.toolkit.plugin.utils.TestConstants;
import com.exadel.aem.toolkit.test.component.IgnoreTestCases;

public class IgnoreTest extends DefaultTestBase {
    private static final String LOCAL_RESOURCE_FOLDER_NAME = "ignore";

    @Test
    public void testFixedColumnsLayout() {
        test(IgnoreTestCases.IgnoreMembersFixedColumnsLayout.class,
            TestConstants.RESOURCE_FOLDER_COMMON,
            LOCAL_RESOURCE_FOLDER_NAME,
            getFolderName(IgnoreTestCases.IgnoreMembersFixedColumnsLayout.class));
    }

    @Test
    public void testTabsLayout() {
        test(IgnoreTestCases.IgnoreMembersTabsLayout.class,
            TestConstants.RESOURCE_FOLDER_COMMON,
            LOCAL_RESOURCE_FOLDER_NAME,
            getFolderName(IgnoreTestCases.IgnoreMembersTabsLayout.class));
    }

    @Test
    public void testAccordionLayout() {
        test(IgnoreTestCases.IgnoreMembersAccordionLayout.class,
            TestConstants.RESOURCE_FOLDER_COMMON,
            LOCAL_RESOURCE_FOLDER_NAME,
            getFolderName(IgnoreTestCases.IgnoreMembersAccordionLayout.class));
    }

    @Test
    public void testFieldSet1() {
        test(IgnoreTestCases.IgnoreMembersInFieldSet.class,
            TestConstants.RESOURCE_FOLDER_COMMON,
            LOCAL_RESOURCE_FOLDER_NAME,
            getFolderName(IgnoreTestCases.IgnoreMembersInFieldSet.class));
    }

    @Test
    public void testFieldSet2() {
        test(IgnoreTestCases.IgnoreMembersImposedOnFieldSet.class,
            TestConstants.RESOURCE_FOLDER_COMMON,
            LOCAL_RESOURCE_FOLDER_NAME,
            getFolderName(IgnoreTestCases.IgnoreMembersInFieldSet.class));
    }

    @Test
    public void testFieldSet3() {
        test(IgnoreTestCases.IgnoreMembersImposedOnFieldSetClassLevel.class,
            TestConstants.RESOURCE_FOLDER_COMMON,
            LOCAL_RESOURCE_FOLDER_NAME,
            getFolderName(IgnoreTestCases.IgnoreMembersInFieldSet.class));
    }

    @Test
    public void testMutlifield1() {
        test(IgnoreTestCases.IgnoreMembersInMultifield.class,
            TestConstants.RESOURCE_FOLDER_COMMON,
            LOCAL_RESOURCE_FOLDER_NAME,
            getFolderName(IgnoreTestCases.IgnoreMembersInMultifield.class));
    }

    @Test
    public void testMutlifield2() {
        test(IgnoreTestCases.IgnoreMembersImposedOnMultifield.class,
            TestConstants.RESOURCE_FOLDER_COMMON,
            LOCAL_RESOURCE_FOLDER_NAME,
            getFolderName(IgnoreTestCases.IgnoreMembersInMultifield.class));
    }

    @Test
    public void testMutlifield3() {
        test(IgnoreTestCases.IgnoreMembersImposedOnMultifieldClassLevel.class,
            TestConstants.RESOURCE_FOLDER_COMMON,
            LOCAL_RESOURCE_FOLDER_NAME,
            getFolderName(IgnoreTestCases.IgnoreMembersInMultifield.class));
    }

    private static String getFolderName(Class<?> testClass) {
        return StringUtils.uncapitalize(testClass.getSimpleName());
    }
}
