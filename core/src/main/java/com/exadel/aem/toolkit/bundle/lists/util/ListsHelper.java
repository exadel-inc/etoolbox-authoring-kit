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

package com.exadel.aem.toolkit.bundle.lists.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

import com.exadel.aem.toolkit.bundle.lists.models.SimpleListItem;

/**
 * Helper methods for working with AAT Lists
 */
public class ListsHelper {

    private static final String PN_VALUE = "value";

    /**
     * Retrieves the list of item resources from {@code listPagePath}
     * @param resourceResolver An instance of ResourceResolver
     * @param listPagePath     The path to List page
     * @return A list of {@link Resource}s
     */
    public static List<Resource> getResourceList(ResourceResolver resourceResolver, String listPagePath) {
        return getList(resourceResolver, listPagePath, Function.identity());
    }

    /**
     * Retrieves the list of item resources from {@code listPagePath} and adapts them to {@link SimpleListItem} model
     * @param resourceResolver An instance of ResourceResolver
     * @param listPagePath     The path to List page
     * @return A list of {@link SimpleListItem}s
     */
    public static List<SimpleListItem> getList(ResourceResolver resourceResolver, String listPagePath) {
        return getList(resourceResolver, listPagePath, res -> res.adaptTo(SimpleListItem.class));
    }

    /**
     * Retrieves the list of item resources from {@code listPagePath} and adapts them to the specified {@code itemClass}
     * @param resourceResolver An instance of ResourceResolver
     * @param listPagePath     The path to List page
     * @param itemClass        Parametrized model class
     * @param <T>              Model which represents a list item
     * @return A list of {@code <T>} instances
     */
    public static <T> List<T> getList(ResourceResolver resourceResolver, String listPagePath, Class<T> itemClass) {
        return getList(resourceResolver, listPagePath, res -> res.adaptTo(itemClass));
    }

    private static <T> List<T> getList(ResourceResolver resourceResolver, String listPagePath, Function<Resource, T> mapper) {
        return getItemsStream(resourceResolver, listPagePath)
            .map(mapper)
            .collect(Collectors.toList());
    }

    /**
     * Retrieves the list of item resources from {@code listPagePath} and maps values from the specified {@code keyName} property to items' {@link Resource}s.
     * If several items have the same key, the last value overrides all the previous ones
     * @param resourceResolver An instance of ResourceResolver
     * @param listPagePath     The path to List page
     * @param keyName          Item property that holds the resulting map's keys
     * @return A map of item {@link Resource}s
     */
    public static Map<String, Resource> getResourceMap(ResourceResolver resourceResolver, String listPagePath, String keyName) {
        return getMap(resourceResolver, listPagePath, keyName, Function.identity());
    }

    /**
     * Retrieves the list of item resources from {@code listPagePath} and maps {@code jcr:title}s to {@code value}s.
     * If several items have the same {@code jcr:title}, the last value overrides all the previous ones
     * @param resourceResolver An instance of ResourceResolver
     * @param listPagePath     The path to List page
     * @return A map representing title-to-value pairs
     */
    public static Map<String, String> getMap(ResourceResolver resourceResolver, String listPagePath) {
        return getMap(resourceResolver, listPagePath, JcrConstants.JCR_TITLE,
            res -> res.getValueMap().get(PN_VALUE, StringUtils.EMPTY));
    }

    /**
     * Retrieves the list of item resources from {@code listPagePath} and maps values from the specified {@code keyName} to the {@code itemClass} model, adapted from item resource.
     * If several items have the same key, the last value overrides all the previous ones
     * @param resourceResolver An instance of ResourceResolver
     * @param listPagePath     The path to List page
     * @param keyName          Item property that holds the resulting map's keys
     * @param itemClass        Parametrized model class
     * @param <T>              Model which represents a list item
     * @return A map that represents items as {@code <T>} instances
     */
    public static <T> Map<String, T> getMap(ResourceResolver resourceResolver, String listPagePath, String keyName, Class<T> itemClass) {
        return getMap(resourceResolver, listPagePath, keyName, res -> res.adaptTo(itemClass));
    }

    private static <T> Map<String, T> getMap(ResourceResolver resourceResolver, String listPagePath, String keyName, Function<Resource, T> mapper) {
        return getItemsStream(resourceResolver, listPagePath)
            .map(res -> new ImmutablePair<>(res.getValueMap().get(keyName, StringUtils.EMPTY), mapper.apply(res)))
            .collect(Collectors.toMap(ImmutablePair::getLeft, ImmutablePair::getRight, (a, b) -> b, LinkedHashMap::new));
    }

    private static Stream<Resource> getItemsStream(ResourceResolver resourceResolver, String listPagePath) {
        Resource listResource = getListResource(resourceResolver, listPagePath);
        return listResource == null ?
            Stream.empty() :
            StreamSupport.stream(Spliterators.spliteratorUnknownSize(listResource.listChildren(), Spliterator.ORDERED), false);
    }

    private static Resource getListResource(ResourceResolver resourceResolver, String listPagePath) {
        if (resourceResolver == null || StringUtils.isBlank(listPagePath)) {
            return null;
        }
        return Optional.of(resourceResolver)
            .map(resolver -> resolver.adaptTo(PageManager.class))
            .map(pageManager -> pageManager.getPage(listPagePath))
            .map(Page::getContentResource)
            .map(contentRes -> contentRes.getChild("list"))
            .orElse(null);
    }

    private ListsHelper() {
    }
}
