package com.exadel.aem.toolkit.bundle.customlists.util;

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
import org.apache.sling.api.resource.ValueMap;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

import com.exadel.aem.toolkit.bundle.customlists.models.GenericItem;

/**
 * Helper methods for working with AEM Custom Lists
 */
public class CustomListsHelper {

    private static final String PN_VALUE = "value";

    /**
     * Retrieves the list of item resources from {@code listPagePath} and adapts them to {@link GenericItem} model
     * @param resourceResolver an instance of ResourceResolver
     * @param listPagePath     the path to List page
     * @return a list of {@link GenericItem}s
     */
    public static List<GenericItem> getAsGenericList(ResourceResolver resourceResolver, String listPagePath) {
        return getAsList(resourceResolver, listPagePath, res -> res.adaptTo(GenericItem.class));
    }

    /**
     * Retrieves the list of item resources from {@code listPagePath} and returns their {@link ValueMap}s
     * @param resourceResolver an instance of ResourceResolver
     * @param listPagePath     the path to List page
     * @return a list of {@link ValueMap}s
     */
    public static List<ValueMap> getAsCustomList(ResourceResolver resourceResolver, String listPagePath) {
        return getAsList(resourceResolver, listPagePath, Resource::getValueMap);
    }

    /**
     * Retrieves the list of item resources from {@code listPagePath} and adapts them to the specified {@code itemClass}
     * @param resourceResolver an instance of ResourceResolver
     * @param listPagePath     the path to List page
     * @param itemClass        parametrized model class
     * @param <T>              model which represents a list item
     * @return a list of {@code <T>} instances
     */
    public static <T> List<T> getAsCustomList(ResourceResolver resourceResolver, String listPagePath, Class<T> itemClass) {
        return getAsList(resourceResolver, listPagePath, res -> res.adaptTo(itemClass));
    }

    private static <T> List<T> getAsList(ResourceResolver resourceResolver, String listPagePath, Function<Resource, T> mapper) {
        return getItemsStream(resourceResolver, listPagePath)
            .map(mapper)
            .collect(Collectors.toList());
    }

    /**
     * Retrieves the list of item resources from {@code listPagePath} and maps {@code jcr:title}s to {@code value}s.
     * If several items have the same {@code jcr:title}, the last value overrides all the previous ones
     * @param resourceResolver an instance of ResourceResolver
     * @param listPagePath     the path to List page
     * @return a map representing title-to-value pairs
     */
    public static Map<String, String> getAsGenericMap(ResourceResolver resourceResolver, String listPagePath) {
        return getAsMap(resourceResolver, listPagePath, JcrConstants.JCR_TITLE,
            res -> res.getValueMap().get(PN_VALUE, StringUtils.EMPTY));
    }

    /**
     * Retrieves the list of item resources from {@code listPagePath} and maps values from the specified {@code keyName} property to items' {@link ValueMap}s.
     * If several items have the same key, the last value overrides all the previous ones
     * @param resourceResolver an instance of ResourceResolver
     * @param listPagePath     the path to List page
     * @param keyName          item property that holds the resulting map's keys
     * @return a map that represents items as {@link ValueMap}s
     */
    public static Map<String, ValueMap> getAsCustomMap(ResourceResolver resourceResolver, String listPagePath, String keyName) {
        return getAsMap(resourceResolver, listPagePath, keyName, Resource::getValueMap);
    }

    /**
     * Retrieves the list of item resources from {@code listPagePath} and maps values from the specified {@code keyName} to the {@code itemClass} model, adapted from item resource.
     * If several items have the same key, the last value overrides all the previous ones
     * @param resourceResolver an instance of ResourceResolver
     * @param listPagePath     the path to List page
     * @param keyName          item property that holds the resulting map's keys
     * @param itemClass        parametrized model class
     * @param <T>              model which represents a list item
     * @return a map that represents items as {@code <T>} instances
     */
    public static <T> Map<String, T> getAsCustomMap(ResourceResolver resourceResolver, String listPagePath, String keyName, Class<T> itemClass) {
        return getAsMap(resourceResolver, listPagePath, keyName, res -> res.adaptTo(itemClass));
    }


    private static <T> Map<String, T> getAsMap(ResourceResolver resourceResolver, String listPagePath, String keyName, Function<Resource, T> mapper) {
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

    private CustomListsHelper() {
    }
}
