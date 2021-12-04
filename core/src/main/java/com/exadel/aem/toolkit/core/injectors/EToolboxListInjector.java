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
package com.exadel.aem.toolkit.core.injectors;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.day.cq.commons.jcr.JcrConstants;

import com.exadel.aem.toolkit.api.annotations.injectors.EToolboxList;
import com.exadel.aem.toolkit.core.lists.utils.ListHelper;

/**
 * Injects EToolbox Lists into Sling models obtained via {@code ResourceResolver} instance
 * @see ListHelper
 * @see Injector
 */
@Component(service = Injector.class,
    property = Constants.SERVICE_RANKING + ":Integer=" + InjectorConstants.SERVICE_RANKING
)
public class EToolboxListInjector implements Injector {

    private static final Logger LOG = LoggerFactory.getLogger(EToolboxListInjector.class);

    public static final String NAME = "eak-etoolbox-list-injector";

    /**
     * Retrieves the name of the current instance
     * @return String value
     * @see Injector
     */
    @Nonnull
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Attempts to inject list entries into the given adaptable
     * @param adaptable        A {@link SlingHttpServletRequest} or a {@link Resource} instance
     * @param name             Name of the Java class member to inject the value into
     * @param type             Type of receiving Java class member
     * @param element          {@link AnnotatedElement} instance that facades the Java class member allowing to retrieve
     *                         annotation objects
     * @param callbackRegistry {@link DisposalCallbackRegistry} object
     * @return The value to inject, or null in case injection is not possible
     * @see Injector
     * @see ListHelper
     */
    @Override
    public Object getValue(
        @Nonnull Object adaptable,
        String name,
        @Nonnull Type type,
        @Nonnull AnnotatedElement element,
        @Nonnull DisposalCallbackRegistry callbackRegistry) {

        EToolboxList annotation = element.getDeclaredAnnotation(EToolboxList.class);
        if (annotation == null) {
            return null;
        }

        ResourceResolver resourceResolver = InjectorUtils.getResourceResolver(adaptable);
        if (resourceResolver == null) {
            return null;
        }

        if (InjectorUtils.isValidRawType(type, Collection.class)) {
            return getList(resourceResolver, annotation.value(), type);

        } else if (InjectorUtils.isValidRawType(type, Map.class)) {
            return getMap(resourceResolver, annotation.value(), annotation.keyProperty(), type);

        } else if (!(type instanceof ParameterizedType) && ((Class<?>) type).isArray()) {
            return getArray(resourceResolver, annotation.value(), (Class<?>) type);
        }

        LOG.debug(InjectorConstants.EXCEPTION_UNSUPPORTED_TYPE, type);
        return null;
    }

    /**
     * Retrieves the collection of items representing list entries stored under given {@code path}.
     * If the provided {@code Type} of the Java class member is a {@code Resource} or {@code Object}, it retrieves
     * a List of resources, otherwise it retrieves a List of generic typed instances adapted to the provided {@code Type}
     * @param resourceResolver Sling {@code ResourceResolver} instance used to access the list
     * @param path             JCR path of the items list
     * @param type             {@code Type} object
     * @return List of generic typed instances. If the path provided is invalid or cannot be resolved,
     * an empty list is returned
     */
    private List<?> getList(ResourceResolver resourceResolver, String path, Type type) {

        return InjectorUtils.isValidCollection(type, Resource.class)
            ? ListHelper.getResourceList(resourceResolver, path)
            : ListHelper.getList(resourceResolver, path, getClass(type, 0));
    }

    /**
     * Retrieves the collection of list entries stored under given {@code path} that is transformed into a key-value map.
     * If the {@code keyProperty} is not specified, the Map keys represent the {@code jcr:title} property
     * of the underlying resource, otherwise the keys represent the attribute of the underlying resources
     * specified by the given {@code keyProperty}
     * @param resourceResolver Sling {@code ResourceResolver} instance used to access the list
     * @param path             JCR path of the items list
     * @param keyProperty      Item resource property that holds the key of the resulting map
     * @param type             {@code Type} object
     * @return Map containing a generic typed instances. If the path provided is invalid or cannot be resolved, an empty
     * map is returned
     */
    private Map<?, ?> getMap(ResourceResolver resourceResolver, String path, String keyProperty, Type type) {

        return keyProperty.isEmpty()
            ? getMapWithoutKeyProperty(resourceResolver, path, type)
            : getMapWithKeyProperty(resourceResolver, path, keyProperty, type);
    }

    /**
     * Retrieves the array of items representing list entries stored under given {@code path}.
     * If the provided {@code Type} is {@code Resource} or {@code Object}, it retrieves an array of resources,
     * otherwise it retrieves an array of generic typed instances adapted to the provided {@code type}
     * @param resourceResolver Sling {@code ResourceResolver} instance used to access the list
     * @param path             JCR path of the items list
     * @param type             {@code Type} object
     * @return Array of generic typed instances. If the path provided is invalid or cannot be resolved,
     * an empty array is returned
     */
    private Object[] getArray(ResourceResolver resourceResolver, String path, Class<?> type) {

        return InjectorUtils.isValidArray(type, Resource.class)
            ? toArray(ListHelper.getResourceList(resourceResolver, path), type.getComponentType())
            : toArray(ListHelper.getList(resourceResolver, path, type.getComponentType()), type.getComponentType());
    }

    /**
     * Retrieves a collection of list entries stored under given {@code path} that is transformed into a key-value map.
     * The keys represent {@code jcr:title} property of the underlying resource. If the provided {@code Type}
     * is {@code String} or {@code Object}, the value represents {@code value} property, otherwise values are the
     * underlying resources themselves as adapted to the provided {@code type} model
     * @param resourceResolver Sling {@code ResourceResolver} instance used to access the list
     * @param path             JCR path of the items list
     * @param type             {@code Class} reference representing type of map values required
     * @return Map containing a generic typed instances. If the path provided is invalid or cannot be resolved, an empty
     * map is returned
     */
    private Map<?, ?> getMapWithoutKeyProperty(ResourceResolver resourceResolver, String path, Type type) {

        return InjectorUtils.isValidMap(type, String.class)
            ? ListHelper.getMap(resourceResolver, path)
            : ListHelper.getMap(resourceResolver, path, JcrConstants.JCR_TITLE, getClass(type, 1));
    }

    /**
     * Retrieves a collection of list entries stored under given {@code path} that is transformed into a key-value map.
     * The keys represent the attribute of the underlying resources specified by the given {@code keyProperty}.
     * If the provided {@code Type} is {@code Resource} or {@code Object}, the values are the underlying
     * resources themselves, otherwise the underlying resources are adapted to the provided {@code type} model
     * @param resourceResolver Sling {@code ResourceResolver} instance used to access the list
     * @param path             JCR path of the items list
     * @param keyProperty      Item resource property that holds the key of the resulting map
     * @param type             {@code Type} object
     * @return Map containing a generic typed instances. If the path provided is invalid or cannot be resolved, an empty
     * map is returned
     */
    private Map<?, ?> getMapWithKeyProperty(ResourceResolver resourceResolver, String path, String keyProperty, Type type) {

        return InjectorUtils.isValidMap(type, Resource.class)
            ? ListHelper.getResourceMap(resourceResolver, path, keyProperty)
            : ListHelper.getMap(resourceResolver, path, keyProperty, getClass(type, 1));
    }

    /**
     * Retrieves type arguments from parameterized collection type
     * @param type  Type of receiving Java class member
     * @param index The index of the element in the array of type objects
     * @return Class of type object
     */
    private Class<?> getClass(Type type, int index) {
        return (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[index];
    }

    /**
     * Converts the generic {@code List} to an array and casts it to the specified type
     * @param list A generic {@code List} that contains list entries
     * @param type Type of receiving Java class member
     * @param <T>  The class of the objects in the array
     * @return An array containing all the elements from provided List
     */
    @SuppressWarnings("unchecked")
    private <T> T[] toArray(List<?> list, Class<?> type) {
        T[] array = (T[]) Array.newInstance(type, list.size());
        IntStream.range(0, list.size()).forEach(i -> array[i] = (T) list.get(i));
        return array;
    }
}
