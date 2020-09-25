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

import java.util.function.BiConsumer;

import org.w3c.dom.Element;

/**
 * Abstraction of handler class to process logic of AEM components and/or AEM TouchUI dialog.
 * Serves as the marker interface for creating and enumerating handlers instances
 */
@SuppressWarnings("unused")
public interface DialogHandler extends BiConsumer<Element, Class<?>> {
    /**
     * Identifies this DialogHandler for binding to a specific {@code DialogAnnotation}
     * @return String value, non-blank
     */
    String getName();

    String before();

    String after();
}
