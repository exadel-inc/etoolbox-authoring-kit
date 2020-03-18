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

import com.exadel.aem.toolkit.core.exceptions.ValidationException;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.exadel.aem.toolkit.core.exceptions.InvalidTabException;
import com.exadel.aem.toolkit.test.component.ExceptionsTestCases;

public class ExceptionsTest extends ExceptionTestBase {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testComponentWithNonexistentTab() {
        exceptionRule.expectCause(IsInstanceOf.instanceOf(InvalidTabException.class));
        exceptionRule.expectMessage("Tab \"Zeroth tab\" is not defined");
        testComponent(ExceptionsTestCases.ComponentWithNonExistentTab.class);
    }


    @Test
    public void testComponentWithWrongDependsOnTab() {
        exceptionRule.expectCause(IsInstanceOf.instanceOf(InvalidTabException.class));
        exceptionRule.expectMessage("Tab \"Zeroth tab\" is not defined");
        testComponent(ExceptionsTestCases.ComponentWithNonExistentDependsOnTab.class);
    }

    @Test
    public void testComponentWithWrongHtmlTag1() {
        exceptionRule.expectCause(IsInstanceOf.instanceOf(ValidationException.class));
        exceptionRule.expectMessage("valid characters are: latin symbols, numbers, '.', '-'");
        testComponent(ExceptionsTestCases.ComponentWithWrongHtmlTag1.class);
    }

    @Test
    public void testComponentWithWrongHtmlTag2() {
        exceptionRule.expectCause(IsInstanceOf.instanceOf(ValidationException.class));
        exceptionRule.expectMessage("valid characters are: latin symbols, numbers, '.', '-'");
        testComponent(ExceptionsTestCases.ComponentWithWrongHtmlTag2.class);
    }
}
