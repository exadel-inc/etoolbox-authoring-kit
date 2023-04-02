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
import java.util.Collection;
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

    /* ----------------------
       Detecting object types
       ---------------------- */

    // Collections

    /**
     * Retrieves whether the provided {@code Type} of Java class member is a parametrized collection type, and its
     * parameter type matches the list of allowed value types
     * @param value  {@code Type} reference that matches a Java class member
     * @param sample {@code Class} object representing the parameter type
     * @return True or false
     */
    public static boolean isSupportedCollectionOfType(Type value, Class<?> sample) {
        if (!isSupportedCollection(value)) {
            return false;
        }
        if (sample == null) {
            return true;
        }
        Class<?> elementType = getElementType(value);
        return ClassUtils.isAssignable(elementType, sample);
    }

    /**
     * Gets whether the provided {@code Type} object represents a collection of type {@link Collection}, {@link List} or
     * {@link Set}
     * @param value {@code Type} object
     * @return True or false
     */
    public static boolean isSupportedCollection(Type value) {
        return isSupportedCollection(value, false);
    }

    /**
     * Gets whether the provided {@code Type} object represents a collection of type {@link Collection}, {@link List} or
     * {@link Set}
     * @param value  {@code Type} object
     * @param strict If set to {@code true}, the {@code value} must be exactly {@code Collection}, {@code List} or
     *               {@code Set}. This flag is useful, e.g., when checking an injection target field for compliance.
     *               Otherwise. the value just needs to be assignable to a collection type
     * @return True or false
     */
    static boolean isSupportedCollection(Type value, boolean strict) {
        if (!(value instanceof Class<?>) && !(value instanceof ParameterizedType)) {
            return false;
        }
        Class<?> effectiveClass = getRawType(value);
        if (strict) {
            return Collection.class.equals(effectiveClass)
                || List.class.equals(effectiveClass)
                || Set.class.equals(effectiveClass);
        }
        return ClassUtils.isAssignable(effectiveClass, Collection.class)
            || ClassUtils.isAssignable(effectiveClass, List.class)
            || ClassUtils.isAssignable(effectiveClass, Set.class);
    }

    // Arrays

    /**
     * Gets whether the provided {@code Type} represents a Java array of the provided type
     * @param value  {@code Type} reference that matches a Java class member
     * @param sample {@code Class} object that the array type must match
     * @return True or false
     */
    public static boolean isArrayOfType(Type value, Class<?> sample) {
        if (!(value instanceof Class<?>) || !((Class<?>) value).isArray()) {
            return false;
        }
        if (sample == null) {
            return true;
        }
        Class<?> thisComponentType = ((Class<?>) value).getComponentType();
        return ClassUtils.isAssignable(thisComponentType, sample);
    }

    /**
     * Gets whether the provided {@code Type} represents a Java array
     * @param value {@code Type} reference that matches a Java class member
     * @return True or false
     */
    static boolean isArray(Type value) {
        return (value instanceof Class<?>) && ((Class<?>) value).isArray();
    }

    /**
     * Gets whether the provided {@code Type} object represents either a collection of type {@link Collection},
     * {@link List} or {@link Set}, or else an array
     * @param value {@code Type} object
     * @return True or false
     */
    public static boolean isSupportedCollectionOrArray(Type value) {
        return isSupportedCollection(value) || isArray(value);
    }

    /**
     * Gets whether the provided {@code Type} object represents either a collection of type {@link Collection},
     * {@link List} or {@link Set}, or else an array of elements having the provided type
     * @param value  {@code Type} object
     * @param sample {@code Class} object that the array type must match
     * @return True or false
     */
    public static boolean isSupportedCollectionOrArrayOfType(Type value, Class<?> sample) {
        return isSupportedCollectionOfType(value, sample) || isArrayOfType(value, sample);
    }

    // Maps

    /**
     * Retrieves whether the provided {@code Type} represents a parametrized {@code Map} type, and its parameter type
     * corresponds to the given argument
     * @param value  {@code Type} reference that matches a Java class member
     * @param sample {@code Class} object that the value type of the map must match
     * @return True or false
     */
    public static boolean isMapOfValueType(Type value, Class<?> sample) {
        if (!(value instanceof ParameterizedType)
            || !ClassUtils.isAssignable((Class<?>) ((ParameterizedType) value).getRawType(), Map.class)) {
            return false;
        }
        Class<?> componentType = (Class<?>) ((ParameterizedType) value).getActualTypeArguments()[1];
        return ClassUtils.isAssignable(componentType, sample);
    }

    /* --------------------
       Extracting type info
       -------------------- */

    /**
     * Retrieves the {@code Class} reference that signifies the "raw" part of a parametrized type entity. E.g., for the
     * {@code List<String>} type, the {@code List} is returned. If the given value is not a parametrized one, the type
     * itself is returned
     * @param value {@code Type} reference that matches a Java class member
     * @return A nullable {@code Class} object. {@code Null} is returned if casting the type to {@code Class} is not
     * possible (e.g., an empty value is provided)
     */
    public static Class<?> getRawType(Type value) {
        if (value instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) value).getRawType();
        } else if (value instanceof Class<?>) {
            return (Class<?>) value;
        }
        return null;
    }

    /**
     * Retrieves the {@code Class<?>} object that represents either the parameter type of supported collection or the
     * array type. If the passed {@code type} cannot be treated as either array or collection, {@code null} is returned
     * @param value {@code Type} reference that matches a Java class member
     * @return A nullable {@code Class} object
     */
    public static Class<?> getElementType(Type value) {
        if (value instanceof Class<?> && ((Class<?>) value).isArray()) {
            return ((Class<?>) value).getComponentType();
        }
        if (!isSupportedCollection(value)) {
            return null;
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

    /* --------------
       Sling adapters
       -------------- */

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
        if (!modelFactory.isModelClass(modelClass)) {
            return false;
        }
        return ArrayUtils.contains(
            modelClass.getAnnotation(Model.class).adaptables(),
            SlingHttpServletRequest.class);
    }
}
