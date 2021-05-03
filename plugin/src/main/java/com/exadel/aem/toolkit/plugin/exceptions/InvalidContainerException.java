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
 * Represents the plugin-specific exception thrown when there is no possibility to operate a particular container section
 * (such as a Tab or an AccordionPanel)
 */
public class InvalidContainerException extends RuntimeException {
    private static final String SECTIONS_NOT_DEFINED_MESSAGE = "No container sections defined for the dialog";
    private static final String SECTION_NOT_DEFINED_MESSAGE_TEMPLATE = "Container section \"%s\" is not defined";

    /**
     * Initializes a class instance with the default exception message
     */
    public InvalidContainerException() {
        super(SECTIONS_NOT_DEFINED_MESSAGE);
    }

    /**
     * Initializes a class instance with the provided container title
     * @param title String value, non-blank
     */
    public InvalidContainerException(String title) {
        super(String.format(SECTION_NOT_DEFINED_MESSAGE_TEMPLATE, title));
    }
}
