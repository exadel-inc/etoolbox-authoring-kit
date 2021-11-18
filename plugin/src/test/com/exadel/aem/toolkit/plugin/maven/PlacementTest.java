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

import org.junit.Test;

import com.exadel.aem.toolkit.test.component.InheritanceTestCases;
import com.exadel.aem.toolkit.test.component.placement.MultipleReplaceTestCases;
import com.exadel.aem.toolkit.test.component.placement.OrderingTestCases;
import com.exadel.aem.toolkit.test.component.placement.ReplaceTestCases;
import com.exadel.aem.toolkit.test.component.placement.coincidences.ClassInterfaceCoincidenceTestCases;
import com.exadel.aem.toolkit.test.component.placement.coincidences.ClassParentCoincidenceTestCases;
import com.exadel.aem.toolkit.test.component.placement.coincidences.ClassParentInterfaceCoincidenceTestCases;
import com.exadel.aem.toolkit.test.component.placement.coincidences.SameClassCoincidenceTestCases;

public class PlacementTest extends DefaultTestBase {

    private static final String TESTCASE_ROOT = "placement";
    private static final String FOLDER_COINCIDING = "coincidingNames";
    private static final String FOLDER_MULTIPLE = "multipleReplace";

    @Test
    public void testReplace() {
        test(ReplaceTestCases.Child.class, TESTCASE_ROOT, "replace");
    }

    @Test
    public void testMultipleReplace() {
        test(MultipleReplaceTestCases.SameClassReplacement.class, TESTCASE_ROOT, FOLDER_MULTIPLE);
        test(MultipleReplaceTestCases.HierarchyReplacement.class, TESTCASE_ROOT, FOLDER_MULTIPLE);
    }

    @Test
    public void testOrdering() {
        test(OrderingTestCases.Test1.class, TESTCASE_ROOT, "ordering1");
        test(OrderingTestCases.Test2.class, TESTCASE_ROOT, "ordering2");
    }

    @Test
    public void testInheritanceOverride() {
        test(InheritanceTestCases.Child.class, TESTCASE_ROOT,"inheritanceOverride");
    }

    @Test
    public void testCoincidingNames() {

        test(SameClassCoincidenceTestCases.NoIssue.class, TESTCASE_ROOT, FOLDER_COINCIDING, "noIssue");
        test(SameClassCoincidenceTestCases.CoincidenceResolved.class, TESTCASE_ROOT, FOLDER_COINCIDING, "resolved");

        test(ClassInterfaceCoincidenceTestCases.NoIssue.class, TESTCASE_ROOT, FOLDER_COINCIDING, "noIssue");
        test(ClassInterfaceCoincidenceTestCases.CoincidenceResolved.class, TESTCASE_ROOT, FOLDER_COINCIDING, "resolved");

        test(ClassParentCoincidenceTestCases.CoincidenceResolved.class, TESTCASE_ROOT, FOLDER_COINCIDING, "resolved");

        test(ClassParentInterfaceCoincidenceTestCases.CoincidenceResolved.class, TESTCASE_ROOT, FOLDER_COINCIDING, "resolved");
    }
}
