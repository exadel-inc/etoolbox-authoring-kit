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
package com.exadel.aem.toolkit.core.lists.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import com.day.cq.commons.jcr.JcrConstants;
import com.adobe.granite.ui.components.ds.ValueMapResource;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.lists.ListConstants;
import com.exadel.aem.toolkit.core.lists.models.SimpleListItem;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Contains methods for manipulation with List Resource
 */
class ListResourceUtil {

    /**
     * Default (instantiation-restricting) constructor
     */
    private ListResourceUtil() {
    }

    private static final List<String> PROPERTIES_TO_IGNORE = Arrays.asList(
        "jcr:createdBy", "jcr:created", "cq:lastModified", "cq:lastModifiedBy", "jcr:lastModified", "jcr:lastModifiedBy",
        "cq:lastReplicationAction", "cq:lastReplicatedBy", "cq:lastReplicated"
    );

    /**
     * Create a {@link com.exadel.aem.toolkit.core.lists.models.internal.ListItemModel} resource under {@code parent}
     * container with given properties.
     * @param resourceResolver Sling {@link ResourceResolver} instance used to create the list
     * @param parent           JCR resource that will contain the list entries
     * @param properties       Properties of the list entry
     * @throws PersistenceException If the list item could not be created
     */
    public static void createListItem(ResourceResolver resourceResolver, Resource parent, Map<String, Object> properties) throws PersistenceException {
        Map<String, Object> withoutSystemProperties = excludeSystemProperties(properties);
        withoutSystemProperties.put(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, ListConstants.LIST_ITEM_RESOURCE_TYPE);
        resourceResolver.create(parent, ResourceUtil.createUniqueChildName(parent, CoreConstants.PN_LIST_ITEM), withoutSystemProperties);
    }

    /**
     * Creates a {@link ValueMapResource} representation of a list entry using the provided {@code title} and {@code value}
     * @param resourceResolver Sling {@link ResourceResolver} instance used to create the list
     * @param title String value representing the title of the list entry
     * @param value String value representing the value of the list entry
     * @return {@link ValueMapResource} object
     */
    public static Resource createValueMapResource(ResourceResolver resourceResolver, String title, Object value) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(JcrConstants.JCR_TITLE, title);
        properties.put(CoreConstants.PN_VALUE, value);
        return createValueMapResource(resourceResolver, properties);
    }

    /**
     * Creates a {@link ValueMapResource} representation of a list entry using the provided properties
     * @param resourceResolver Sling {@link ResourceResolver} instance used to create the list
     * @param properties       Resource properties
     * @return {@link ValueMapResource}
     */
    public static Resource createValueMapResource(ResourceResolver resourceResolver, Map<String, Object> properties) {
        return new ValueMapResource(resourceResolver, StringUtils.EMPTY, JcrConstants.NT_UNSTRUCTURED, new ValueMapDecorator(properties));
    }

    /**
     * Converts a key-value map to the list of {@link ValueMapResource} objects
     * @param values {@code Map} instance that will be converted to the {@link ValueMapResource}
     * @return List of {@link ValueMapResource} objects
     */
    public static List<Resource> mapToValueMapResources(ResourceResolver resourceResolver, Map<String, Object> values) {
        return MapUtils.emptyIfNull(values).entrySet().stream()
            .map(entry -> ListResourceUtil.createValueMapResource(resourceResolver, entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }

    /**
     * Returns a {@code BiFunction} representing the conversion of a Sling model instance into a {@code Map} that can
     * further be used for creating a {@link ValueMapResource}
     * @param modelType Type of the Sling model
     * @return {@code BiFunction}.
     */
    public static BiFunction<Object, ObjectMapper, Map<String, Object>> getMapingFunction(Class<?> modelType) {
        if (ClassUtils.isAssignable(modelType, SimpleListItem.class)) {
            return (model, objectMapper) -> {
                SimpleListItem simpleListItem = (SimpleListItem) model;
                Map<String, Object> properties = new HashMap<>();
                properties.put(JcrConstants.JCR_TITLE, simpleListItem.getTitle());
                properties.put(CoreConstants.PN_VALUE, simpleListItem.getValue());
                return properties;
            };
        }
        return (model, objectMapper) -> objectMapper.convertValue(model, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * Filters the provided map of properties excluding the "system" properties not relevant for a list entry
     * @param properties {@code Map} instance
     * @return Filtered map
     */
    private static Map<String, Object> excludeSystemProperties(Map<String, Object> properties) {
        return MapUtils.emptyIfNull(properties).entrySet().stream()
            .filter(entry -> !PROPERTIES_TO_IGNORE.contains(entry.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
