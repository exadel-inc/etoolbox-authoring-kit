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

package com.exadel.aem.toolkit.api.lists.services;

import java.util.List;
import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import com.day.cq.commons.jcr.JcrConstants;

import com.exadel.aem.toolkit.api.lists.models.SimpleListItem;

/**
 * Contains methods that assist in retrieving AEMBox Lists values
 */
public interface ListHelperService {

    /**
     * Retrieves collection of Sling {@link Resource}s representing list entries stored under given {@code path}
     * @param resourceResolver Sling {@code ResourceResolver} instance used to access the list
     * @param path             JCR path of the items list
     * @return List of resources. If the path provided is invalid or cannot be resolved, an empty list
     * is returned
     */
    default List<Resource> getResourceList(ResourceResolver resourceResolver, String path) {
        return getList(resourceResolver, path, Resource.class);
    }

    /**
     * Retrieves collection of {@link SimpleListItem} values representing list entries stored under given {@code path}
     * @param resourceResolver Sling {@code ResourceResolver} instance used to access the list
     * @param path             JCR path of the items list
     * @return List of {@link SimpleListItem}s. If the path provided is invalid or cannot be resolved, an empty list
     * is returned
     */
    default List<SimpleListItem> getList(ResourceResolver resourceResolver, String path) {
        return getList(resourceResolver, path, SimpleListItem.class);
    }

    /**
     * Retrieves collection of items representing list entries stored under given {@code path} adapted to the provided
     * {@code itemType}
     * @param resourceResolver Sling {@code ResourceResolver} instance used to access the list
     * @param path             JCR path of the items list
     * @param itemType         {@code Class} reference representing type of entries required
     * @param <T>              Type of list entries, must be one adaptable from a Sling {@code Resource}
     * @return List of {@code <T>}-typed instances. If the path provided is invalid or cannot be resolved, or else
     * a non-adaptable {@code itemType} is given, an empty list is returned
     */
    <T> List<T> getList(ResourceResolver resourceResolver, String path, Class<T> itemType);

    /**
     * Retrieves collection of list entries stored under given {@code path} that is transformed into a key-value map.
     * The keys represent {@code jcr:title} property of the underlying resource while the values are the underlying
     * resources themselves. If several items have the same {@code jcr:title}, the last one is effective
     * @param resourceResolver Sling {@code ResourceResolver} instance used to access the list
     * @param path             JCR path of the items list
     * @return Map representing title-to-value pairs. If the path provided is invalid or cannot be resolved, an empty
     * map is returned
     */
    default Map<String, Resource> getResourceMap(ResourceResolver resourceResolver, String path) {
        return getMap(resourceResolver, path, JcrConstants.JCR_TITLE, Resource.class);
    }

    /**
     * Retrieves collection of list entries stored under given {@code path} that is transformed into a key-value map.
     * The keys represent the attribute of the underlying resources specified by the given {@code keyName} while the
     * values are the underlying resources themselves. If several items have the same {@code jcr:title}, the last one
     * is effective
     * @param resourceResolver Sling {@code ResourceResolver} instance used to access the list
     * @param path             JCR path of the items list
     * @param keyName          Item resource property that holds the key of the resulting map
     * @return Map representing title-to-value pairs. If the path provided is invalid or cannot be resolved, an empty
     * map is returned
     */
    default Map<String, Resource> getResourceMap(ResourceResolver resourceResolver, String path, String keyName) {
        return getMap(resourceResolver, path, keyName, Resource.class);
    }

    /**
     * Retrieves collection of list entries stored under given {@code path} that is transformed into a key-value map.
     * The keys represent {@code jcr:title} property of the underlying resource while the value represents {@code value}
     * property. If several items have the same {@code jcr:title}, the last one is effective
     * @param resourceResolver Sling {@code ResourceResolver} instance used to access the list
     * @param path             JCR path of the items list
     * @return Map representing title-to-value pairs. If the path provided is invalid or cannot be resolved, an empty
     * map is returned
     */
    Map<String, String> getMap(ResourceResolver resourceResolver, String path);

    /**
     * Retrieves collection of list entries stored under given {@code path} that is transformed into a key-value map.
     * Keys represent the attribute of the underlying resources specified by the given {@code keyName}. Values are the
     * underlying resources themselves as adapted to the provided {@code itemType} model. If several items have the same
     * key, the last one is effective
     * @param resourceResolver Sling {@code ResourceResolver} instance used to access the list
     * @param path             JCR path of the items list
     * @param keyName          Item resource property that holds the key of the resulting map
     * @param itemType         {@code Class} reference representing type of map values required
     * @param <T>              Type of map values, must be one adaptable from a Sling {@code Resource}
     * @return Map containing {@code <T>}-typed instances. If the path provided is invalid or cannot be resolved,
     * or else a non-adaptable model {@code itemType} is given, an empty map is returned
     */
    <T> Map<String, T> getMap(ResourceResolver resourceResolver, String path, String keyName, Class<T> itemType);
}
