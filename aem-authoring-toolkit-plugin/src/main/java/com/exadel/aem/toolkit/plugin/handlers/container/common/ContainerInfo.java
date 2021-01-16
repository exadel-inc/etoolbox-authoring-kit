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
package com.exadel.aem.toolkit.plugin.handlers.container.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an aggregate of Tab or Accordion instance and a list of fields designed to be rendered within this container element.
 * Used to compose a sorted "container element registry" for a component class
 */
public class ContainerInfo {
    private final String title;
    private final Map<String, Object> attributes;
    private final Map<String, Object> fields;

    /**
     * Creates a new {@code ContainerInfo} wrapped around a specified  with an empty list of associated fields
     * @param containerElementTitle Container element name
     */
    ContainerInfo(String containerElementTitle) {
        this.title = containerElementTitle;
        this.fields = new HashMap<>();
        this.attributes = new HashMap<>();
    }

    /**
     * Get current container element name
     * @return container element name
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the stored list of fields
     * @return {@code Map<String, Object>} object
     */
    public Map<String, Object> getFields() {
        return fields;
    }

    void setField(String name, Object value) {
        this.fields.put(name, value);
    }

    /**
     * Merges a foreign {@code ContainerInfo} to the current instance, basically by adding other instance's fields
     * while preserving the same  reference
     * @param other Foreign {@code ContainerInfo} object
     * @return This instance
     */
    ContainerInfo merge(ContainerInfo other) {
        this.fields.putAll(other.getFields());
        return this;
    }
    /**
     * Get current container element attributes
     * @return attributes
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    /**
     * Set attributes for current container element
     */
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes.putAll(attributes);
    }
}
