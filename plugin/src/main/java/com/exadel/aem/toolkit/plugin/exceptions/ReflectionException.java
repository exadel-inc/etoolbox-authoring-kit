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
package com.exadel.aem.toolkit.plugin.exceptions;

/**
 * Represents the plugin-specific exception due to one of exceptions produced by {@code java.lang.reflect} routines
 */
public class ReflectionException extends RuntimeException {

    /**
     * Initializes a class instance with an exception message and cause specified
     * @param value String value, non-blank
     * @param cause Non-null {@code Exception} object
     */
    public ReflectionException(String value, Exception cause) {
        super(value, cause);
    }

    /**
     * Initializes a class instance with references to a failed class and member
     * @param reflected Non-null {@code Class} reference
     * @param member String value, non-blank
     */
    public ReflectionException(Class<?> reflected, String member) {
        super(String.format("Member '%s' not present in %s", member, reflected));
    }
}
