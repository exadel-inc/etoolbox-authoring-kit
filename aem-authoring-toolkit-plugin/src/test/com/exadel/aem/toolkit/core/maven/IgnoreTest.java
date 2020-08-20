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

package com.exadel.aem.toolkit.core.maven;

import org.junit.Test;

import com.exadel.aem.toolkit.core.util.TestConstants;
import com.exadel.aem.toolkit.test.component.IgnoreTestCases;

public class IgnoreTest extends DefaultTestBase {
    @Test
    public void testFixedColumnsLayout() {
        test(IgnoreTestCases.IgnoreFieldsFixedColumnsLayout.class,
                TestConstants.RESOURCE_FOLDER_COMMON,
                IgnoreTestCases.IgnoreFieldsFixedColumnsLayout.class.getSimpleName());
    }

    @Test
    public void testTabsLayout() {
        test(IgnoreTestCases.IgnoreFieldsTabsLayout.class,
                TestConstants.RESOURCE_FOLDER_COMMON,
                IgnoreTestCases.IgnoreFieldsTabsLayout.class.getSimpleName());
    }

    @Test
    public void testFieldSet1() {
        test(IgnoreTestCases.IgnoreFieldsInFieldSet.class,
                TestConstants.RESOURCE_FOLDER_COMMON,
                IgnoreTestCases.IgnoreFieldsInFieldSet.class.getSimpleName());
    }

    @Test
    public void testFieldSet2() {
        test(IgnoreTestCases.IgnoreFieldsImposedOnFieldSet.class,
                TestConstants.RESOURCE_FOLDER_COMMON,
                IgnoreTestCases.IgnoreFieldsInFieldSet.class.getSimpleName());
    }

    @Test
    public void testFieldSet3() {
        test(IgnoreTestCases.IgnoreFieldsImposedOnFieldSetClassLevel.class,
                TestConstants.RESOURCE_FOLDER_COMMON,
                IgnoreTestCases.IgnoreFieldsInFieldSet.class.getSimpleName());
    }

    @Test
    public void testMutlifield1() {
        test(IgnoreTestCases.IgnoreFieldsInMultifield.class,
                TestConstants.RESOURCE_FOLDER_COMMON,
                IgnoreTestCases.IgnoreFieldsInMultifield.class.getSimpleName());
    }

    @Test
    public void testMutlifield2() {
        test(IgnoreTestCases.IgnoreFieldsImposedOnMultifield.class,
                TestConstants.RESOURCE_FOLDER_COMMON,
                IgnoreTestCases.IgnoreFieldsInMultifield.class.getSimpleName());
    }

    @Test
    public void testMutlifield3() {
        test(IgnoreTestCases.IgnoreFieldsImposedOnMultifieldClassLevel.class,
                TestConstants.RESOURCE_FOLDER_COMMON,
                IgnoreTestCases.IgnoreFieldsInMultifield.class.getSimpleName());
    }
}
