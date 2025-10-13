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
package com.exadel.aem.toolkit.core.configurator.servlets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.osgi.service.metatype.AttributeDefinition;
import org.osgi.service.metatype.ObjectClassDefinition;

/**
 * Represents a configuration definition, i.e., a set of configuration attributes united by the same PID together
 * with metadata usefult to build the {@code EToolbox Configurator} user experience
 * @see ConfigAttribute
 */
class ConfigDefinition {

    private String id;
    private ObjectClassDefinition ocd;
    private List<ConfigAttribute> attributes;
    private long changeCount;
    private boolean factory;
    private boolean factoryInstance;
    private boolean modified;
    private boolean published;

    /**
     * Default (instantiation-restricting) constructor
     */
    private ConfigDefinition() {
    }

    /**
     * Gets the configuration PID
     * @return The string value
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the displayable configuration name
     * @return The string value
     */
    public String getName() {
        return ocd.getName();
    }

    /**
     * Gets the configuration description
     * @return The string value
     */
    public String getDescription() {
        return ocd.getDescription();
    }

    /**
     * Gets the list of configuration attributes
     * @return The list of {@link ConfigAttribute} instances
     */
    public List<ConfigAttribute> getAttributes() {
        return attributes;
    }

    /**
     * Gets the number of times the configuration has been changed
     * @return The integer value
     */
    public long getChangeCount() {
        return changeCount;
    }

    /**
     * Determines whether the configuration is a factory configuration
     * @return True or false
     */
    public boolean isFactory() {
        return factory;
    }

    /**
     * Determines whether the configuration is an instance of a factory configuration
     * @return True or false
     */
    public boolean isFactoryInstance() {
        return factoryInstance;
    }

    /**
     * Determines whether the configuration has been modified by a user
     * @return True or false
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * Marks the configuration as user-modified or not
     * @param value True or false
     */
    public void setModified(boolean value) {
        this.modified = value;
    }

    /**
     * Determines whether the configuration has been replicated to publisher instance(-s)
     * @return True or false
     */
    public boolean isPublished() {
        return published;
    }

    /**
     * Marks the configuration as published or not
     * @param value True or false
     */
    public void setPublished(boolean value) {
        this.published = value;
    }

    /* -------------
       Factory logic
       ------------- */

    /**
     * Prepares a new instance of the {@link Builder} class to create a {@code ConfigDefinition} object
     * @return The {@code Builder} instance
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * Implements the builder pattern for {@link ConfigDefinition} instantiation
     */
    static class Builder {
        private List<ConfigAttribute> attributes;
        private long changeCount;
        private boolean factory;
        private boolean factoryInstance;
        private String id;
        private ObjectClassDefinition ocd;

        /**
         * Assigns the number of times the configuration has been changed
         * @param value The integer value
         * @return This instance
         */
        Builder changeCount(long value) {
            this.changeCount = value;
            return this;
        }

        /**
         * Assigns the configuration PID
         * @param value The string value
         * @return This instance
         */
        Builder id(String value) {
            this.id = value;
            return this;
        }

        /**
         * Assigns the value that determines whether the configuration is a factory configuration
         * @param value True or false
         * @return This instance
         */
        Builder isFactory(boolean value) {
            this.factory = value;
            return this;
        }

        /**
         * Assigns the value that determines whether the configuration is an instance of a factory configuration
         * @param value True or false
         * @return This instance
         */
        Builder isFactoryInstance(boolean value) {
            this.factoryInstance = value;
            return this;
        }

        /**
         * Assigns the {@link ObjectClassDefinition} instance that provides metadata for the configuration
         * @param value The {@code ObjectClassDefinition} instance
         * @return This instance
         */
        Builder ocd(ObjectClassDefinition value) {
            this.ocd = value;
            return this;
        }

        /**
         * Assigns the map of configuration attribute IDs and their values
         * @param value The map of values
         * @return This instance
         */
        Builder values(Map<String, Object> value) {
            this.attributes = new ArrayList<>();
            for (AttributeDefinition definition : ocd.getAttributeDefinitions(ObjectClassDefinition.ALL)) {
                attributes.add(
                    new ConfigAttribute(
                        definition,
                        value != null ? value.get(definition.getID()) : null)
                );
            }
            return this;
        }

        /**
         * Creates a {@link ConfigDefinition} instance using the values previously assigned to this builder
         * @return The {@code ConfigDefinition} instance
         */
        ConfigDefinition build() {
            ConfigDefinition instance = new ConfigDefinition();
            instance.attributes = this.attributes != null ? this.attributes : new ArrayList<>();
            instance.changeCount = this.changeCount;
            instance.factory = this.factory;
            instance.factoryInstance = this.factoryInstance;
            instance.id = this.id;
            instance.ocd = this.ocd;
            return instance;
        }
    }
}
