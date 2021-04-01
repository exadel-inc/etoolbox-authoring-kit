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
 * Represents the plugin-specific exception produced for a generic invalid setting of the ToolKit,
 * such as a reference to an unreachable component class, or an invalid resource type
 */
public class InvalidSettingException extends RuntimeException {

    /**
     * Initializes a class instance with an exception message specified
     * @param value String value, non-blank
     */
    public InvalidSettingException(String value) {
        super(value);
    }
}
