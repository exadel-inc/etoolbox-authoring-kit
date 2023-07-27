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
package com.exadel.aem.toolkit.plugin.metadata;

/**
 * Represents a single element of a property path
 * @see PropertyPath
 */
class PropertyPathElement {
    private final String name;
    private final int index;

    /**
     * Initializes a class instance with the specified name and index
     * @param name  A string representing the element name; a non-null value is expected
     * @param index An integer representing the element index (e.g., an item inside a property array). If a negative
     *              value is passed, the element is considered to be having no index
     */
    PropertyPathElement(String name, int index) {
        this.name = name;
        this.index = index;
    }

    /**
     * Retrieves the element name
     * @return String value
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves the element index (e.g., to signify an item inside a property array). The value is generally similar to
     * the {@code [number]} part in an {@code XPath} expression. If a negative value is specified, the element is
     * considered to be having no index
     * @return Integer value
     */
    public int getIndex() {
        return index;
    }

    /**
     * Determines whether the element has an actual index
     * @return True or false
     */
    public boolean hasIndex() {
        return index >= 0;
    }
}
