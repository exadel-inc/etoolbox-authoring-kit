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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.spi.Injector;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.day.cq.commons.jcr.JcrConstants;

import com.exadel.aem.toolkit.api.annotations.injectors.EToolboxList;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.injectors.utils.AdaptationUtil;
import com.exadel.aem.toolkit.core.injectors.utils.TypeUtil;
import com.exadel.aem.toolkit.core.lists.utils.ListHelper;

/**
 * Provides injecting into a Sling model entries of an EToolbox List obtained via a {@code ResourceResolver} instance
 * @see ListHelper
 * @see EToolboxList
 * @see BaseInjector
 */
@Component(
    service = Injector.class,
    property = Constants.SERVICE_RANKING + ":Integer=" + BaseInjector.SERVICE_RANKING)
public class EToolboxListInjector extends BaseInjector<EToolboxList> {

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
     * {@inheritDoc}
     */
    @Override
    public EToolboxList getManagedAnnotation(AnnotatedElement element) {
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
        Class<?> rawType = TypeUtil.getRawType(type);
        if (List.class.equals(rawType) || Collection.class.equals(rawType) || Object.class.equals(rawType)) {
            return getList(resourceResolver, annotation.value(), type);

        } else if (Set.class.equals(rawType)) {
            List<?> valueList = getList(resourceResolver, annotation.value(), type);
            Class<?> listItemType = TypeUtil.getElementType(type);
            if (listItemType != null && listItemType.isInterface()) {
                return new ProxySet(valueList, listItemType);
            }
            return new LinkedHashSet<>(valueList);

        } else if (Map.class.equals(rawType)) {
            return getMap(resourceResolver, annotation.value(), annotation.keyProperty(), type);

        } else if (type instanceof Class<?> && ((Class<?>) type).isArray()) {
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
        return Object.class.equals(type) || TypeUtil.isSupportedCollectionOfType(type, Resource.class, false)
            ? ListHelper.getResourceList(resourceResolver, path)
            : ListHelper.getList(resourceResolver, path, getTypeArgument(type, 0));
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
        return TypeUtil.isMapOfValueType(type, String.class)
            ? ListHelper.getMap(resourceResolver, path)
            : ListHelper.getMap(resourceResolver, path, JcrConstants.JCR_TITLE, getTypeArgument(type, 1));
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

        return TypeUtil.isMapOfValueType(type, Resource.class)
            ? ListHelper.getResourceMap(resourceResolver, path, keyProperty)
            : ListHelper.getMap(resourceResolver, path, keyProperty, getTypeArgument(type, 1));
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

        return TypeUtil.isArrayOfType(type, Resource.class)
            ? toArray(ListHelper.getResourceList(resourceResolver, path), type.getComponentType())
            : toArray(ListHelper.getList(resourceResolver, path, type.getComponentType()), type.getComponentType());
    }

    /**
     * Retrieves a type argument from the parameterized collection type
     * @param type  Type of receiving Java class member
     * @param index The index of the type argument
     * @return {@code Class} object that matches the type parameter
     */
    private Class<?> getTypeArgument(Type type, int index) {
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

    /**
     * Represents a {@link Set} containing interface based proxy objects that do not have a proper implementation of
     * {@code hashCode()} and {@code equals()}. This can be, e.g., Sling model instances created out of
     * {@code @Model}-annotated Java interfaces
     */
    private static class ProxySet extends AbstractSet<Object> {

        private static final int HASH_SEED = 7;
        private static final int HASH_FACTOR = 31;

        private final LinkedList<Object> collection;
        private final Class<?> itemType;

        /**
         * Default constructor
         * @param collection The collection of objects to initialize the set with
         * @param itemType   {@code Class} reference that presents the type of objects in the set
         */
        private ProxySet(Collection<?> collection, Class<?> itemType) {
            this.collection = new LinkedList<>(collection);
            this.itemType = itemType;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean add(Object value) {
            if (!contains(value)) {
                return collection.add(value);
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean contains(Object value) {
            if (value == null) {
                return collection.stream().anyMatch(Objects::isNull);
            }
            if (!ClassUtils.isAssignable(value.getClass(), itemType)) {
                return false;
            }
            return collection.stream().anyMatch(item -> isEqual(item, itemType.cast(value)));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object value) {
            if (value == null) {
                return false;
            }
            if (!(value instanceof ProxySet)) {
                return value.equals(this);
            }
            return ((ProxySet) value).size() == size() && value.hashCode() == hashCode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            int code = HASH_SEED;
            for (Object item : collection) {
                if (item == null) {
                    continue;
                }
                code = HASH_FACTOR * getHashCode(item);
            }
            return code;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Iterator<Object> iterator() {
            return collection.iterator();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int size() {
            return collection.size();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean remove(Object value) {
            if (value == null) {
                return super.remove(null);
            }
            if (!ClassUtils.isAssignable(value.getClass(), itemType)) {
                return false;
            }
            Iterator<Object> it = iterator();
            while (it.hasNext()) {
                if (isEqual(it.next(), itemType.cast(value))) {
                    it.remove();
                    return true;
                }
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return collection
                .stream()
                .map(this::toString)
                .collect(Collectors.joining(CoreConstants.SEPARATOR_COMMA));
        }

        /**
         * Retrieves the string representation of the given object provided it cannot return a proper value from its own
         * {@code .toString()} method
         * @param value An arbitrary object, typically an item in the collection
         * @return String value, non-null
         */
        private String toString(Object value) {
            StringBuilder result = new StringBuilder();
            for (Method method : getInstanceMethods()) {
                Object invocationResult = getInvocationResult(method, value);
                if (invocationResult != null) {
                    result
                        .append(result.length() > 0 ? CoreConstants.SEPARATOR_COMMA : StringUtils.EMPTY)
                        .append(method.getName())
                        .append(CoreConstants.EQUALITY_SIGN)
                        .append(invocationResult);
                }
            }
            return result.toString();
        }

        /**
         * Creates a hash code for the given object provided it cannot return a proper hash code itself
         * @param value An arbitrary object, typically an item in the collection
         * @return Integer value
         */
        private int getHashCode(Object value) {
            int code = HASH_SEED;
            for (Method method : getInstanceMethods()) {
                Object invocationResult = getInvocationResult(method, value);
                if (invocationResult != null) {
                    code = HASH_FACTOR * code + invocationResult.hashCode();
                } else {
                    code = HASH_FACTOR * code;
                }
            }
            return code;
        }

        /**
         * Calculates whether the given objects are equal judging by their hash codes. It is implied that objects cannot
         * return a proper result of calling the {@code .equals()} method themselves
         * @param first  An arbitrary object, typically an item in the collection
         * @param second An arbitrary object, typically an item in the collection
         * @return True or false
         */
        private boolean isEqual(Object first, Object second) {
            return getHashCode(first) == getHashCode(second);
        }

        /**
         * Retrieves a list of own public instance methods that an item of the current set has
         * @return A non-null {@code List} instance; can be empty
         */
        private List<Method> getInstanceMethods() {
            return Arrays.stream(itemType.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()) && !Modifier.isStatic(method.getModifiers()))
                .filter(method -> method.getParameterCount() == 0)
                .filter(method -> Arrays
                    .stream(Object.class.getDeclaredMethods())
                    .noneMatch(objectMethod -> objectMethod.getName().equals(method.getName())))
                .collect(Collectors.toList());
        }

        /**
         * Invokes the given {@link Method} on the provided object. Handles possible exceptions internally
         * @param method {@code Method} instance
         * @param value  The object the method belongs to
         * @return A nullable value
         */
        private static Object getInvocationResult(Method method, Object value) {
            try {
                return method.invoke(value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                LOG.warn("Could not invoke {}#{}", value.getClass().getName(), method.getName(), e);
            }
            return null;
        }
    }
}
