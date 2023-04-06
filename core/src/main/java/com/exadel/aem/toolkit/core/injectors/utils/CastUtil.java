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

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.exadel.aem.toolkit.core.CoreConstants;

/**
 * Contains utility methods for casting value types involved in Sing injectors processing.
 * <p><u>Note</u>: This class is not a part of the public API and is subject to change. Do not use it in your own
 * code</p>
 */
public class CastUtil {

    private static final String SIGN_DOUBLE = "d";

    /**
     * Default (instantiation-restricting) constructor
     */
    private CastUtil() {
    }

    /**
     * Attempts to cast the given value to the provided {@code Type}. Designed for use with Sling injectors
     * @param value     An arbitrary value
     * @param type      A {@link Type} reference; usually reflects the type of Java class member
     * @return Transformed value; returns null in case type casting is not possible
     */
    public static Object toType(Object value, Type type) {
        return toType(value, type, CastUtil::toInstanceOfType);
    }

    /**
     * Attempts to cast the given value to the provided {@code Type}. Designed for use with Sling injectors
     * @param value     An arbitrary value
     * @param type      A {@link Type} reference; usually reflects the type of Java class member
     * @param converter A function that we use to convert a single entry of the provided value into the given type.
     *                  Considering the given value is an array or collection, the {@code converter} must provide
     *                  processing for every separate array's (collection's) element. If the given value is a singleton
     *                  object or primitive, the {@code converter} processes it as is
     * @return Transformed value; returns null in case type casting is not possible
     */
    public static Object toType(Object value, Type type, BiFunction<Object, Type, Object> converter) {
        if (value == null) {
            return null;
        }

        Class<?> elementType = TypeUtil.getElementType(type);

        if (TypeUtil.isArray(type)) {
            return toArray(value, elementType, converter);
        }

        if (TypeUtil.isSupportedCollection(type, true)) {
            return Set.class.equals(TypeUtil.getRawType(type))
                ? toCollection(value, elementType, converter, LinkedHashSet::new)
                : toCollection(value, elementType, converter, ArrayList::new);
        }

        if (Object.class.equals(type)) {
            return converter.apply(value, type);
        }
        return converter.apply(extractFirstElement(value), type);
    }

    /**
     * Transforms the given value into an array of the given type. If the source value is not array-like, a singleton
     * array is created
     * @param value     An arbitrary non-null value
     * @param type      {@link Type} reference; usually reflects the type of Java class member
     * @param converter A function that we use to convert a single entry of the provided value into the given type
     * @return A non-null array instance; contains nullable entries
     */
    private static Object toArray(Object value, Class<?> type, BiFunction<Object, Type, Object> converter) {
        int length = 1;
        if (value.getClass().isArray()) {
            length = Array.getLength(value);
        } else if (value instanceof Collection) {
            length = ((Collection<?>) value).size();
        }
        Object result = Array.newInstance(type, length);
        if (value.getClass().isArray()) {
            for (int i = 0; i < length; i++) {
                Array.set(result, i, toType(Array.get(value, i), type, converter));
            }
        } else if (value instanceof Collection) {
            int i = 0;
            for (Object entry : (Collection<?>) value) {
                Array.set(result, i++, converter.apply(entry, type));
            }
        } else {
            Array.set(result, 0, converter.apply(value, type));
        }
        return result;
    }

    /**
     * Transforms the given value into a {@code List} or a {@code Set} containing entries of the given type
     * @param value     An arbitrary non-null value
     * @param type      {@link Type} reference; usually reflects the type of Java class member
     * @param converter A function that we use to convert a single entry of the provided value into the given type
     * @param factory   A reference to the routine that produces a particular {@code List} or {@code Set} instance
     * @return A non-null {@code List} or {@code Set} instance; contains nullable entries
     */
    private static Object toCollection(
        Object value,
        Class<?> type,
        BiFunction<Object, Type, Object> converter,
        Supplier<Collection<Object>> factory) {

        Collection<Object> result = factory.get();
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                result.add(converter.apply(Array.get(value, i), type));
            }
        } else if (value instanceof Collection) {
            for (Object entry : (Collection<?>) value) {
                result.add(converter.apply(entry, type));
            }
        } else {
            result.add(converter.apply(value, type));
        }
        return result;
    }

    /**
     * This method is used to "flatten" the given argument value. If the argument is an array or collection, the first
     * element is returned. Otherwise, the given value itself is returned
     * @param source The object to extract an element from; a non-null value is expected
     * @return Object reference
     */
    private static Object extractFirstElement(Object source) {
        if (source.getClass().isArray() && Array.getLength(source) > 0) {
            return Array.get(source, 0);
        } else if (TypeUtil.isSupportedCollection(source.getClass(), false)
            && !IterableUtils.isEmpty((Collection<?>) source)) {
            Iterator<?> iterator = ((Collection<?>) source).iterator();
            if (iterator.hasNext()) {
                return iterator.next();
            }
        }
        return source;
    }

    /**
     * Called by {@code CastUtil#toType} to further adapt the passed value to the given type if there is type
     * compatibility. E.g., when an {@code int} value is passed, and the receiving type is {@code long}, or else the
     * passed value is an implementation of an interface, and the receiving type is the interface itself. The adaptation
     * is made to increase the probability of successful value injection
     * @param value An arbitrary non-null value
     * @param type  {@link Type} reference. When passed to this method, {@code type} is expected to be singular (not an
     *              array or collection)
     * @return Usually, a transformed value. Returns an original one if type casting is not possible or not needed
     */
    private static Object toInstanceOfType(Object value, Type type) {
        if (value == null || value.getClass().equals(type) || type instanceof ParameterizedType) {
            return value;
        }
        assert type instanceof Class<?>;

        if ((StringUtils.equalsIgnoreCase(value.toString(), Boolean.TRUE.toString())
            || StringUtils.equalsIgnoreCase(value.toString(), Boolean.FALSE.toString()))
            && ClassUtils.primitiveToWrapper((Class<?>) type).equals(Boolean.class)) {
            return Boolean.parseBoolean(value.toString().toLowerCase());
        }
        Object effectiveValue = value;
        if (value instanceof String && NumberUtils.isCreatable(value.toString())) {
            String stringifiedValue = value.toString();
            if (stringifiedValue.contains(CoreConstants.SEPARATOR_DOT) && !stringifiedValue.endsWith(SIGN_DOUBLE)) {
                stringifiedValue += SIGN_DOUBLE;
            }
            effectiveValue = NumberUtils.createNumber(stringifiedValue);
        }
        if (ClassUtils.isPrimitiveOrWrapper(effectiveValue.getClass()) && ClassUtils.isPrimitiveOrWrapper((Class<?>) type)) {
            return toNumeric(effectiveValue, type);
        }
        if (ClassUtils.isAssignable(effectiveValue.getClass(), (Class<?>) type)) {
            return ((Class<?>) type).cast(effectiveValue);
        }
        return getDefaultValue(type);
    }

    /**
     * Adapts the passed primitive or boxed numeric value to the given type. E.g., when an {@code int} value is passed
     * and the receiving type is {@code long}
     * @param value An arbitrary object
     * @param type  {@link Type} reference; usually reflects the type of Java class member
     * @return Usually, a transformed value. Returns an original one if type casting is not possible, or {@code 0} if
     * {@code null} was passed
     */
    private static Object toNumeric(Object value, Type type) {
        if (ClassUtils.isAssignable((Class<?>) type, Integer.class)) {
            return value != null ? toInt(value) : 0;
        }
        if (ClassUtils.isAssignable((Class<?>) type, Long.class)) {
            return value != null ? toLong(value) : 0L;
        }
        if (ClassUtils.isAssignable((Class<?>) type, Double.class)) {
            return value != null ? toDouble(value) : 0d;
        }
        return value;
    }

    /**
     * Attempts to cast the given value to {@code int}
     * @param value An arbitrary non-null value. Java primitive number or a boxed number is expected
     * @return A transformed value or {@code 0}
     */
    private static int toInt(Object value) {
        if (ClassUtils.isAssignable(value.getClass(), Integer.class)) {
            return (int) value;
        }
        if (ClassUtils.isAssignable(value.getClass(), Byte.class)) {
            return ((Byte) value).intValue();
        }
        if (ClassUtils.isAssignable(value.getClass(), Short.class)) {
            return ((Short) value).intValue();
        }
        if (ClassUtils.isAssignable(value.getClass(), Long.class)) {
            return ((Long) value).intValue();
        }
        if (ClassUtils.isAssignable(value.getClass(), Float.class)) {
            return ((Float) value).intValue();
        }
        if (ClassUtils.isAssignable(value.getClass(), Double.class)) {
            return ((Double) value).intValue();
        }
        return 0;
    }

    /**
     * Attempts to cast the given value to {@code long}
     * @param value An arbitrary non-null value. Java primitive number or a boxed number is expected
     * @return A transformed value or {@code 0}
     */
    private static long toLong(Object value) {
        if (ClassUtils.isAssignable(value.getClass(), Long.class)) {
            return (long) value;
        }
        if (ClassUtils.isAssignable(value.getClass(), Double.class)) {
            return ((Double) value).longValue();
        }
        return toInt(value);
    }

    /**
     * Attempts to cast the given value to {@code double}
     * @param value An arbitrary non-null value, a Java primitive number, or a boxed number is expected
     * @return A transformed value or {@code 0}
     */
    private static double toDouble(Object value) {
        if (ClassUtils.isAssignable(value.getClass(), Double.class)) {
            return (double) value;
        }
        if (ClassUtils.isAssignable(value.getClass(), Float.class)) {
            return ((Float) value).doubleValue();
        }
        if (ClassUtils.isAssignable(value.getClass(), Long.class)) {
            return ((Long) value).doubleValue();
        }
        return toInt(value);
    }

    /**
     * Retrieves the default value for the given Java type by creating an empty array of that type. The return value is
     * going to be {@code 0} for the numeric fields, {@code false} for the booleans fields, and {@code null} for the
     * reference types
     * @param type The type of the field
     * @return The default value for the type
     */
    private static Object getDefaultValue(Type type) {
        return Array.get(Array.newInstance((Class<?>) type, 1), 0);
    }
}
