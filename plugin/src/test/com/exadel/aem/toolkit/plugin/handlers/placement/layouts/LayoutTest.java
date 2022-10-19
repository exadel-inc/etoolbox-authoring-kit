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
package com.exadel.aem.toolkit.plugin.handlers.placement.layouts;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.exadel.aem.toolkit.plugin.exceptions.InvalidContainerException;
import com.exadel.aem.toolkit.plugin.exceptions.InvalidLayoutException;
import com.exadel.aem.toolkit.plugin.handlers.placement.layouts.cases.LayoutExceptionTestCases;
import com.exadel.aem.toolkit.plugin.maven.FileSystemRule;
import com.exadel.aem.toolkit.plugin.maven.PluginContextRenderingRule;
import com.exadel.aem.toolkit.plugin.maven.ThrowsPluginException;

@ThrowsPluginException
public class LayoutTest {

    private static final String MESSAGE_RECURSION_PROHIBITED = "Recursive rendering prohibited";

    @ClassRule
    public static FileSystemRule fileSystemHost = new FileSystemRule();

    @Rule
    public PluginContextRenderingRule pluginContext = new PluginContextRenderingRule(fileSystemHost.getFileSystem());

    @Test
    public void testThrowsOnNonexistentTab() {
        pluginContext.testThrows(
            LayoutExceptionTestCases.ComponentWithNonexistentTab.class,
            InvalidContainerException.class,
            "Container section \"Zeroth tab\" is not defined");
    }

    @Test
    public void testThrowsOnWrongDependsOnTab() {
        pluginContext.testThrows(
            LayoutExceptionTestCases.ComponentWithNonexistentDependsOnTab.class,
            InvalidContainerException.class,
            "Container section \"Zeroth tab\" is not defined");
    }

    @Test
    public void testThrowsOnRenderingRecursion1() {
        pluginContext.testThrows(
            LayoutExceptionTestCases.ComponentWithRecursiveMember1.class,
            InvalidLayoutException.class,
            MESSAGE_RECURSION_PROHIBITED,
            "LayoutExceptionTestCases$ComponentWithRecursiveMember1",
            "LayoutExceptionTestCases$RecursionParent");
    }

    @Test
    public void testThrowsOnRenderingRecursion2() {
        pluginContext.testThrows(
            LayoutExceptionTestCases.ComponentWithRecursiveMember2.class,
            InvalidLayoutException.class,
            MESSAGE_RECURSION_PROHIBITED,
            "LayoutExceptionTestCases$ComponentWithRecursiveMember2",
            "LayoutExceptionTestCases$RecursionInterface");
    }

    @Test
    public void testThrowsOnRenderingRecursion3() {
        pluginContext.testThrows(
            LayoutExceptionTestCases.ComponentWithRecursiveMember3.class,
            InvalidLayoutException.class,
            MESSAGE_RECURSION_PROHIBITED,
            "LayoutExceptionTestCases$RecursionChild",
            "LayoutExceptionTestCases$ComponentWithRecursiveMember3");
    }

    @Test
    public void testThrowsOnCircularPlacement1() {
        pluginContext.testThrows(
            LayoutExceptionTestCases.ComponentWithCircularPlacement1.class,
            InvalidLayoutException.class,
            "Field named \"containerB\" in class \"ComponentWithCircularPlacement1\"",
            "declared by field named \"containerA\" in class \"ComponentWithCircularPlacement1\"");
    }

    @Test
    public void testThrowsOnCircularPlacement2() {
        pluginContext.testThrows(
            LayoutExceptionTestCases.ComponentWithCircularPlacement2.class,
            InvalidLayoutException.class,
            "Field named \"containerA\" in class \"ComponentWithCircularPlacement2\"",
            "declared by field named \"containerC\" in class \"ComponentWithCircularPlacement2\""
        );
    }
}
