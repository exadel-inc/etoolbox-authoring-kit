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
package com.exadel.aem.toolkit.core.configurator.models.internal;

import org.apache.commons.lang3.ArrayUtils;
import org.osgi.service.metatype.AttributeDefinition;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;

/**
 * Represents a configuration attribute, i.e., a pair of {@link AttributeDefinition} and its value
 * <p><b>Note</b>: This class is not a part of the public API and is subject to change. Do not use it in your own code
 * @see ConfigDefinition
 */
public class ConfigAttribute {
    private final AttributeDefinition definition;
    private final Object value;

    /**
     * Instantiates a new object
     * @param definition The attribute definition
     * @param value      The attribute value
     */
    public ConfigAttribute(AttributeDefinition definition, Object value) {
        this.definition = definition;
        this.value = value;
    }

    /**
     * Gets the attribute definition
     * @return The {@link AttributeDefinition} instance
     */
    public AttributeDefinition getDefinition() {
        return definition;
    }

    /**
     * Determines the resource type that corresponds to the attribute definition
     * @return A string value that can be assigned to {@code sling:resourceType} property of a dialog field
     */
    public String getResourceType() {
        if (ArrayUtils.getLength(definition.getOptionValues()) > 0) {
            return ResourceTypes.SELECT;
        }
        int type = definition.getType();
        if (isIntegerType(type)) {
            return ResourceTypes.NUMBERFIELD;
        }
        if (type == AttributeDefinition.BOOLEAN) {
            return ResourceTypes.CHECKBOX;
        }
        if (type == AttributeDefinition.PASSWORD) {
            return ResourceTypes.PASSFIELD;
        }
        return ResourceTypes.TEXTFIELD;
    }

    /**
     * Determines the JCR property type that corresponds to the attribute definition
     * @return A string value that represents an Oak/JCR property type
     */
    public String getJcrType() {
        String result = "String";
        int type = definition.getType();
        if (isIntegerType(type)) {
            result = "Long";
        } else if (isFloatingPointType(type)) {
            result = "Double";
        } else if (type == AttributeDefinition.BOOLEAN) {
            result = "Boolean";
        }
        if (isMultiValue()) {
            result += "[]";
        }
        return result;
    }

    /**
     * Gets the attribute value
     * @return A nullable object
     */
    public Object getValue() {
        return value;
    }

    /**
     * Determines whether the attribute is multivalued
     * @return True or false
     */
    public boolean isMultiValue() {
        return definition.getCardinality() != 0;
    }

    /**
     * Determines whether the attribute type is a floating point
     * @param type The attribute type
     * @return True or false
     */
    private static boolean isFloatingPointType(int type) {
        return type == AttributeDefinition.DOUBLE || type == AttributeDefinition.FLOAT;
    }

    /**
     * Determines whether the attribute type is integer
     * @param type The attribute type
     * @return True or false
     */
    private static boolean isIntegerType(int type) {
        return type == AttributeDefinition.BYTE
            || type == AttributeDefinition.INTEGER
            || type == AttributeDefinition.LONG
            || type == AttributeDefinition.SHORT;
    }
}
