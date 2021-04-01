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
package com.exadel.aem.toolkit.api.handlers;

import org.w3c.dom.Element;

/**
 * Represents an abstraction of logic for handling a dialog annotation.
 * Serves as the marker interface for creating and enumerating handlers instances
 * @deprecated This is deprecated and will be removed in a version after 2.0.2. Please use {@link Handler} instead
 */
@Deprecated
@SuppressWarnings("squid:S1133") // See deprecation message
public interface DialogHandler extends Handler {

    /**
     * Identifies this DialogHandler for binding to a specific {@code DialogAnnotation}
     * @return String value, non-blank
     */
    default String getName() {
        return "";
    }

    /**
     * Method representing the entry-point for handling data, modern style
     * @param source Non-null {@code Source} object
     * @param target Non-null {@code Target} object
     */
    default void accept(Source source, Target target) {
    }

    /**
     * Method representing the entry-point for handling data, legacy style
     * @param element Non-null DOM {@code element} object
     * @param cls     Non-null {@code Class} reference
     */
    default void accept(Element element, Class<?> cls) {
    }
}
