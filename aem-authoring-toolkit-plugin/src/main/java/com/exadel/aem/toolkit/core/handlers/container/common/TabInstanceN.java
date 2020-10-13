package com.exadel.aem.toolkit.core.handlers.container.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an aggregate of Tab or Accordion instance and a list of fields designed to be rendered within this tab.
 * Used to compose a sorted "tab registry" for a component class
 */
public class TabInstanceN {
    private String title;
    private Map<String, Object> attributes = new HashMap<>();
    private Map<String, Object> fields;

    /**
     * Creates a new {@code TabInstanceN} wrapped around a specified  with an empty list of associated fields
     *
     * @param tab Tab name
     */
    public TabInstanceN(String tab) {
        this.title = tab;
        this.fields = new HashMap<>();
    }

    /**
     * Creates a new {@code TabInstanceN} wrapped around a specified  with a particular list of associated
     * fields
     *
     * @param tab    tab name
     * @param fields List of objects (fields) to associate with the current tab, stored in map
     */
    public TabInstanceN(String tab, Map<String, Object> fields) {
        this.title = tab;
        this.fields = new HashMap<>(fields);
    }

    /**
     * Get current tab name
     *
     * @return tab name
     */
    public String getTab() {
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
     * Merges a foreign {@code TabInstanceN} to the current instance, basically by adding other instance's fields
     * while preserving the same  reference
     *
     * @param other Foreign {@code TabInstanceN} object
     * @return This instance
     */
    public TabInstanceN merge(TabInstanceN other) {
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
