/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
 * @deprecated This is deprecated and will be removed in a version after 2.0.1. Please use {@link Handler} instead
 */
@Deprecated
@SuppressWarnings({"unused", "squid:S1133"}) // See deprecation message
public interface DialogHandler extends Handler {

    /**
     * Identifies this DialogHandler for binding to a specific {@code DialogAnnotation}
     * @return String value, non-blank
     */
    default String getName() {
        return "";
    }

    default void accept(Source source, Target target) {
    }

    default void accept(Element element, Class<?> cls) {
    }
}
