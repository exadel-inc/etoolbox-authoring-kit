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
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
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
class ListResourceUtils {

    public static final Map<String, Object> LIST_PROPERTIES
        = Collections.singletonMap(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, "wcm/foundation/components/responsivegrid");

    private static final List<String> PROPERTIES_TO_IGNORE = Arrays.asList(
        "jcr:createdBy", "jcr:created", "cq:lastModified", "cq:lastModifiedBy", "jcr:lastModified", "jcr:lastModifiedBy",
        "cq:lastReplicationAction", "cq:lastReplicatedBy", "cq:lastReplicated"
    );

    /**
     * Default (instantiation-restricting) constructor
     */
    private ListResourceUtils() {
    }

    /**
     * Converts key-value map to list of {@link ValueMapResource} where each item represents {@code listItem}
     * @param values Key-value map that will be converted to {@link ValueMapResource}
     * @return List of {@link ValueMapResource}
     */
    public static List<Resource> mapToListItemResources(Map<String, Object> values) {
        return MapUtils.emptyIfNull(values).entrySet().stream()
            .map(entry -> ListResourceUtils.createListItemResource(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }

    /**
     * Returns a map without system properties
     * @param properties Initial map of properties
     * @return Filtered map of properties
     */
    public static Map<String, Object> excludeSystemProperties(Map<String, Object> properties) {
        return MapUtils.emptyIfNull(properties).entrySet().stream()
            .filter(entry -> !PROPERTIES_TO_IGNORE.contains(entry.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Returns BiFunction mapper that converts model into resource.
     * @param modelType Model type
     * @return Mapper that converts model into resource.
     */
    public static BiFunction<Object, ObjectMapper, Resource> getMapper(Class<?> modelType) {
        if (SimpleListItem.class.equals(modelType)) {
            return (model, objectMapper) -> {
                SimpleListItem simpleListItem = (SimpleListItem) model;
                return createListItemResource(simpleListItem.getTitle(), simpleListItem.getValue());
            };
        }

        return (model, objectMapper) -> {
            Map<String, Object> properties = objectMapper.convertValue(model, new TypeReference<Map<String, Object>>() {
            });
            return new ValueMapResource(null, new ResourceMetadata(), JcrConstants.NT_UNSTRUCTURED, new ValueMapDecorator(properties));
        };
    }

    /**
     * Creates {@link ValueMapResource} representation of {@code listItem} entry using title as a {@code jcr:title}
     * and {@code value} as a value
     * @param title {@code jcr:title} of {@code listItem}
     * @param value {@code value} of {@code listItem}
     * @return {@link ValueMapResource} representation of {@code listItem}
     */
    private static Resource createListItemResource(String title, Object value) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(JcrConstants.JCR_TITLE, title);
        properties.put(CoreConstants.PN_VALUE, value);
        properties.put(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, ListConstants.LIST_ITEM_RESOURCE_TYPE);
        return new ValueMapResource(null, new ResourceMetadata(), JcrConstants.NT_UNSTRUCTURED, new ValueMapDecorator(properties));
    }
}
