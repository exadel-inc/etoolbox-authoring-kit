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

import com.exadel.aem.toolkit.test.common.ChildEditConfigAnnotation;
import com.exadel.aem.toolkit.test.common.EditConfigAnnotation;
import com.exadel.aem.toolkit.test.component.ComplexComponent1;
import com.exadel.aem.toolkit.test.component.ComplexComponent2;
import com.exadel.aem.toolkit.test.component.ComponentWithRichTextAndExternalClasses;
import com.exadel.aem.toolkit.test.component.ComponentWithTabsAndInnerClass;
import com.exadel.aem.toolkit.test.component.ComponentWithTabsAsNestedClasses;

public class DialogsTest extends DefaultTestBase {
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
}
