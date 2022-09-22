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
package com.exadel.aem.toolkit.plugin.validators;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.exadel.aem.toolkit.plugin.exceptions.ValidationException;
import com.exadel.aem.toolkit.plugin.maven.FileSystemRule;
import com.exadel.aem.toolkit.plugin.maven.PluginContextRenderingRule;
import com.exadel.aem.toolkit.plugin.maven.ThrowsPluginException;
import com.exadel.aem.toolkit.plugin.validators.cases.ValidatorTestCases;

@ThrowsPluginException
public class ValidatorsTest {

    @ClassRule
    public static FileSystemRule fileSystemHost = new FileSystemRule();

    @Rule
    public PluginContextRenderingRule pluginContext = new PluginContextRenderingRule(fileSystemHost.getFileSystem());

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testNonBlankValidation() {
        pluginContext.testThrows(
            ValidatorTestCases.InvalidTitleDialog.class,
            ValidationException.class,
            "' ' provided");
    }

    @Test
    public void testAllNotBlankValidation() {
        pluginContext.testThrows(
            ValidatorTestCases.InvalidRteParaformatDialog.class,
            ValidationException.class,
            "string properties must not be blank");
    }

    @Test
    public void testNumberValidation() {
        pluginContext.testThrows(
            ValidatorTestCases.InvalidNumberFieldDialog.class,
            ValidationException.class,
            "'not-a-number' provided");
    }

    @Test
    public void testNonNegativeNumberValidation() {
        pluginContext.testThrows(
            ValidatorTestCases.InvalidImageUploadDialog.class,
            ValidationException.class,
            "'-99' provided");
    }

    @Test
    public void testPositiveNumberValidation() {
        pluginContext.testThrows(
            ValidatorTestCases.InvalidTextAreaDialog.class,
            ValidationException.class,
            "'-99' provided");
    }

    @Test
    public void testCharactersValidation() {
        pluginContext.testThrows(
            ValidatorTestCases.InvalidRteCharactersDialog.class,
            ValidationException.class,
            "a character range (start < end) or entity definition must be set");
    }

    @Test
    public void testJcrPathValidation() {
        pluginContext.testThrows(
            ValidatorTestCases.InvalidPathDialog.class,
            ValidationException.class,
            "'wrong path' provided");
    }

    @Test
    public void testDateValidation() {
        pluginContext.testThrows(
            ValidatorTestCases.InvalidDatePickerDialog.class,
            ValidationException.class,
            "valid date/time value expected");
    }

    @Test
    public void testCustomValidation() {
        pluginContext.testThrows(
            ValidatorTestCases.InvalidCustomAnnotationDialog.class,
            ValidationException.class,
            "one of 'red', 'green', or 'blue' must be provided");
    }


    @Test
    public void testComponentWithWrongHtmlTag1() {
        pluginContext.testThrows(
            ValidatorTestCases.ComponentWithWrongHtmlTag1.class,
            ValidationException.class,
            "'' provided, non-blank string expected");
    }

    @Test
    public void testComponentWithWrongHtmlTag2() {
        pluginContext.testThrows(
            ValidatorTestCases.ComponentWithWrongHtmlTag2.class,
            ValidationException.class,
            "' ' provided, non-blank string expected");
    }

    @Test
    public void testDialogTitleMissing() {
        pluginContext.testThrows(
            ValidatorTestCases.MissingTitleDialog.class,
            ValidationException.class,
            "Title property is missing for dialog");
    }
}
