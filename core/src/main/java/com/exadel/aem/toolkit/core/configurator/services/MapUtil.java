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
package com.exadel.aem.toolkit.core.configurator.services;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ClassUtils;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import com.exadel.aem.toolkit.core.utils.ValueMapUtil;

/**
 * Provides utility methods to work with {@link Map}, {@link ValueMap}, and {@link Dictionary} instances
 */
class MapUtil {

    /**
     * Converts a {@link ValueMap} instance to a {@link Dictionary} instance, excluding system properties and properties
     * with unsupported value types
     * @param properties The source value map
     * @return The resulting dictionary
     */
    public static Dictionary<String, ?> toDictionary(ValueMap properties) {
        Dictionary<String, Object> dictionary = new Hashtable<>();
        ValueMapUtil.excludeSystemProperties(properties)
            .entrySet()
            .stream()
            .filter(entry -> isValid(entry.getValue()))
            .forEach(entry -> dictionary.put(entry.getKey(), entry.getValue()));
        return dictionary;
    }

    /**
     * Converts a {@link Dictionary} instance to a {@link Map} instance, excluding system properties and properties
     * with unsupported value types
     * @param dictionary The source dictionary
     * @return The resulting map
     */
    public static Map<String, Object> toMap(Dictionary<String, ?> dictionary) {
        Map<String, Object> valueMap = new HashMap<>();
        if (dictionary == null) {
            return new ValueMapDecorator(new Hashtable<>());
        }
        for (Enumeration<String> keys = dictionary.keys(); keys.hasMoreElements(); ) {
            String key = keys.nextElement();
            if ("service.pid".equals(key) || "service.factoryPid".equals(key)) {
                continue;
            }
            Object value = dictionary.get(key);
            if (isValid(value)) {
                valueMap.put(key, value);
            }
        }
        return new ValueMapDecorator(valueMap);
    }

    /**
     * Checks if the specified attribute value is of a supported type
     * @param attribute The attribute value to check
     * @return True or false
     */
    private static boolean isValid(Object attribute) {
        if (attribute == null) {
            return false;
        }
        if (attribute.getClass().isArray()) {
            return isValidType(attribute.getClass().getComponentType());
        }
        if (attribute instanceof List) {
            return ((Collection<?>) attribute).stream().allMatch(e -> e != null && isValidType(e.getClass()));
        }
        return isValidType(attribute.getClass());
    }

    /**
     * Checks if the specified type is supported
     * @param type The type to check
     * @return True or false
     */
    private static boolean isValidType(Class<?> type) {
        return ClassUtils.isPrimitiveOrWrapper(type) || String.class.equals(type);
    }
}
