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

public class PlacementTest extends DefaultTestBase {

    @Test
    public void testReplace() {
        test(ReplaceTestCases.Child.class, "placement/replace");
    }

    @Test
    public void testMultipleReplace() {
        test(MultipleReplaceTestCases.SameClassReplacement.class, "placement/multipleReplace");
        test(MultipleReplaceTestCases.HierarchyReplacement.class, "placement/multipleReplace");
    }

    @Test
    public void testOrdering() {
        test(OrderingTestCases.Test1.class, "placement/ordering1");
        test(OrderingTestCases.Test2.class, "placement/ordering2");
    }

    @Test
    public void testInheritanceOverride() {
        test(InheritanceTestCases.Child.class, "placement/inheritanceOverride");
    }
}
