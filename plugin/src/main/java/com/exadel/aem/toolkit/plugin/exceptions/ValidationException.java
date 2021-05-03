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

import org.apache.commons.lang3.ArrayUtils;

/**
 * Represents the plugin-specific exception produced when the validation of a ToolKit annotation value fails
 */
public class ValidationException extends RuntimeException {

    /**
     * Initializes a class instance with an exception message template and an arbitrary number of arguments specified
     * @param value String value representing the message template, non-blank
     * @param args Optional arguments that can be rendered in the template
     */
    public ValidationException(String value, Object... args) {
        super(ArrayUtils.isEmpty(args) ? value : String.format(value, args));
    }
}
