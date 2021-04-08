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

import org.hamcrest.CoreMatchers;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.matchers.ThrowableMessageMatcher;
import org.junit.rules.ExpectedException;

import com.exadel.aem.toolkit.plugin.exceptions.ValidationException;
import com.exadel.aem.toolkit.test.component.ValidationTestCases;

public class ValidationsTest extends ExceptionsTestBase {
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testNonBlankValidation() {
        exceptionRule.expectCause(IsInstanceOf.instanceOf(ValidationException.class));
        exceptionRule.expectMessage("' ' provided");
        test(ValidationTestCases.InvalidTitleDialog.class);
    }

    @Test
    public void testAllNotBlankValidation() {
        exceptionRule.expectCause(IsInstanceOf.instanceOf(ValidationException.class));
        exceptionRule.expectMessage("string properties must not be blank");
        test(ValidationTestCases.InvalidRteParaformatDialog.class);
    }

    @Test
    public void testNumberValidation() {
        exceptionRule.expectCause(IsInstanceOf.instanceOf(ValidationException.class));
        exceptionRule.expectMessage("'not-a-number' provided");
        test(ValidationTestCases.InvalidNumberFieldDialog.class);
    }

    @Test
    public void testNonNegativeNumberValidation() {
        exceptionRule.expectCause(IsInstanceOf.instanceOf(ValidationException.class));
        exceptionRule.expectMessage("'-99' provided");
        test(ValidationTestCases.InvalidImageUploadDialog.class);
    }

    @Test
    public void testPositiveNumberValidation() {
        exceptionRule.expectCause(IsInstanceOf.instanceOf(ValidationException.class));
        exceptionRule.expect(CoreMatchers.anyOf(
            ThrowableMessageMatcher.hasMessage(CoreMatchers.containsString("'0' provided")),
            ThrowableMessageMatcher.hasMessage(CoreMatchers.containsString("'-99' provided"))
        ));
        test(ValidationTestCases.InvalidTextAreaDialog.class);
    }

    @Test
    public void testCharactersValidation() {
        exceptionRule.expectCause(IsInstanceOf.instanceOf(ValidationException.class));
        exceptionRule.expectMessage("a character range (start < end) or entity definition must be set");
        test(ValidationTestCases.InvalidRteCharactersDialog.class);
    }

    @Test
    public void testJcrPathValidation() {
        exceptionRule.expectCause(IsInstanceOf.instanceOf(ValidationException.class));
        exceptionRule.expectMessage("'wrong path' provided");
        test(ValidationTestCases.InvalidPathDialog.class);
    }

    @Test
    public void testDateValidation() {
        exceptionRule.expectCause(IsInstanceOf.instanceOf(ValidationException.class));
        exceptionRule.expectMessage("valid date/time value expected");
        test(ValidationTestCases.InvalidDatePickerDialog.class);
    }

    @Test
    public void testCustomValidation() {
        exceptionRule.expectCause(IsInstanceOf.instanceOf(ValidationException.class));
        exceptionRule.expectMessage("one of 'red', 'green', or 'blue' must be provided");
        test(ValidationTestCases.InvalidCustomAnnotationDialog.class);
    }


    @Test
    public void testComponentWithWrongHtmlTag1() {
        exceptionRule.expectCause(IsInstanceOf.instanceOf(ValidationException.class));
        exceptionRule.expectMessage("'' provided, non-blank string expected");
        test(ValidationTestCases.ComponentWithWrongHtmlTag1.class);
    }

    @Test
    public void testComponentWithWrongHtmlTag2() {
        exceptionRule.expectCause(IsInstanceOf.instanceOf(ValidationException.class));
        exceptionRule.expectMessage("' ' provided, non-blank string expected");
        test(ValidationTestCases.ComponentWithWrongHtmlTag2.class);
    }

    @Test
    public void testDialogTitleMissing() {
        exceptionRule.expectCause(IsInstanceOf.instanceOf(ValidationException.class));
        exceptionRule.expectMessage("Title property is missing for dialog");
        test(ValidationTestCases.MissingTitleDialog.class);
    }
}
