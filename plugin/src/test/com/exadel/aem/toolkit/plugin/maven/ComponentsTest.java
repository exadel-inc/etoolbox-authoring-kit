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

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.plugin.utils.TestConstants;
import com.exadel.aem.toolkit.test.common.ChildEditConfigAnnotation;
import com.exadel.aem.toolkit.test.common.EditConfigAnnotation;
import com.exadel.aem.toolkit.test.component.ComplexComponent1;
import com.exadel.aem.toolkit.test.component.ComplexComponent2;
import com.exadel.aem.toolkit.test.component.ComponentWithPanelsAsNestedClasses;
import com.exadel.aem.toolkit.test.component.ComponentWithRichTextAndExternalClasses;
import com.exadel.aem.toolkit.test.component.ComponentWithTabsAndInnerClass;
import com.exadel.aem.toolkit.test.component.ComponentWithTabsAsNestedClasses;
import com.exadel.aem.toolkit.test.component.InheritanceTestCases;
import com.exadel.aem.toolkit.test.component.viewPattern.component1.ComplexComponentHolder;

public class ComponentsTest extends DefaultTestBase {
    private static final Logger LOG = LoggerFactory.getLogger(ComponentsTest.class);

    @Test
    public void testComponentWithRichTextAndExternalClasses() {
        test(ComponentWithRichTextAndExternalClasses.class);
    }

    @Test
    public void testDialogWithTabsAndInnerClass() {
        test(ComponentWithTabsAndInnerClass.class);
    }

    @Test
    public void testDialogWithTabsAsNestedClasses() {
        test(ComponentWithTabsAsNestedClasses.class);
    }

    @Test
    public void testDialogWithPanelsAsNestedClasses() {
        test(ComponentWithPanelsAsNestedClasses.class);
    }

    @Test
    public void testComplexComponent1() {
        test(ComplexComponent1.class);
    }

    @Test
    public void testComplexComponent2() {
        test(ComplexComponent2.class);
    }

    @Test
    public void testEditConfig() {
        test(EditConfigAnnotation.class);
    }

    @Test
    public void testChildEditConfig() {
        test(ChildEditConfigAnnotation.class);
    }

    @Test
    public void testInheritanceOverride() {
        test(InheritanceTestCases.Child.class, "component/inheritanceOverride");
    }

    @Test
    public void testComponentViewPattern() {
        Path targetPath = Paths.get(TestConstants.CONTENT_ROOT_PATH, "component/viewPattern/component1").toAbsolutePath();
        String outdatedContentXml = readFile(Paths.get(TestConstants.CONTENT_ROOT_PATH, "common/editConfig/.content.xml"));
        test(
            ComplexComponentHolder.class,
            targetPath,
            fileSystem -> writeFile(fileSystem.getPath(TestConstants.DEFAULT_COMPONENT_NAME, ".content.xml"), outdatedContentXml));
    }

    private static String readFile(Path path) {
        try {
            return String.join(StringUtils.EMPTY, Files.readAllLines(path));
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
        return StringUtils.EMPTY;
    }

    private static void writeFile(Path path, String content) {
        try(BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE)) {
            writer.write(content);
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }
}
