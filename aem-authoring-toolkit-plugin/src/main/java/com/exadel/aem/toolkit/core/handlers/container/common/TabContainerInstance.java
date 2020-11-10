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
package com.exadel.aem.toolkit.core.handlers.container.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an aggregate of Tab or Accordion instance and a list of fields designed to be rendered within this tab.
 * Used to compose a sorted "tab registry" for a component class
 */
public class TabContainerInstance {
    private String title;
    private Map<String, Object> attributes = new HashMap<>();
    private Map<String, Object> fields;

    /**
     * Creates a new {@code TabContainerInstance} wrapped around a specified  with an empty list of associated fields
     *
     * @param tab Tab name
     */
    public TabContainerInstance(String tab) {
        this.title = tab;
        this.fields = new HashMap<>();
    }

    /**
     * Creates a new {@code TabContainerInstance} wrapped around a specified  with a particular list of associated
     * fields
     *
     * @param tab    tab name
     * @param fields List of objects (fields) to associate with the current tab, stored in map
     */
    public TabContainerInstance(String tab, Map<String, Object> fields) {
        this.title = tab;
        this.fields = new HashMap<>(fields);
    }

    /**
     * Get current tab name
     *
     * @return tab name
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the stored list of fields
     *
     * @return {@code Map<String, Object>} object
     */
    public Map<String, Object> getFields() {
        return fields;
    }

    public void setFields(String name, Object value) {
        this.fields.put(name, value);
    }

    /**
     * Merges a foreign {@code TabContainerInstance} to the current instance, basically by adding other instance's fields
     * while preserving the same  reference
     *
     * @param other Foreign {@code TabContainerInstance} object
     * @return This instance
     */
    public TabContainerInstance merge(TabContainerInstance other) {
        this.fields.putAll(other.getFields());
        return this;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes.putAll(attributes);
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
