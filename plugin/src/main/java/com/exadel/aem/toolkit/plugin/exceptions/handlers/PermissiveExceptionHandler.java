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

import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.api.runtime.ExceptionHandler;

/**
 * Implements the "permissive" kind of {@link com.exadel.aem.toolkit.api.runtime.ExceptionHandler}, that is, the one
 * that never throws {@link com.exadel.aem.toolkit.plugin.exceptions.PluginException}s and therefore doesn't terminate
 * a Maven workflow
 */
class PermissiveExceptionHandler implements ExceptionHandler {
    static final Logger LOG = LoggerFactory.getLogger("EToolbox Authoring Kit");

    /**
     * Logs the handled exception. Checked exceptions are logged as error messages, and unchecked exceptions are logged
     * as warnings
     * @param message Attached exception message
     * @param cause   Base exception
     */
    @Override
    public void handle(String message, Exception cause) {
        if (ClassUtils.isAssignable(cause.getClass(), RuntimeException.class)) {
            LOG.warn(message, cause);
        } else {
            LOG.error(message, cause);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean shouldTerminateOn(Class<? extends Exception> exceptionType) {
        return false;
    }
}
