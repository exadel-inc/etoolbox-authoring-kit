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
import com.exadel.aem.toolkit.core.exceptions.handlers.PluginExceptionHandlers;
import com.google.common.collect.ImmutableMap;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.exadel.aem.toolkit.core.exceptions.InvalidFieldContainerException;
import com.exadel.aem.toolkit.core.exceptions.InvalidTabException;
import com.exadel.aem.toolkit.test.component.ExceptionsTestCases;
import com.exadel.aem.toolkit.test.component.InheritanceTestCases;

import java.io.IOException;
import java.util.Map;

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

    public void testComponentWithDuplicateFields() {
        exceptionRule.expectCause(IsInstanceOf.instanceOf(InvalidFieldContainerException.class));
        exceptionRule.expectMessage("Field named \"text2\" in class \"DuplicateOverride\"");
        test(InheritanceTestCases.DuplicateOverride.class);
    }

    @Test
    public void testTerminateOnSettings() {
        Map<String, Exception> terminateOnCases = ImmutableMap.of(
                "java.lang.RuntimeException", new IndexOutOfBoundsException(),
                "java.lang.IOException, !java.lang.Exception, *", new IOException(),
                "!java.lang.IndexOutOfBoundsException, java.lang.RuntimeException", new NullPointerException()
        );
        terminateOnCases.forEach((setting, exception) -> {
            ExceptionsTest newTest = new ExceptionsTest(){
                @Override
                String getExceptionSetting() {
                    return setting;
                }
            };
            exceptionRule.expectCause(IsInstanceOf.instanceOf(exception.getClass()));
            PluginExceptionHandlers.getHandler(setting).handle(exception);
        });
    }

    @Test
    public void testNonTerminatingSettings() {
        Map<String, Exception> terminateOnCases = ImmutableMap.of(
                " ValidationException, !java.lang.RuntimeException", new ValidationException(""),
                "!java.lang.IndexOutOfBoundsException, *", new IndexOutOfBoundsException(),
                "!java.io.IOException, !java.lang.RuntimeException, !com.exadel.aem.plugin.exceptions.*", new InvalidTabException("")
        );
        terminateOnCases.forEach((setting, exception) -> {
            ExceptionsTest newTest = new ExceptionsTest(){
                @Override
                String getExceptionSetting() {
                    return setting;
                }
            };
            PluginExceptionHandlers.getHandler(setting).handle(exception);
        });
    }
}
