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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

/**
 * Contains common methods for use with the bundled {@code Injector} components
 */
class InjectorUtils {

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
     * @return {@code Resource} object if adaptable is of an appropriate type, or null
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
     * @return {@code ResourceResolver} object if adaptable is of an appropriate type, or null
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
     * Retrieves whether the provided {@code Type} of a Java class member is a parameterized collection type and its
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
     * Retrieves whether the provided {@code Type} of a Java class member is a parameterized collection type and checks
     * if its raw type matches the list of allowed value types
     * @param type         {@code Type} object
     * @param allowedTypes {@code Class} objects representing allowed value types
     * @return True or false
     */
    public static boolean isValidCollectionRawType(Type type, Class<?>... allowedTypes) {
        if (!(type instanceof ParameterizedType)
            || !ClassUtils.isAssignable((Class<?>) ((ParameterizedType) type).getRawType(), Collection.class)) {
            return false;
        }
        Class<?> collectionType = (Class<?>) ((ParameterizedType) type).getRawType();
        return isValidObjectType(collectionType, allowedTypes);
    }

    /**
     * Retrieves whether the provided {@code Type} of a Java class member is a parameterized collection type
     * and checks if its raw type equals Map and the parameter type matches the list of allowed value types
     * @param type               {@code Type} object
     * @param allowedTypesOfKeys {@code Class} objects representing allowed value types
     * @return True or false
     */
    public static boolean isValidMapKeyType(Type type, Class<?>... allowedTypesOfKeys) {
        if (!(type instanceof ParameterizedType)) {
            return false;
        }
        Class<?> actualMapKey = (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
        return ((ParameterizedType) type).getRawType().equals(Map.class)
            && isValidObjectType(actualMapKey, allowedTypesOfKeys);
    }

    /**
     * Retrieves whether the provided {@code Type} of a Java class member is a parameterized collection type and its
     * parameter type matches the list of allowed value types
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
}
