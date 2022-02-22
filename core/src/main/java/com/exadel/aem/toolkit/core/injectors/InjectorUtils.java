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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains common methods for use with the bundled {@code Injector} components
 */
class InjectorUtils {

    private static final Logger LOG = LoggerFactory.getLogger(InjectorUtils.class);

    /**
     * Default (instantiation-restricting) constructor
     */
    private InjectorUtils() {
    }

    /**
     * Retrieves a {@link SlingHttpServletRequest} instance from the provided adaptable object
     * @param adaptable The object which Sling tries to adapt from
     * @return {@code SlingHttpServletRequest} object if adaptable is of an appropriate type, or null
     */
    public static SlingHttpServletRequest getRequest(Object adaptable) {
        if (adaptable instanceof SlingHttpServletRequest) {
            return (SlingHttpServletRequest) adaptable;
        }
        return null;
    }

    /**
     * Retrieves a {@link Resource} instance from the provided adaptable object if it is assignable from
     * {@code SlingHttpServletRequest} or {@code Resource}
     * @param adaptable The object which Sling tries to adapt from
     * @return {@code Resource} object if the {@code adaptable} is of an appropriate type, or null
     */
    public static Resource getResource(Object adaptable) {
        if (adaptable instanceof SlingHttpServletRequest) {
            return ((SlingHttpServletRequest) adaptable).getResource();
        }
        if (adaptable instanceof Resource) {
            return (Resource) adaptable;
        }
        return null;
    }

    /**
     * Retrieves a {@link ResourceResolver} instance from the provided adaptable object if it is assignable from
     * {@code Resource} or {@code SlingHttpServletRequest}
     * @param adaptable The object which Sling tries to adapt from
     * @return {@code ResourceResolver} object if the {@code adaptable} is of an appropriate type, or null
     */
    public static ResourceResolver getResourceResolver(Object adaptable) {
        ResourceResolver resolver = null;
        if (adaptable instanceof Resource) {
            resolver = ((Resource) adaptable).getResourceResolver();
        } else if (adaptable instanceof SlingHttpServletRequest) {
            resolver = ((SlingHttpServletRequest) adaptable).getResourceResolver();
        }
        return resolver;
    }

    /**
     * Retrieves the {@code ValueMap} instance from the provided adaptable if it is of type {@code SlingHttpServletRequest},
     * or {@code Resource}, or else {@code ValueMap}
     * @param adaptable The object which Sling tries to adapt from
     * @return Data-filled {@code ValueMap} if adaptation was successful. Otherwise, an empty {@code ValueMap} is returned
     */
    public static ValueMap getValueMap(Object adaptable) {
        ValueMap result = null;
        if (adaptable instanceof SlingHttpServletRequest) {
            result = ((SlingHttpServletRequest) adaptable).getResource().getValueMap();
        } else if (adaptable instanceof Resource) {
            result = ((Resource) adaptable).getValueMap();
        } else if (adaptable instanceof ValueMap) {
            result = (ValueMap) adaptable;
        }
        if (result != null) {
            return result;
        }
        return ValueMap.EMPTY;
    }

    /**
     * Retrieves whether the provided {@code Type} of a Java class member is a parametrized collection type, and its
     * parameter type matches the list of allowed value types
     * @param value              {@code Type} object
     * @param allowedMemberTypes {@code Class} objects representing allowed value types
     * @return True or false
     */
    public static boolean isValidCollection(Type value, Class<?>... allowedMemberTypes) {
        if (!(value instanceof ParameterizedType)
            || !ClassUtils.isAssignable((Class<?>) ((ParameterizedType) value).getRawType(), Collection.class)) {
            return false;
        }
        Class<?> componentType = (Class<?>) ((ParameterizedType) value).getActualTypeArguments()[0];
        return isValidObjectType(componentType, allowedMemberTypes);
    }

    /**
     * Retrieves whether the provided {@code Type} of a Java class member is a parametrized Map type and its
     * parameter type matches the list of allowed value types
     * @param value              {@code Type} object
     * @param allowedMemberTypes {@code Class} objects representing allowed value types
     * @return True or false
     */
    public static boolean isValidMap(Type value, Class<?>... allowedMemberTypes) {
        if (!(value instanceof ParameterizedType)
            || !ClassUtils.isAssignable((Class<?>) ((ParameterizedType) value).getRawType(), Map.class)) {
            return false;
        }
        Class<?> componentType = (Class<?>) ((ParameterizedType) value).getActualTypeArguments()[1];
        return isValidObjectType(componentType, allowedMemberTypes);
    }

    /**
     * Retrieves whether the provided {@code Type} of a Java class member is a parameterized type and checks
     * if the specified raw type is compatible with the {@code allowedType} parameter
     * @param value       {@code Type} object
     * @param allowedType {@code Class} object representing the allowed type
     * @return True or false
     */
    public static boolean isValidRawType(Type value, Class<?> allowedType) {
        if (!(value instanceof ParameterizedType)) {
            return false;
        }
        return ClassUtils.isAssignable((Class<?>) ((ParameterizedType) value).getRawType(), allowedType);
    }

    /**
     * Gets whether the provided {@code Type} of a Java class member is a parametrized collection type and checks whether
     * its parameter type matches the list of allowed value types
     * @param value              {@code Type} object
     * @param allowedMemberTypes {@code Class} objects representing allowed value types
     * @return True or false
     */
    public static boolean isValidArray(Type value, Class<?>... allowedMemberTypes) {
        if (!(value instanceof Class<?>) || !((Class<?>) value).isArray()) {
            return false;
        }
        Class<?> componentType = ((Class<?>) value).getComponentType();
        return isValidObjectType(componentType, allowedMemberTypes);
    }

    /**
     * Retrieves whether the provided {@code Type} of a Java class member is eligible for injection
     * @param value        {@code Type} object
     * @param allowedTypes {@code Class} objects representing allowed value types
     * @return True or false
     */
    public static boolean isValidObjectType(Type value, Class<?>... allowedTypes) {
        if (!(value instanceof Class<?>)) {
            return false;
        }
        return isValidObjectType((Class<?>) value, allowedTypes);
    }

    /**
     * Retrieves whether the provided {@code Class} representing the type of a Java class member is eligible for injection
     * @param value        {@code Class} object
     * @param allowedTypes {@code Class} objects representing allowed value types
     * @return True or false
     */
    private static boolean isValidObjectType(Class<?> value, Class<?>... allowedTypes) {
        if (ArrayUtils.isEmpty(allowedTypes)) {
            return true;
        }
        return Arrays.asList(allowedTypes).contains(value) || value.equals(Object.class);
    }

    /**
     * Retrieves whether the provided {@code Type} of a Java class member is a parametrized collection
     * @param type {@code Type}
     * @return True of false
     */
    public static boolean isCollectionType(Type type) {
        return type instanceof ParameterizedType
            && ClassUtils.isAssignable((Class<?>) ((ParameterizedType) type).getRawType(), Collection.class);
    }

    /**
     * Retrieves actual type parameter from parameterized type
     * @param parameterizedType {@code Class} that represents a parameterized type
     * @return {@code Class} representing the actual type arguments to this type
     */
    public static Class<?> extractParameterType(ParameterizedType parameterizedType) {
        return (Class<?>) parameterizedType.getActualTypeArguments()[0];
    }

    /**
     * Creates and initializes a new instance of a class
     * @param instanceClass Represent a {@code Class} to be initialized
     * @param <T>           Parameterized type
     * @return <T> initialized object instance
     */
    public static <T> T getObjectInstance(Class<? extends T> instanceClass) {
        try {
            return instanceClass.getConstructor().newInstance();
        } catch (InstantiationException
            | IllegalAccessException
            | InvocationTargetException
            | NoSuchMethodException ex) {
            LOG.error("Could not initialize object {}", instanceClass.getName(), ex);
        }
        return null;
    }

    /**
     * Creates new {@code Resource} that contains filtered properties from {@code currentResource}
     * @param currentResource Current {@code Resource} contains properties to be filtered
     * @param predicates      {@code List<Predicate<String>>} that contains filters for properties
     * @return New created {@code Resource} with filtered properties if success, otherwise null is returned
     */
    public static Resource createFilteredResource(Resource currentResource, List<Predicate<String>> predicates) {
        Map<String, Object> values = currentResource
            .getValueMap()
            .entrySet()
            .stream()
            .filter(item -> predicates.stream().anyMatch(f -> f.test(item.getKey())))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        try {
            return currentResource
                .getResourceResolver()
                .create(currentResource, ResourceUtil.createUniqueChildName(currentResource, "item"), values);
        } catch (PersistenceException ex) {
            LOG.debug("Cannot create new resource", ex);
        }

        return null;
    }

    /**
     * Retrieves predicates list according to prefix and postfix arguments.
     * If prefix and postfix arguments are empty default predicate is added
     * @param prefix  String argument
     * @param postfix String argument
     * @return List of predicates
     */
    public static List<Predicate<String>> getPropertiesPredicates(String prefix, String postfix) {
        List<Predicate<String>> predicates = new ArrayList<>();

        if (StringUtils.isNotBlank(prefix)) {
            predicates.add(value -> value.startsWith(prefix));
        }

        if (StringUtils.isNotBlank(postfix)) {
            predicates.add(value -> value.endsWith(postfix));
        }

        if (predicates.isEmpty()) {
            predicates.add(value -> true);
        }

        return predicates;
    }
}
