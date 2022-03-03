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
package com.exadel.aem.toolkit.core.injectors.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.day.crx.JcrConstants;
import com.adobe.granite.ui.components.ds.ValueMapResource;

/**
 * Contains utility methods for creating instances ob objects
 * <p><u>Note</u>: This class is not a part of the public API</p>
 */
public class InstantiationUtil {

    private static final Logger LOG = LoggerFactory.getLogger(InstantiationUtil.class);

    /**
     * Default (instantiation-restricting) constructor
     */
    private InstantiationUtil() {
    }

    /**
     * Creates a new instance of the specified {@code Class}
     * @param type The class to instantiate
     * @param <T>  Instance type
     * @return New object instance, or null if the creation or initialization failed
     */
    public static <T> T getObjectInstance(Class<? extends T> type) {
        try {
            return type.getConstructor().newInstance();
        } catch (InstantiationException
            | IllegalAccessException
            | InvocationTargetException
            | NoSuchMethodException ex) {
            LOG.error("Could not initialize object {}", type.getName(), ex);
        }
        return null;
    }

    /**
     * Gets an existing resource or else creates a new {@code Resource} that contains properties from the given current
     * resource filtered with a predicate
     * @param current {@code Resource} object contains properties to be filtered
     * @param prefix  {@code String} representing an optional prefix the properties are checked against when filtering
     * @param postfix {@code String} representing an optional postfix the properties are checked against when filtering
     * @return {@code Resource} instance, or null if retrieval failed
     */
    public static Resource getFilteredResource(Resource current, String prefix, String postfix) {
        if (StringUtils.isEmpty(prefix) && StringUtils.isEmpty(postfix)) {
            return current;
        }
        Map<String, Object> values = current
            .getValueMap()
            .entrySet()
            .stream()
            .filter(entry -> isMatchByPrefixOrPostfix(entry.getKey(), prefix, postfix))
            .collect(Collectors.toMap(
                entry -> clearPrefixOrPostfix(entry.getKey(), prefix, postfix),
                Map.Entry::getValue));
        return new ValueMapResource(
            current.getResourceResolver(),
            current.getPath(),
            values.getOrDefault(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, JcrConstants.NT_UNSTRUCTURED).toString(),
            new ValueMapDecorator(values));
    }

    /**
     * Returns whether the given property name is matched by the provided prefix or postfix
     * @param property String value representing the property name
     * @param prefix   String value representing an optional prefix
     * @param postfix  String value representing an optional postfix
     * @return True or false
     */
    private static boolean isMatchByPrefixOrPostfix(String property, String prefix, String postfix) {
        if (StringUtils.isNotEmpty(prefix) && StringUtils.isNotEmpty(postfix)) {
            return StringUtils.startsWith(property, prefix) && StringUtils.endsWith(property, postfix);
        }
        if (StringUtils.isNotEmpty(prefix) && StringUtils.isEmpty(postfix)) {
            return StringUtils.startsWith(property, prefix);
        }
        if (StringUtils.isEmpty(prefix) && StringUtils.isNotEmpty(postfix)) {
            return StringUtils.endsWith(property, postfix);
        }
        return true;
    }

    /**
     * Removes the given prefix and/or postfix from the provided string if they are present
     * @param property String value representing the property name
     * @param prefix   String value representing an optional prefix
     * @param postfix  String value representing an optional postfix
     * @return String value
     */
    private static String clearPrefixOrPostfix(String property, String prefix, String postfix) {
        String result = property;
        if (StringUtils.isNotEmpty(prefix)) {
            result = StringUtils.removeStart(result, prefix);
        }
        if (StringUtils.isNotEmpty(postfix)) {
            result = StringUtils.removeEnd(result, postfix);
        }
        return result;
    }
}
