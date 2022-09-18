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

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.exadel.aem.toolkit.plugin.exceptions.InvalidLayoutException;
import com.exadel.aem.toolkit.plugin.handlers.placement.coincidence.ClassInterfaceCoincidenceTestCases;
import com.exadel.aem.toolkit.plugin.handlers.placement.coincidence.ClassParentCoincidenceTestCases;
import com.exadel.aem.toolkit.plugin.handlers.placement.coincidence.ClassParentInterfaceCoincidenceTestCases;
import com.exadel.aem.toolkit.plugin.handlers.placement.coincidence.SameClassCoincidenceTestCases;
import com.exadel.aem.toolkit.plugin.maven.FileSystemRule;
import com.exadel.aem.toolkit.plugin.maven.PluginContextRenderingRule;
import com.exadel.aem.toolkit.plugin.maven.ThrowsPluginException;

public class CoincidenceTest {
    private static final String FOLDER_COINCIDING = "placement/coincidingNames";

    @ClassRule
    public static FileSystemRule fileSystemHost = new FileSystemRule();

    @Rule
    public PluginContextRenderingRule pluginContext = new PluginContextRenderingRule(fileSystemHost.getFileSystem());

    @Test
    public void testCoincidingNamesResolved() {
        pluginContext.test(
            SameClassCoincidenceTestCases.NoIssue.class,
            FOLDER_COINCIDING,
            "noIssue");
        pluginContext.test(
            SameClassCoincidenceTestCases.CoincidenceResolved.class,
            FOLDER_COINCIDING,
            "resolved");

        pluginContext.test(
            ClassInterfaceCoincidenceTestCases.NoIssue.class,
            FOLDER_COINCIDING,
            "noIssue");
        pluginContext.test(
            ClassInterfaceCoincidenceTestCases.CoincidenceResolved.class,
            FOLDER_COINCIDING,
            "resolved");

        pluginContext.test(
            ClassParentCoincidenceTestCases.CoincidenceResolved.class,
            FOLDER_COINCIDING,
            "resolved");

        pluginContext.test(
            ClassParentInterfaceCoincidenceTestCases.CoincidenceResolved.class,
            FOLDER_COINCIDING,
            "resolved");
    }

    @Test
    @ThrowsPluginException
    public void testThrowsOnNameCoincidenceWithShadowingResourceType1() {
        pluginContext.testThrows(
            ClassInterfaceCoincidenceTestCases.CoincidenceException.class,
            InvalidLayoutException.class,
            "Method named \"getTitle\" in class \"CoincidenceException\" collides with the method named \"getTitle\" in class \"ClassInterface\"");
    }

    @Test
    @ThrowsPluginException
    public void testThrowsOnNameCoincidenceWithShadowingResourceType2() {
        pluginContext.testThrows(
            ClassParentInterfaceCoincidenceTestCases.CoincidenceException.class,
            InvalidLayoutException.class,
            "Method named \"getTitle\" in class \"CoincidenceException\" collides with the method named \"getTitle\" in class \"ParentException\"");
    }
}
