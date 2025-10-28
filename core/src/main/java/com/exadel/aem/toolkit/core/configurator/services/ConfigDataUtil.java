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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.osgi.service.cm.Configuration;

import com.exadel.aem.toolkit.core.configurator.ConfiguratorConstants;
import com.exadel.aem.toolkit.core.utils.ValueMapUtil;

/**
 * Provides utility methods to work with {@link Configuration} instances and their properties
 */
class ConfigDataUtil {

    private static final List<String> EXCLUDED_KEYS = Arrays.asList(
        "service.pid",
        "service.factoryPid",
        ConfiguratorConstants.ATTR_NAME_HINT);

    /**
     * Default (instantiation-restricting) constructor
     */
    private ConfigDataUtil() {
    }

    /**
     * Extracts backup values from the specified {@link Configuration} instance
     * @param value The source configuration
     * @return The non-null dictionary of values
     */
    public static Dictionary<String, Object> getBackup(Configuration value) {
        return getData(
            value,
            key -> key.endsWith(ConfiguratorConstants.SUFFIX_BACKUP),
            key -> ConfiguratorConstants.SUFFIX_BACKUP.equals(key) ? key : StringUtils.removeEnd(key, ConfiguratorConstants.SUFFIX_BACKUP));
    }

    /**
     * Extracts the valid (non-backup and non-embargoed) values from the specified {@link Configuration} instance
     * @param value The source configuration
     * @return The non-null dictionary of values
     */
    public static Dictionary<String, Object> getData(Configuration value) {
        return getData(
            value,
            key -> !EXCLUDED_KEYS.contains(key)
                && !key.startsWith(ConfiguratorConstants.ATTR_CONFIGURATOR)
                && !key.endsWith(ConfiguratorConstants.SUFFIX_BACKUP),
            UnaryOperator.identity());
    }

    /**
     * Extracts values from the specified {@link Configuration} instance according to the provided filter and modifier
     * @param value    The source configuration
     * @param filter   A predicate to filter out unwanted keys
     * @param modifier A function to modify the keys that pass the filter
     * @return Dictionary of values
     */
    private static Dictionary<String, Object> getData(
        Configuration value,
        Predicate<String> filter,
        UnaryOperator<String> modifier) {

        Dictionary<String, Object> result = new Hashtable<>();
        Dictionary<String, Object> properties = value != null ? value.getProperties() : null;
        if (value == null || properties == null) {
            return result;
        }
        Enumeration<String> keys = properties.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (!filter.test(key)) {
                continue;
            }
            result.put(modifier.apply(key), properties.get(key));
        }
        return result;
    }

    /**
     * Converts a {@link ValueMap} instance to a {@link Dictionary} instance, excluding system properties and properties
     * with unsupported value types
     * @param properties The source value map
     * @return The resulting dictionary
     */
    public static Dictionary<String, Object> toDictionary(ValueMap properties) {
        Dictionary<String, Object> dictionary = new Hashtable<>();
        ValueMapUtil.excludeSystemProperties(properties)
            .entrySet()
            .stream()
            .filter(entry -> isValid(entry.getValue()))
            .forEach(entry -> dictionary.put(
                entry.getKey(),
                !ConfiguratorConstants.VALUE_EMPTY.equals(entry.getValue()) ? entry.getValue() : StringUtils.EMPTY));
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
        for (Enumeration<String> keys = dictionary.keys(); keys.hasMoreElements();) {
            String key = keys.nextElement();
            if (EXCLUDED_KEYS.contains(key) || key.endsWith(ConfiguratorConstants.SUFFIX_BACKUP)) {
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
     * Checks if the provided {@link Dictionary} and {@link Map} instances are equal in terms of keys and values
     * @param dictionary The dictionary (usually a collection of values derived from an OSGi configuration)
     * @param map        The map (usually a collection of values derived from a JCR resource)
     * @return True or false
     */
    public static boolean equals(Dictionary<String, ?> dictionary, Map<String, Object> map) {
        if (dictionary == null && map == null) {
            return true;
        }
        if (dictionary == null || map == null) {
            return false;
        }
        Map<String, Object> dictAsMap = toMap(dictionary);
        map = ValueMapUtil.excludeSystemProperties(map);
        if (dictAsMap.size() != MapUtils.size(map)) {
            return false;
        }
        for (String key : dictAsMap.keySet()) {
            Object dictValue = dictAsMap.get(key);
            Object mapValue = map.get(key);
            if (dictValue == null && mapValue == null) {
                continue;
            }
            if (dictValue == null || mapValue == null) {
                return false;
            }
            if (isCollection(dictValue) && isCollection(mapValue)) {
                if (!collectionsEqual(dictValue, mapValue)) {
                    return false;
                }
            } else if (isCollection(dictValue) || isCollection(mapValue)) {
                return false;
            } else if (ClassUtils.isPrimitiveOrWrapper(dictValue.getClass())
                && ClassUtils.isPrimitiveOrWrapper(mapValue.getClass())) {
                return Objects.equals(
                    ClassUtils.primitiveToWrapper(dictValue.getClass()).cast(dictValue),
                    ClassUtils.primitiveToWrapper(mapValue.getClass()).cast(mapValue)
                );
            } else if (!Objects.equals(dictValue, mapValue)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Whether the specified objects that adhere to {@link ConfigDataUtil#isCollection(Object)}{@code == true} are equal
     * @param first  The first collection object
     * @param second The second collection object
     * @return True or false
     */
    private static boolean collectionsEqual(Object first, Object second) {
        Object firstArray = first.getClass().isArray() ? first : ((List<?>) first).toArray();
        Object secondArray = second.getClass().isArray() ? second : ((List<?>) second).toArray();
        if (Array.getLength(firstArray) != Array.getLength(secondArray)) {
            return false;
        }
        for (int i = 0; i < Array.getLength(firstArray); i++) {
            Object firstValue = Array.get(firstArray, i);
            Object secondValue = Array.get(secondArray, i);
            if (firstValue == null && secondValue == null) {
                continue;
            }
            if (firstValue == null || secondValue == null) {
                return false;
            }
            if (ClassUtils.isPrimitiveOrWrapper(firstValue.getClass())
                && ClassUtils.isPrimitiveOrWrapper(secondValue.getClass())
                && !Objects.equals(
                    ClassUtils.primitiveToWrapper(firstValue.getClass()).cast(firstValue),
                    ClassUtils.primitiveToWrapper(secondValue.getClass()).cast(secondValue)
                )) {
                return false;
            }
            if (!Objects.equals(firstValue, secondValue)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determines whether the specified value is a {@link List} or an array)
     * @param value The value to check
     * @return True or false
     */
    private static boolean isCollection(Object value) {
        return value instanceof List || value.getClass().isArray();
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
