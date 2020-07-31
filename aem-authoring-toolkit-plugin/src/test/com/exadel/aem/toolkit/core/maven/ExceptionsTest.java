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

import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.exadel.aem.toolkit.core.exceptions.InvalidFieldContainerException;
import com.exadel.aem.toolkit.core.exceptions.InvalidTabException;
import com.exadel.aem.toolkit.test.component.ExceptionsTestCases;
import com.exadel.aem.toolkit.test.component.InheritanceTestCases;

public class ExceptionsTest extends ExceptionsTestBase {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testComponentWithNonexistentTab() {
        exceptionRule.expectCause(IsInstanceOf.instanceOf(InvalidTabException.class));
        exceptionRule.expectMessage("Tab \"Zeroth tab\" is not defined");
        test(ExceptionsTestCases.ComponentWithNonexistentTab.class);
    }

    @Test
    public void testComponentWithWrongDependsOnTab() {
        exceptionRule.expectCause(IsInstanceOf.instanceOf(InvalidTabException.class));
        exceptionRule.expectMessage("Tab \"Zeroth tab\" is not defined");
        test(ExceptionsTestCases.ComponentWithNonexistentDependsOnTab.class);
    }

    @Test
    public void testComponentWithDuplicateFields() {
        exceptionRule.expectCause(IsInstanceOf.instanceOf(InvalidFieldContainerException.class));
        exceptionRule.expectMessage("Field name \"text2\" in class \"DuplicateOverride\"");
        test(InheritanceTestCases.DuplicateOverride.class);
    }
}
