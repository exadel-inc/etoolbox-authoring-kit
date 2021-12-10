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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MapUtils;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import com.adobe.granite.ui.components.ds.ValueMapResource;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.lists.ListConstants;
import com.exadel.aem.toolkit.core.lists.models.SimpleListItem;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Contains methods for manipulation with List Resource
 */
class ListResourceUtils {

    private static final Map<String, Object> LIST_PROPERTIES
        = Collections.singletonMap(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, "wcm/foundation/components/responsivegrid");

    private static final List<String> PROPERTIES_TO_IGNORE = Arrays.asList(
        "jcr:createdBy", "jcr:created", "cq:lastModified", "cq:lastModifiedBy", "jcr:lastModified", "jcr:lastModifiedBy",
        "cq:lastReplicationAction", "cq:lastReplicatedBy", "cq:lastReplicated"
    );

    /**
     * Creates a list resource under given parent
     * @param resourceResolver Sling {@link ResourceResolver} instance used to create the list
     * @param parent           List Page that holds list resource
     * @return list Resource or {@code null} if {@link ResourceResolver} or {@code parent} is null
     * @throws PersistenceException if list resource cannot be created
     */
    static Resource createListResource(ResourceResolver resourceResolver, Page parent) throws PersistenceException {
        if (resourceResolver == null || parent == null) {
            return null;
        }

        return resourceResolver.create(parent.getContentResource(), ListConstants.NN_LIST, LIST_PROPERTIES);
    }

    /**
     * Create {@link com.exadel.aem.toolkit.core.lists.models.internal.ListItemModel} resource under {@code parent}
     * container with given properties.
     * @param resourceResolver Sling {@link ResourceResolver} instance used to create the list
     * @param parent           Container for {@code listItem}'s.
     * @param properties       {@code listItem} properties
     * @throws PersistenceException if {@code listItem} cannot be created
     */
    static void createListItem(ResourceResolver resourceResolver, Resource parent, Map<String, Object> properties) throws PersistenceException {
        Map<String, Object> withoutSystemProperties = getWithoutSystemProperties(properties);
        withoutSystemProperties.put(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, ListConstants.LIST_ITEM_RESOURCE_TYPE);
        resourceResolver.create(parent, ResourceUtil.createUniqueChildName(parent, CoreConstants.PN_LIST_ITEM), withoutSystemProperties);
    }

    /**
     * Converts key-value map to list of {@link ValueMapResource}
     * @param values key-value map that will be converted to {@link ValueMapResource}
     * @return list of {@link ValueMapResource}
     */
    static List<Resource> mapToValueMapResources(Map<String, Object> values) {
        return MapUtils.emptyIfNull(values).entrySet().stream()
            .map(entry -> ListResourceUtils.createValueMapResource(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }

    /**
     * Creates {@link ValueMapResource} representation of {@code listItem} entry using title as a {@code jcr:title}
     * and {@code value} as a value
     * @param title {@code jcr:title} of {@code listItem}
     * @param value {@code value} of {@code listItem}
     * @return {@link ValueMapResource} representation of {@code listItem}
     */
    static Resource createValueMapResource(String title, Object value) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(JcrConstants.JCR_TITLE, title);
        properties.put(CoreConstants.PN_VALUE, value);
        return createValueMapResource(properties);
    }

    /**
     * Creates {@link ValueMapResource} with given properties
     * @param properties resource properties
     * @return {@link ValueMapResource}
     */
    static Resource createValueMapResource(Map<String, Object> properties) {
        return new ValueMapResource(null, "", JcrConstants.NT_UNSTRUCTURED, new ValueMapDecorator(properties));
    }

    /**
     * Returns BiFunction mapper that converts model into properties.
     * @param modelType Model type
     * @return Mapper that converts model into properties.
     */
    static BiFunction<Object, ObjectMapper, Map<String, Object>> getMapper(Class<?> modelType) {
        if (SimpleListItem.class.equals(modelType)) {
            return (model, objectMapper) -> {
                SimpleListItem simpleListItem = (SimpleListItem) model;
                Map<String, Object> properties = new HashMap<>();
                properties.put(JcrConstants.JCR_TITLE, simpleListItem.getTitle());
                properties.put(CoreConstants.PN_VALUE, simpleListItem.getValue());
                return properties;
            };
        }

        return (model, objectMapper) -> objectMapper.convertValue(model, new TypeReference<Map<String, Object>>() {
        });
    }

    /**
     * Returns a map without ignored properties
     * @param properties initial map of properties
     * @return filtered map of properties
     */
    private static Map<String, Object> getWithoutSystemProperties(Map<String, Object> properties) {
        return MapUtils.emptyIfNull(properties).entrySet().stream()
            .filter(entry -> !PROPERTIES_TO_IGNORE.contains(entry.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Default (instantiation-restricting) constructor
     */
    private ListResourceUtils() {
    }
}
