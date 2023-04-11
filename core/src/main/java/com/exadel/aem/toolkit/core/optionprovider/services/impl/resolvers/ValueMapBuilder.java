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
package com.exadel.aem.toolkit.core.optionprovider.services.impl.resolvers;

import java.util.HashMap;
import java.util.Map;

import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;

/**
 * Creates an instance of {@link ValueMap} via separate value assignments
 */
class ValueMapBuilder {

    private final Map<String, Object> propertyMap;

    /**
     * Default constructor
     */
    ValueMapBuilder() {
        propertyMap = new HashMap<>();
    }

    /**
     * Assigns a new key-value pair to the current builder
     * @param key   Arbitrary string value; a non-empty string is expected
     * @param value Arbitrary object
     * @return This builder
     */
    public ValueMapBuilder put(String key, Object value) {
        propertyMap.put(key, value);
        return this;
    }

    /**
     * Completes the builder
     * @return {@link ValueMap} instance containing the provided data
     */
    public ValueMap build() {
        return new ValueMapDecorator(propertyMap);
    }
}
