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

package com.exadel.aem.toolkit.core.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.widgets.datepicker.DateTimeValue;
import com.exadel.aem.toolkit.core.exceptions.ReflectionException;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;

/**
 * Contains utility methods to perform {@code annotation - to - plain Java object} and {@code annotation - to - annotation}
 * value conversions for the sake of proper dialog markup rendering
 */
public class PluginObjectUtility {

    private static final String MEMBER_VALUES = "memberValues";

    private PluginObjectUtility() {}

    /**
     * Creates in runtime a {@code <T extends Annotation>}-typed annotation-like proxy object to mimic the behavior of
     * an actual annotation. Values to annotation methods are provided as a {@code Map<String, Object>} collection
     * @param type Target annotation type
     * @param values Values that map to the target annotation's fields
     * @param <T> Particular type of the annotation facade
     * @return Facade annotation instance, a subtype of the {@code Annotation} class
     */
    public static <T extends Annotation> T create(Class<T> type, Map<String, Object> values) {
        Map<String, BiFunction<Annotation, Object[], Object>> methods = new HashMap<>();
        Arrays.stream(type.getDeclaredMethods())
                .forEach(method -> {
                    BiFunction<Annotation, Object[], Object> methodFunction =  (src, args) ->
                            values != null && values.containsKey(method.getName())
                                    ? values.get(method.getName())
                                    : method.getDefaultValue();
                    methods.put(method.getName(),methodFunction);
                });
        return modify(null, type, methods);
    }

    /**
     * Creates at runtime a {@code <T extends Annotation>}-typed facade for the specified annotation with only some fields
     * set to a non-default value, others voided
     * This method is used to render the same {@code Annotation} object differently (like two or more different sets of values)
     * depending on the values specified
     * @param source {@code Annotation} instance to produce a facade for
     * @param type Target {@code Class} of the facade (one of subtypes of the {@code Annotation} class)
     * @param voidedFields The fields to be voided, a list of non-blank Strings
     * @param <T> Particular type of the annotation facade
     * @return Facade annotation instance, a subtype of the {@code Annotation} class
     */
    public static <T extends Annotation> T filter(Annotation source, Class<T> type, List<String> voidedFields) {
        Map<String, BiFunction<Annotation, Object[], Object>> methods = new HashMap<>();
        if (voidedFields != null) {
            voidedFields.stream()
                    .map(methodName -> getMethodInstance(type, methodName))
                    .filter(Objects::nonNull)
                    .forEach(method -> methods.put(method.getName(),
                            (annotation, args) -> method.getReturnType().equals(String.class) ? StringUtils.EMPTY : 0L));
        }
        return modify(source, type, methods);
    }

    /**
     * Creates at runtime a {@code <T extends Annotation>}-typed facade for the specified annotation with only some fields
     * set to a non-default value, others voided
     * This method is used to render the same {@code Annotation} object differently (like two or more different sets of values)
     * depending on the values specified
     * @param source {@code Annotation} instance to produce a facade for
     * @param type Target {@code Class} of the facade (one of subtypes of the {@code Annotation} class)
     * @param name String representing the method to check for default value, non-blank
     * @param value Fallback value
     * @param <T> Particular type of the annotation facade
     * @param <R> Return type of a fallback method / methods
     * @return Facade annotation instance, a subtype of the {@code Annotation} class
     */
    public static <T extends Annotation, R> T modifyIfDefault(Annotation source, Class<T> type, String name, R value) {
        return modifyIfDefault(source, type, Collections.singletonMap(name, value));
    }

    /**
     * Creates at runtime a {@code <T extends Annotation>}-typed facade for the specified annotation with only some fields
     * set to a non-default value, others voided
     * This method is used to render the same {@code Annotation} object differently (like two or more different sets of values)
     * depending on the values specified
     * @param source {@code Annotation} instance to produce a facade for
     * @param type Target {@code Class} of the facade (one of subtypes of the {@code Annotation} class)
     * @param fallbackValues {@code Map<String, Object>} representing values to be exposed by the annotation methods
     *                                                  whether they possess their default values
     * @param <T> Particular type of the annotation facade
     * @param <R> Return type of a fallback method / methods
     * @return Facade annotation instance, a subtype of the {@code Annotation} class
     */
    private static <T extends Annotation, R> T modifyIfDefault(Annotation source, Class<T> type, Map<String, R> fallbackValues) {
        Map<String, BiFunction<Annotation, Object[], Object>> methods = new HashMap<>();
        if (fallbackValues != null) {
            fallbackValues.keySet().stream()
                    .map(methodName -> getMethodInstance(type, methodName))
                    .filter(Objects::nonNull)
                    .filter(method -> !PluginReflectionUtility.annotationPropertyIsNotDefault(source, method))
                    .forEach(method -> methods.put(method.getName(),
                            (annotation, args) -> fallbackValues.get(method.getName())));
        }
        return modify(source, type, methods);
    }

    /**
     * Creates Java {@code Temporal} from {@code DateTimeValue} annotation
     * @param obj {@code DateTimeValue} annotation instance
     * @return Canonical Temporal instance, or null
     */
    public static Temporal getDateTimeInstance(Object obj) {
        DateTimeValue dt = (DateTimeValue) obj;
        try {
            if (StringUtils.isNotBlank(dt.timezone())) { // the following induces exception if any of DateTime parameters is invalid
                return ZonedDateTime.of(dt.year(), dt.month(), dt.day(), dt.hour(), dt.minute(), 0, 0, ZoneId.of(dt.timezone()));
            }
            return LocalDateTime.of(dt.year(), dt.month(), dt.day(), dt.hour(), dt.minute());
        } catch (DateTimeException e) {
            return null;
        }
    }

    /**
     * Extends an object in runtime by casting it to an extension interface and applying additional methods
     * @param value Source object, typically final or one coming from outside the user scope
     * @param modification {@code Class} object representing an interface that {@code value} implements,
     *                                        or an extending interfaca of such
     * @param methods {@code Map<String, Function>} of named routines that represent the new and/or modified methods
     *                                             that {@code value} must expose. Each routine accepts a source object,
     *                                             and a variadic array of {@code Object}-typed arguments
     * @param <T> The {@code Class} of the source object
     * @param <U> The interface exposing modified methods that the {@code value} implements, or an interface bearing
     *           newly added methods that extends one of the interfaces implemented by {@code value}
     * @param <R> The return type of extension methods. Must be fallen back to {@code Object} type of a narrower generic
     *           type in case returns are to be differently typed
     * @return The {@code <U>}-typed extension object, or else null if {@code modification} is null, or else
     * the {@code methods} map is empty
     */
    private static <T, U, R> U modify(T value, Class<U> modification, Map<String, BiFunction<T, Object[], R>> methods) {
        if (modification == null) {
            return null;
        }
        if (methods == null || methods.isEmpty()) {
            try {
                return modification.cast(value);
            } catch (ClassCastException e) {
                return null;
            }
        }
        Object result = Proxy.newProxyInstance(modification.getClassLoader(),
                new Class[] {modification},
                new ExtensionInvocationHandler<>(value, modification, methods));
        return modification.cast(result);
    }

    /**
     * Wraps up extracting method from an {@code Annotation} signature by name, with {@link NoSuchMethodException} handled
     * @param annotationType Target {@code Class} of the facade (one of subtypes of the {@code Annotation} class)
     * @param name Method name, non-blank String expected
     * @return The {@link Method} instance, or null
     */
    private static Method getMethodInstance(Class<? extends Annotation> annotationType, String name) {
        try {
            return annotationType.getDeclaredMethod(name);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * Implements {@link InvocationHandler} mechanism for creating object extension
     * as per {@link PluginObjectUtility#modify(Object, Class, Map)}
     * @param <T> The {@code Class} of the source object
     * @param <R> The return type of the extension methods defined for this instance
     */
    private static class ExtensionInvocationHandler<T, U, R> implements InvocationHandler {
        private static final String METHOD_TO_STRING = "toString";
        private static final String METHOD_ANNOTATION_TYPE = "annotationType";
        private static final String INVOCATION_EXCEPTION_MESSAGE_TEMPLATE = "Could not invoke method '%s' on object %s";

        private T source;
        private Class<U> targetType;
        private Map<String, BiFunction<T, Object[], R>> extensionMethods;

        /**
         * Class constructor
         * @param source The object to extend
         * @param extensionMethods {@code Map} composed of extension method names, String-typed,
         *                         and the extension routines, each a function accepting a source object, and
         *                                    a variadic array of Object-typed arguments
         */
        private ExtensionInvocationHandler(T source, Class<U> targetType, Map<String, BiFunction<T, Object[], R>> extensionMethods) {
            this.source = source;
            this.targetType = targetType;
            this.extensionMethods = extensionMethods;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            if (source == null && method.getName().equals(METHOD_TO_STRING)) {
                return targetType.toString();
            } else if (source == null && method.getName().equals(METHOD_ANNOTATION_TYPE)) {
                return targetType;
            }

            try {
                if (extensionMethods != null && extensionMethods.containsKey(method.getName())) {
                    return extensionMethods.get(method.getName()).apply(this.source, args);
                }
                return source != null ? method.invoke(source, args) : null;
            } catch (IllegalAccessException | InvocationTargetException e) {
                PluginRuntime.context().getExceptionHandler().handle(new ReflectionException(
                        String.format(INVOCATION_EXCEPTION_MESSAGE_TEMPLATE, method.getName(), proxy),
                        e));
            }
            return null;
        }
    }

    public static void changeAnnotationValue(Annotation annotation, String key, Object newValue) {
        try {
            Object handler = Proxy.getInvocationHandler(annotation);
            Field f = handler.getClass().getDeclaredField(MEMBER_VALUES);
            f.setAccessible(true);
            Map<String, Object> memberValues = (Map<String, Object>) f.get(handler);
            memberValues.put(key, newValue);
        } catch (Exception ignored) {
            //ignored
        }
    }
}
