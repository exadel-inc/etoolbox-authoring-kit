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

import com.exadel.aem.toolkit.test.component.IgnoreTestCases;

public class IgnoreTest extends DefaultTestBase {
    @Test
    public void testFixedColumnsLayout() {
        testComponent(IgnoreTestCases.IgnoreFieldsFixedColumnsLayout.class);
    }

    @Test
    public void testTabsLayout() {
        testComponent(IgnoreTestCases.IgnoreFieldsTabsLayout.class);
    }

    @Test
    public void testFieldSet1() {
        testComponent(IgnoreTestCases.IgnoreFieldsInFieldSet.class);
    }

    @Test
    public void testFieldSet2() {
        testComponent(IgnoreTestCases.IgnoreFieldsImposedOnFieldSet.class,
                "dialogIgnoreFieldsInFieldSet");
    }

    @Test
    public void testFieldSet3() {
        testComponent(IgnoreTestCases.IgnoreFieldsImposedOnFieldSetClassLevel.class,
                "dialogIgnoreFieldsInFieldSet");
    }

    @Test
    public void testMutlifield1() {
        testComponent(IgnoreTestCases.IgnoreFieldsInMultifield.class);
    }

    @Test
    public void testMutlifield2() {
        testComponent(IgnoreTestCases.IgnoreFieldsImposedOnMultifield.class,
                "dialogIgnoreFieldsInMultifield");
    }

    @Test
    public void testMutlifield3() {
        testComponent(IgnoreTestCases.IgnoreFieldsImposedOnMultifieldClassLevel.class,
                "dialogIgnoreFieldsInMultifield");
    }
}
