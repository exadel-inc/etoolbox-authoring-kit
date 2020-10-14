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

import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.core.handlers.container.TabsContainerHandler;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents an aggregate of {@link Tab} instance and a list of fields designed to be rendered within this tab.
 * Used to compose a sorted "tab registry" for a component class
 *
 * @see TabsContainerHandler#getTabInstances(List)
 */
public class TabInstance {
    private Tab tab;

    private List<Field> fields;

    /**
     * Creates a new {@code TabInstance} wrapped around a specified {@link Tab} with an empty list of associated fields
     *
     * @param tab {@code Tab} object
     */
    public TabInstance(Tab tab) {
        this.tab = tab;
        this.fields = new LinkedList<>();
    }

    /**
     * Creates a new {@code TabInstance} wrapped around a specified {@link Tab} with a particular list of associated
     * fields
     *
     * @param tab    {@code Tab} object
     * @param fields List of {@code Field} objects to associate with the current tab
     */
    public TabInstance(Tab tab, List<Field> fields) {
        this.tab = tab;
        this.fields = new LinkedList<>(fields);
    }

    /**
     * Gets the stored {@link Tab}
     *
     * @return {@code Tab} object
     */
    public Tab getTab() {
        return tab;
    }

    /**
     * Gets the stored list of {@link Field}s
     *
     * @return {@code List<Field>} object
     */
    public List<Field> getFields() {
        return fields;
    }

    /**
     * Merges a foreign {@code TabInstance} to the current instance, basically by adding other instance's {@code Field}s
     * while preserving the same {@code Tab} reference
     *
     * @param other Foreign {@code TabInstance} object
     * @return This instance
     */
    public TabInstance merge(TabInstance other) {
        this.fields.addAll(other.getFields());
        return this;
    }
}
