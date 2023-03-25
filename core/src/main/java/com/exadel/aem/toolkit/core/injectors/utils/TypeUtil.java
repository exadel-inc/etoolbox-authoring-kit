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
package com.exadel.aem.toolkit.core.injectors.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.factory.ModelFactory;

/**
 * Contains utility methods for validating types of entities involved in Sing injectors processing.
 * <p><u>Note</u>: This class is not a part of the public API and is subject to change. Do not use it in your own
 * code</p>
 */
public class TypeUtil {

    /**
     * Default (instantiation-restricting) constructor
     */
    private TypeUtil() {
    }

    /**
     * Retrieves the {@code Class<?>} object that represents either the parameter type of collection or the element type
     * of array. If the passed {@code type} cannot be treated as either array or collection, {@code null} is returned
     * @param value {@code Type} reference that characterizes the current class member
     * @return {@code Class} object, or null
     */
    public static Class<?> getElementType(Type value) {
        if (value instanceof Class<?> && ((Class<?>) value).isArray()) {
            return ((Class<?>) value).getComponentType();
        }
        Type currentType = value;
        while (currentType != null) {
            if (currentType instanceof ParameterizedType && ((ParameterizedType) currentType).getActualTypeArguments()[0] != null) {
                return (Class<?>) ((ParameterizedType) currentType).getActualTypeArguments()[0];
            }
            if (!(currentType instanceof Class<?>)) {
                break;
            }
            currentType = ((Class<?>) currentType).getGenericSuperclass();
        }
        return value instanceof Class<?> ? (Class<?>) value : null;
    }

    public static Class<?> getRawType(Type value) {
        if (value instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) value).getRawType();
        } else if (value instanceof Class<?>) {
            return (Class<?>) value;
        }
        return null;
    }

    /**
     * Gets whether the provided {@code Type} of Java class member is a parametrized collection type and checks whether
     * its parameter type matches the list of allowed value types
     * @param value              {@code Type} object
     * @param allowedMemberTypes {@code Class} objects representing allowed value types
     * @return True or false
     */
    public static boolean isValidArray(Type value, Class<?>... allowedMemberTypes) {
        if (!(value instanceof Class<?>) || !((Class<?>) value).isArray()) {
            return false;
        }
        if (ArrayUtils.isEmpty(allowedMemberTypes)) {
            return true;
        }
        Class<?> componentType = ((Class<?>) value).getComponentType();
        return isValidObjectType(componentType, allowedMemberTypes);
    }

    /**
     * Gets whether the provided {@code Type} object represents a collection such as {@link List} or {@link Set}
     * @param value   {@code Type} object
     * @return True or false
     */
    public static boolean isValidCollectionType(Type value) {
        if (!(value instanceof Class<?>) && !(value instanceof ParameterizedType)) {
            return false;
        }
        Class<?> effectiveClass = getRawType(value);
        return ClassUtils.isAssignable(effectiveClass, List.class) || ClassUtils.isAssignable(effectiveClass, Set.class);
    }

    /**
     * Retrieves whether the provided {@code Type} of Java class member is a parametrized collection type and its
     * parameter type matches the list of allowed value types
     * @param value              {@code Type} object
     * @param allowedMemberTypes {@code Class} objects representing allowed entry types
     * @return True or false
     */
    public static boolean isValidCollection(Type value, Class<?>... allowedMemberTypes) {
        if (!isValidCollectionType(value)) {
            return false;
        }
        if (ArrayUtils.isEmpty(allowedMemberTypes)) {
            return true;
        }
        Class<?> elementType = getElementType(value);
        return isValidObjectType(elementType, allowedMemberTypes);
    }

    /**
     * Retrieves whether the provided {@code Type} of Java class member is a parametrized {@code Map} type and its
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
     * Retrieves whether the provided {@code Type} of Java class member is a parameterized type and checks if the
     * specified raw member type is compatible with the {@code allowedType} parameter
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
     * Gets whether the provided {@code Type} of Java class member is eligible for injection
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
     * Gets whether the provided {@code Class} representing the type of Java class member is eligible for injection
     * @param value        {@code Class} object
     * @param allowedTypes {@code Class} objects representing allowed value types
     * @return True or false
     */
    private static boolean isValidObjectType(Class<?> value, Class<?>... allowedTypes) {
        if (value == null) {
            return false;
        }
        if (value.equals(Object.class) || ArrayUtils.isEmpty(allowedTypes)) {
            return true;
        }
        return Arrays
            .stream(allowedTypes)
            .anyMatch(type -> ClassUtils.isAssignable(value, type));
    }

    /**
     * Gets whether the given {@code type} is a {@code SlingHttpServletRequest} adapter
     * @param modelFactory {@link ModelFactory} instance
     * @param type         Type of injectable
     * @return True or false
     */
    public static boolean isSlingRequestAdapter(ModelFactory modelFactory, Type type) {
        if (!(type instanceof Class<?>)) {
            return false;
        }
        Class<?> modelClass = (Class<?>) type;
        return isSlingRequestAdapter(modelFactory, modelClass);
    }

    /**
     * Gets whether the given {@code type} is a {@code SlingHttpServletRequest} adapter
     * @param modelFactory {@link ModelFactory} instance
     * @param type         {@code Class<?>} object representing the type of injectable
     * @return True or false
     */
    public static boolean isSlingRequestAdapter(ModelFactory modelFactory, Class<?> type) {
        if (!modelFactory.isModelClass(type)) {
            return false;
        }
        return ArrayUtils.contains(
            type.getAnnotation(Model.class).adaptables(),
            SlingHttpServletRequest.class);
    }
}
