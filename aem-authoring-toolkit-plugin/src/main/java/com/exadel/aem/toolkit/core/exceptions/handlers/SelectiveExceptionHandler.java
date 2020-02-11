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

package com.exadel.aem.toolkit.core.exceptions.handlers;

import java.util.List;

import com.exadel.aem.toolkit.core.exceptions.PluginException;

/**
 * Implements the "selective" kind of {@link com.exadel.aem.toolkit.api.runtime.ExceptionHandler}, that is, the one
 * that throws {@link com.exadel.aem.toolkit.core.exceptions.PluginException}s and so terminates Maven workflow
 * in case one of the specific internal exceptions (listed ln constructor's on;ly argument) is thrown, and otherwise
 * falls back to {@code PermissiveExceptionHandler} behavior
 */
class SelectiveExceptionHandler extends PermissiveExceptionHandler {
    private List<String> criticalExceptions;

    SelectiveExceptionHandler(List<String> criticalExceptions) {
        this.criticalExceptions = criticalExceptions;
    }

    /**
     * Handles exception with additional checking whether to terminate plugin execution
     * (by re-throwing more generic {@code PluginException}) or not
     * @param message Attached exception message
     * @param cause   Base exception
     */
    @Override
    public void handle(String message, Exception cause) {
        if (haltsOn(cause.getClass())) {
            throw new PluginException(cause);
        }
        super.handle(message, cause);
    }

    @Override
    public boolean haltsOn(Class<? extends Exception> exceptionType) {
        return criticalExceptions.stream().anyMatch(exName -> exceptionType.getName().equalsIgnoreCase(exName));
    }
}
