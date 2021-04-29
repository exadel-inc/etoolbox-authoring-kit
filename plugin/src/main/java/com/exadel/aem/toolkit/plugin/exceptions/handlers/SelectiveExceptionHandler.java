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
package com.exadel.aem.toolkit.plugin.exceptions.handlers;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.plugin.exceptions.PluginException;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;

/**
 * Implements the "selective" kind of {@link com.exadel.aem.toolkit.api.runtime.ExceptionHandler}, that is, the one
 * that throws {@link com.exadel.aem.toolkit.plugin.exceptions.PluginException}s and so terminates a Maven workflow
 * in case one of the specific internal exceptions (listed ln constructor's only argument) is thrown, or otherwise
 * falls back to {@code PermissiveExceptionHandler} behavior
 */
class SelectiveExceptionHandler extends PermissiveExceptionHandler {
    private static final String PACKAGE_POSTFIX = ".*";

    private final List<String> exceptionTokens;

    /**
     * Initializes an instance of {@code SelectiveExceptionHandler} with pre-defined exception qualifiers
     * @param exceptionTokens {@code List} of string values
     */
    SelectiveExceptionHandler(List<String> exceptionTokens) {
        this.exceptionTokens = exceptionTokens;
    }

    /**
     * Handles exception with additional checking whether to terminate plugin execution
     * (by re-throwing more generic {@code PluginException}) or not
     * @param message Attached exception message
     * @param cause   Base exception
     */
    @Override
    public void handle(String message, Exception cause) {
        if (shouldTerminateOn(cause.getClass())) {
            throw new PluginException(message, cause);
        }
        super.handle(message, cause);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean shouldTerminateOn(Class<? extends Exception> exceptionType) {
        for (String exceptionToken : exceptionTokens) {
            Optional<Boolean> result = isMatch(exceptionType,
                    StringUtils.strip(exceptionToken, DialogConstants.NEGATION),
                    exceptionToken.startsWith(DialogConstants.NEGATION));
            if (result.isPresent()) {
                return result.get();
            }
        }
        return false;
    }

    /**
     * Called from {@link SelectiveExceptionHandler#shouldTerminateOn(Class)} to decide whether the exception class provided
     * matches the exception token (string values defining an exception or a set of exceptions) and, if so, what verdict
     * ("terminate" or "pass") should be made
     * @param exceptionType  {@code Class<?>} reference pointing to the particular Exception
     * @param exceptionToken String representing the particular exception token
     * @param inverse        Boolean value saying whether the inversion sign is present in the match
     * @return {@code Optional} value saying the exception does match the token, if non-empty. In such case, the wrapped
     * boolean value represents the verdict
     */
    private Optional<Boolean> isMatch(Class<? extends Exception> exceptionType, String exceptionToken, boolean inverse) {
        if (StringUtils.equalsAnyIgnoreCase(exceptionToken, DialogConstants.VALUE_ALL, DialogConstants.WILDCARD)) {
            return !inverse ? Optional.of(true) : Optional.empty();
        }
        if (exceptionToken.endsWith(PACKAGE_POSTFIX)) {
            if (exceptionType.getName().startsWith(StringUtils.strip(exceptionToken, PACKAGE_POSTFIX))) {
                return Optional.of(!inverse);
            } else {
                return Optional.empty();
            }
        }
        // We allow "simple" exception names for the plugin's in-box exception classes
        if (exceptionType.getPackage().equals(PluginException.class.getPackage())
                && exceptionToken.equals(exceptionType.getSimpleName())) {
            return Optional.of(!inverse);
        }
        try {
            Class<?> managedClass = Class.forName(exceptionToken);
            if (ClassUtils.isAssignable(exceptionType, managedClass)) {
                return Optional.of(!inverse);
            }
        } catch (ClassNotFoundException exception) {
            LOG.warn("Could not recognize exception class: {}", exceptionToken);
        }
        return Optional.empty();
    }
}
