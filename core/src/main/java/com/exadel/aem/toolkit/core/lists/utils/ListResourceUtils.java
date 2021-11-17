package com.exadel.aem.toolkit.core.lists.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import com.adobe.granite.ui.components.ds.ValueMapResource;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.lists.ListConstants;
import com.exadel.aem.toolkit.core.lists.models.SimpleListItem;

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
     * Converts collection of {@link SimpleListItem} to list of {@link ValueMapResource}
     * @param resourceResolver Sling {@link ResourceResolver} instance used to create the list
     * @param values           collection of {@link SimpleListItem} that will be converted to {@link ValueMapResource}
     * @return list of {@link ValueMapResource}
     */
    static List<Resource> mapToValueMapResources(ResourceResolver resourceResolver, Collection<SimpleListItem> values) {
        return CollectionUtils.emptyIfNull(values).stream()
            .map(entry -> ListResourceUtils.createValueMapResource(resourceResolver, entry.getTitle(), entry.getValue()))
            .collect(Collectors.toList());
    }

    /**
     * Converts key-value map to list of {@link ValueMapResource}
     * @param resourceResolver Sling {@link ResourceResolver} instance used to create the list
     * @param values           key-value map that will be converted to {@link ValueMapResource}
     * @return list of {@link ValueMapResource}
     */
    static List<Resource> mapToValueMapResources(ResourceResolver resourceResolver, Map<String, Object> values) {
        return MapUtils.emptyIfNull(values).entrySet().stream()
            .map(entry -> ListResourceUtils.createValueMapResource(resourceResolver, entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }

    /**
     * Creates {@link ValueMapResource} representation of {@code listItem} entry using title as a {@code jcr:title}
     * and {@code value} as a value
     * @param resourceResolver Sling {@link ResourceResolver} instance used to create the list
     * @param title            {@code jcr:title} of {@code listItem}
     * @param value            {@code value} of {@code listItem}
     * @return {@link ValueMapResource} representation of {@code listItem}
     */
    static Resource createValueMapResource(ResourceResolver resourceResolver, String title, Object value) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(JcrConstants.JCR_TITLE, title);
        properties.put(CoreConstants.PN_VALUE, value);
        return new ValueMapResource(resourceResolver, "", JcrConstants.NT_UNSTRUCTURED, new ValueMapDecorator(properties));
    }

    /**
     * Returns a map without ignored properties
     * @param properties initial map of properties
     * @return filtered map of properties
     */
    static Map<String, Object> getWithoutSystemProperties(Map<String, Object> properties) {
        return properties.entrySet().stream()
            .filter(entry -> !PROPERTIES_TO_IGNORE.contains(entry.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Default (instantiation-restricting) constructor
     */
    private ListResourceUtils() {
    }
}
