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
package com.exadel.aem.toolkit.api.runtime;

/**
 * Implements handling exceptions thrown in the scope of a ToolKit routine. This is done for such purposes
 * as unified logging, or making a decision whether Maven plugin execution should continue or stop
 */
public interface ExceptionHandler {

    /**
     * Handles an exception
     * @param e {@code Exception} instance
     */
    default void handle(Exception e) {
        handle(e.getMessage(), e);
    }

    /**
     * Handles an exception with a supplementary message
     * @param message {@code String} value
     * @param cause   {@code Exception} instance
     */
    void handle(String message, Exception cause);

    /**
     * Gets whether an exception of the specified class would cause the AEM Authoring Toolkit's Maven plugin to terminate.
     * Should the user choose to skip this particular exception or all the exceptions in the plugin's configuration,
     * this function must return false, otherwise it returns true
     * @param exceptionType Class of {@link Exception} to test on
     * @return True or false
     */
    boolean shouldTerminateOn(Class<? extends Exception> exceptionType);
}
