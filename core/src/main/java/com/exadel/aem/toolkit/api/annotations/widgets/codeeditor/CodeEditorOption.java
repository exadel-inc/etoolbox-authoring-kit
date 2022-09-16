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
package com.exadel.aem.toolkit.api.annotations.widgets.codeeditor;

/**
 * Represents a configuration option passed to a {@code CodeEditor} upon initialization
 */
public @interface CodeEditorOption {

    /**
     * Defines the name of the option
     * @return String value, non-blank
     */
    String name();

    /**
     * Defines the value of the option
     * @return String value
     */
    String value();

    /**
     * Defines the type of the option that will be used with the initialization script. Expected values are {@code
     * String} (default), {@code Boolean}, and {@code Integer}
     * @return Class reference
     */
    Class<?> type() default String.class;
}
