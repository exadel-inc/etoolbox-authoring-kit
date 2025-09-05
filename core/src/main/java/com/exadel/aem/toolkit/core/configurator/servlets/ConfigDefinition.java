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

    private final String id;
    private final ObjectClassDefinition ocd;
    private final List<ConfigAttribute> attributes;
    private final boolean factory;
    private final boolean factoryInstance;
    private boolean modified;
    private boolean published;

    /**
     * Instantiates a new object
     * @param id                The configuration PID
     * @param ocd               The {@link ObjectClassDefinition} instance that contains configuration attribute
     *                          definitions
     * @param values            A map of configuration attribute values, where the key is attribute ID
     * @param isFactory         Whether the configuration is a factory configuration
     * @param isFactoryInstance Whether the configuration is an instance of a factory configuration
     */
    ConfigDefinition(
        String id,
        ObjectClassDefinition ocd,
        Map<String, Object> values,
        boolean isFactory,
        boolean isFactoryInstance) {

        this.id = id;
        this.ocd = ocd;
        this.attributes = new ArrayList<>();
        for (AttributeDefinition definition : ocd.getAttributeDefinitions(ObjectClassDefinition.ALL)) {
            attributes.add(
                new ConfigAttribute(
                    definition,
                    values != null ? values.get(definition.getID()) : null)
            );
        }
        this.factory = isFactory;
        this.factoryInstance = isFactoryInstance;
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
}
