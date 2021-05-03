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

import java.util.Optional;

/**
 * Presents an abstraction of data source for rendering Granite UI entities. A common variant of a data source is a Java
 * class, a class member, a particular annotation, or a specific data storage
 */
public interface Source {

    /**
     * Retrieves the name of the source
     * @return Non-blank string value
     */
    String getName();

    /**
     * Indicates whether this source is valid for rendering
     * @return Non-blank string value
     */
    boolean isValid();

    /**
     * Adapts the current {@code Source} instance to the provided type
     * @param adaptation {@code Class} reference indicating the desired data type
     * @param <T>        The type of the resulting value
     * @return The {@code T}-typed object or null in case the adaptation to the particular type was not possible or failed
     */
    <T> T adaptTo(Class<T> adaptation);

    /**
     * Tries to adapt the current {@code Source} instance to the provided type
     * @param adaptation {@code Class} reference indicating the desired data type
     * @param <T>        The type of the resulting value
     * @return An {@code Optional} containing the result of adaptation. In case the adaptation to the particular type
     * was not possible or failed, a non-null yet empty optional object is returned
     */
    default <T> Optional<T> tryAdaptTo(Class<T> adaptation) {
        return Optional.ofNullable(adaptTo(adaptation));
    }
}
