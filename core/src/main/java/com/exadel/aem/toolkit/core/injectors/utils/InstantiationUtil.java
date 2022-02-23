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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
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
     * Creates a new {@code Resource} that contains properties from the given current resource filtered with a
     * predicate
     * @param current    {@code Resource} object contains properties to be filtered
     * @param predicates {@code List} of predicates used to filter properties
     * @return New {@code Resource} instance, or null if creation failed
     */
    public static Resource createFilteredResource(Resource current, Predicate<String> predicates) {
        Map<String, Object> values = current
            .getValueMap()
            .entrySet()
            .stream()
            .filter(item -> predicates.test(item.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new ValueMapResource(
            current.getResourceResolver(),
            StringUtils.EMPTY,
            values.getOrDefault(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, JcrConstants.NT_UNSTRUCTURED).toString(),
            new ValueMapDecorator(values));
    }

    /**
     * Retrieves an aggregated predicate used against property names. The predicate takes into account the {@code
     * prefix} and {@code postfix} filters as specified in an annotation such as {@link
     * com.exadel.aem.toolkit.api.annotations.injectors.Child} or {@link com.exadel.aem.toolkit.api.annotations.injectors.Children}
     * @param prefix  A string representing a required property name prefix
     * @param postfix A string representing a required property name prefix
     * @return List of predicates
     */
    public static Predicate<String> getPropertyNamePredicate(String prefix, String postfix) {
        List<Predicate<String>> predicates = new ArrayList<>();

        if (StringUtils.isNotBlank(prefix)) {
            predicates.add(value -> value.startsWith(prefix));
        }

        if (StringUtils.isNotBlank(postfix)) {
            predicates.add(value -> value.endsWith(postfix));
        }

        return predicates.stream().reduce(Predicate::and).orElse(value -> true);
    }
}
