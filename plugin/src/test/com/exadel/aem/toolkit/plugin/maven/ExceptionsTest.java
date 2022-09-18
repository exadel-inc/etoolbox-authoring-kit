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

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import com.google.common.collect.ImmutableMap;

import com.exadel.aem.toolkit.plugin.exceptions.ValidationException;
import com.exadel.aem.toolkit.plugin.exceptions.handlers.ExceptionHandlers;

public class ExceptionsTest {
    private static final String NOT_AN_EXCEPTION_MESSAGE = "NOT AN EXCEPTION: testing terminateOn logic";
    private static final String SHOULD_TERMINATE_TEMPLATE = "Setting \"%s\" should have caused termination with %s";

    @Test
    public void testTerminateOnSettings() {
        // Test non-terminating cases
        Map<String, Exception> nonTerminatingCases = ImmutableMap.of(
                "!ValidationException, java.lang.RuntimeException", new ValidationException(NOT_AN_EXCEPTION_MESSAGE),
                "!java.lang.IndexOutOfBoundsException, *", new IndexOutOfBoundsException(NOT_AN_EXCEPTION_MESSAGE),
                "!java.io.IOException, !java.lang.RuntimeException, !com.exadel.aem.plugin.exceptions.*", new ValidationException(NOT_AN_EXCEPTION_MESSAGE),
                "!java.lang.IndexOutOfBoundsException, !*", new RuntimeException(NOT_AN_EXCEPTION_MESSAGE) // must neglect the "!*" sign
        );
        nonTerminatingCases.forEach((setting, exception) -> ExceptionHandlers.forSetting(setting).handle(exception));

        // Test terminating cases
        Map<String, Exception> terminatingCases = ImmutableMap.of(
            "ValidationException, !java.lang.RuntimeException", new ValidationException(StringUtils.EMPTY),
                "java.lang.RuntimeException", new IndexOutOfBoundsException(),
                "java.io.IOException, !java.lang.Exception, *", new IOException(),
                "!java.lang.IndexOutOfBoundsException, java.lang.RuntimeException", new NullPointerException(),
                "java.lang.Exception, !java.lang.Exception, *", new RuntimeException()
        );
        for (Map.Entry<String, Exception> terminatingCase : terminatingCases.entrySet()) {
            try {
                ExceptionHandlers.forSetting(terminatingCase.getKey()).handle(terminatingCase.getValue());
                Assert.fail(String.format(SHOULD_TERMINATE_TEMPLATE, terminatingCase.getKey(), terminatingCase.getValue().getClass().getName()));
            } catch (Exception ignored) {
                // As the exception is legitimately caught, the test should proceed
            }
        }
    }
}
