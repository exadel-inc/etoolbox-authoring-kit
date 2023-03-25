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

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.spi.Injector;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import com.day.cq.commons.jcr.JcrConstants;

import com.exadel.aem.toolkit.api.annotations.injectors.EToolboxList;
import com.exadel.aem.toolkit.core.injectors.utils.AdaptationUtil;
import com.exadel.aem.toolkit.core.injectors.utils.TypeUtil;
import com.exadel.aem.toolkit.core.lists.utils.ListHelper;

/**
 * Injects into a Sling model entries of an EToolbox List obtained via a {@code ResourceResolver} instance
 * @see ListHelper
 * @see EToolboxList
 * @see BaseInjector
 */
@Component(service = Injector.class,
    property = Constants.SERVICE_RANKING + ":Integer=" + BaseInjector.SERVICE_RANKING
)
public class EToolboxListInjector extends BaseInjector<EToolboxList> {

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
     * {@inheritDoc}
     */
    @Override
    public EToolboxList getAnnotationType(AnnotatedElement element) {
        return element.getDeclaredAnnotation(EToolboxList.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(Object adaptable, String name, Type type, EToolboxList annotation) {

        ResourceResolver resourceResolver = AdaptationUtil.getResourceResolver(adaptable);
        if (resourceResolver == null) {
            return null;
        }

        if (TypeUtil.isValidRawType(type, Collection.class)) {
            return getList(resourceResolver, annotation.value(), type);

        } else if (TypeUtil.isValidRawType(type, Map.class)) {
            return getMap(resourceResolver, annotation.value(), annotation.keyProperty(), type);

        } else if (!(type instanceof ParameterizedType) && ((Class<?>) type).isArray()) {
            return getArray(resourceResolver, annotation.value(), (Class<?>) type);
        }

        return null;
    }

    /**
     * Retrieves the list entries stored under the given {@code path}. If the {@code type} parameter points to a
     * {@code Resource} or an {@code Object}, a list of resources is returned. Otherwise, a list of generic-typed
     * entities adapted to the provided {@code type} is returned
     * @param resourceResolver Sling {@code ResourceResolver} instance used to access the list
     * @param path             JCR path of the items list
     * @param type             {@code Type} object
     * @return List of generic-typed instances. If the provided path is invalid or cannot be resolved, an empty list is
     * returned
     */
    private List<?> getList(ResourceResolver resourceResolver, String path, Type type) {

        return TypeUtil.isValidCollection(type, Resource.class)
            ? ListHelper.getResourceList(resourceResolver, path)
            : ListHelper.getList(resourceResolver, path, getClass(type, 0));
    }

    /**
     * Retrieves the list entries stored under the given {@code path}. Each is put into a key-value map. If the
     * {@code keyProperty} is not specified, the map key represents the {@code jcr:title} property of the underlying
     * resource. Otherwise, the key represents the attribute of the underlying resource specified by the given
     * {@code keyProperty}
     * @param resourceResolver Sling {@code ResourceResolver} instance used to access the list
     * @param path             JCR path of the items list
     * @param keyProperty      Name of the property from which to extract keys for the resulting map
     * @param type             {@code Type} object
     * @return Map containing generic-typed instances. If the path provided is invalid or cannot be resolved, an empty
     * map is returned
     */
    private Map<?, ?> getMap(ResourceResolver resourceResolver, String path, String keyProperty, Type type) {

        return keyProperty.isEmpty()
            ? getMapWithoutKeyProperty(resourceResolver, path, type)
            : getMapWithKeyProperty(resourceResolver, path, keyProperty, type);
    }

    /**
     * Retrieves list entries stored under the given {@code path} in the form of a key-value map. The key represents the
     * {@code jcr:title} property of the underlying resource. If the provided {@code type} is {@code String} or
     * {@code Object}, the value represents {@code value} property. Otherwise, values are the underlying resources
     * themselves adapted to the provided {@code type} model
     * @param resourceResolver Sling {@code ResourceResolver} instance used to access the list
     * @param path             JCR path of the items list
     * @param type             {@code Class} reference representing the type of the map values
     * @return Map containing generic-typed instances. If the provided path is invalid or cannot be resolved, an empty
     * map is returned
     */
    private Map<?, ?> getMapWithoutKeyProperty(ResourceResolver resourceResolver, String path, Type type) {

        return TypeUtil.isValidMap(type, String.class)
            ? ListHelper.getMap(resourceResolver, path)
            : ListHelper.getMap(resourceResolver, path, JcrConstants.JCR_TITLE, getClass(type, 1));
    }

    /**
     * Retrieves the list entries stored under given {@code path} in the form of a key-value map. Each key in the map
     * represents one selected attribute of the underlying resource specified by the given {@code keyProperty}. If the
     * provided {@code Type} is {@code Resource} or {@code Object}, the values of the map are the underlying resources
     * themselves. Otherwise, the underlying resources are adapted to the provided {@code type}
     * @param resourceResolver Sling {@code ResourceResolver} instance used to access the list
     * @param path             JCR path of the items list
     * @param keyProperty      Name of the property from which to extract keys for the resulting map
     * @param type             {@code Type} of the map values
     * @return Map containing generic-typed instances. If the provided path is invalid or cannot be resolved, an empty
     * map is returned
     */
    private Map<?, ?> getMapWithKeyProperty(ResourceResolver resourceResolver, String path, String keyProperty, Type type) {

        return TypeUtil.isValidMap(type, Resource.class)
            ? ListHelper.getResourceMap(resourceResolver, path, keyProperty)
            : ListHelper.getMap(resourceResolver, path, keyProperty, getClass(type, 1));
    }

    /**
     * Retrieves an array representing list entries stored under the given {@code path}. If the {@code type} parameter
     * is {@code Resource} or {@code Object}, an array of resources is returned. Otherwise, generic-typed instances
     * adapted to the provided {@code type} are returned
     * @param resourceResolver Sling {@code ResourceResolver} instance used to access the list
     * @param path             JCR path of the items list
     * @param type             {@code Type} object
     * @return An array of generic-typed instances. If the provided path is invalid or cannot be resolved, an empty
     * array is returned
     */
    private Object[] getArray(ResourceResolver resourceResolver, String path, Class<?> type) {

        return TypeUtil.isValidArray(type, Resource.class)
            ? toArray(ListHelper.getResourceList(resourceResolver, path), type.getComponentType())
            : toArray(ListHelper.getList(resourceResolver, path, type.getComponentType()), type.getComponentType());
    }

    /**
     * Retrieves a type argument from the parameterized collection type
     * @param type  Type of receiving Java class member
     * @param index The index of the type argument
     * @return {@code Class} object that matches the type parameter
     */
    private Class<?> getClass(Type type, int index) {
        return (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[index];
    }

    /**
     * Converts a generic {@code List} to an array and casts it to the specified type
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
