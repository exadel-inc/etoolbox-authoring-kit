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

package com.exadel.aem.toolkit.plugin.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.meta.IgnorePropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.plugin.exceptions.ReflectionException;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.source.Sources;

/**
 * Contains utility methods to perform {@code annotation - to - plain Java object} and {@code annotation - to - annotation}
 * value conversions for the sake of proper dialog markup rendering
 */
public class AnnotationUtil {
    private static final String INVOCATION_EXCEPTION_MESSAGE_TEMPLATE = "Could not invoke method '%s' on %s";

    private static final Predicate<Method> MAP_ALL_PROPERTIES = member -> true;

    /**
     * Default (hiding) constructor
     */
    private AnnotationUtil() {
    }

    /**
     * Retrieves property value of the specified annotation. This method wraps up exception handling, therefore, can be
     * used within functional calls, etc
     * @param annotation The annotation used for value retrieval
     * @param method {@code Method} object representing the annotation's property
     * @return Method invocation result, or null if an internal exception was thrown
     */
    public static Object getProperty(Annotation annotation, Method method) {
        return getProperty(annotation, method, null);
    }

    /**
     * Retrieves property value of the specified annotation. This method wraps up exception handling, therefore, can be
     * used within functional calls, etc
     * @param annotation The annotation used for value retrieval
     * @param method {@code Method} object representing the annotation's property
     * @param defaultValue Value to return in case of an exception
     * @return Method invocation result, or the default value if an internal exception was thrown
     */
    public static Object getProperty(Annotation annotation, Method method, Object defaultValue) {
        try {
            return method.invoke(annotation);
        } catch (IllegalAccessException | InvocationTargetException e) {
            PluginRuntime.context().getExceptionHandler().handle(new ReflectionException(
                String.format(INVOCATION_EXCEPTION_MESSAGE_TEMPLATE, method.getName(), annotation.annotationType().getName()),
                e));
        }
        return defaultValue;
    }

    /**
     * Gets whether any of the {@code Annotation}'s properties has a value which is not default
     * @param annotation The annotation to analyze
     * @return True or false
     */
    public static boolean isNotDefault(Annotation annotation) {
        return Arrays.stream(annotation.annotationType().getDeclaredMethods())
            .anyMatch(method -> propertyIsNotDefault(annotation, method));
    }

    /**
     * Gets whether an {@code Annotation} property has a value which is not default
     * @param annotation The annotation to analyze
     * @param method The method representing the property
     * @return True or false
     */
    public static boolean propertyIsNotDefault(Annotation annotation, Method method) {
        if (annotation == null) {
            return false;
        }
        try {
            Object defaultValue = method.getDefaultValue();
            if (defaultValue == null) {
                return true;
            }
            Object invocationResult = method.invoke(annotation);
            if (method.getReturnType().isArray() && ArrayUtils.isEmpty((Object[]) invocationResult)) {
                return false;
            }
            return !defaultValue.equals(invocationResult);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return true;
        }
    }

    /**
     * Retrieves list of properties of an {@code Annotation} object to which non-default values have been set
     * as a key-value map. The keys are the method names this annotation possesses, and the values are the results
     * of methods' invocation
     * @param annotation The annotation instance to analyze
     * @return List of {@code Method} instances that represent properties initialized with non-defaults
     */
    public static Map<String, Object> getNonDefaultProperties(Annotation annotation) {
        return getProperties(annotation, method -> propertyIsNotDefault(annotation, method));
    }

    /**
     * Retrieves list of properties of an {@code Annotation} object as a key-value map. The keys are the method names
     * this annotation possesses, and the values are the results of methods' invocation
     * @param annotation The annotation instance to analyze
     * @param filter {@code Predicate<Method>} do decide whether the current method is eligible for collection
     * @return {@code Map<String, Object>} instance containing property names and values
     */
    private static Map<String, Object> getProperties(Annotation annotation, Predicate<Method> filter) {
        Map<String, Object> result = new HashMap<>();
        for (Method method : Arrays.stream(annotation.annotationType().getDeclaredMethods()).filter(filter).collect(Collectors.toList())) {
            try {
                Object value = method.invoke(annotation, (Object[]) null);
                result.put(method.getName(), value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                PluginRuntime.context().getExceptionHandler().handle(new ReflectionException(
                    String.format(INVOCATION_EXCEPTION_MESSAGE_TEMPLATE, method.getName(), annotation.annotationType().getName()),
                    e));
            }
        }
        return result;
    }

    /**
     * Creates in runtime a {@code <T extends Annotation>}-typed annotation-like proxy object to mimic the behavior of
     * an actual annotation. Values to annotation methods are provided as a {@code Map<String, Object>} collection
     * @param type   Target annotation type
     * @param values Values that map to the target annotation's fields
     * @param <T>    Particular type of the annotation facade
     * @return Facade annotation instance, a subtype of the {@code Annotation} class
     */
    public static <T extends Annotation> T createInstance(Class<T> type, Map<String, Object> values) {
        Map<String, BiFunction<Annotation, Object[], Object>> methods = new HashMap<>();
        Arrays.stream(type.getDeclaredMethods())
            .forEach(method -> {
                BiFunction<Annotation, Object[], Object> methodFunction = (src, args) ->
                    values != null && values.containsKey(method.getName())
                        ? values.get(method.getName())
                        : method.getDefaultValue();
                methods.put(method.getName(), methodFunction);
            });
        return genericModify(null, type, methods);
    }

    /**
     * Creates in runtime a {@code <T extends Annotation>}-typed facade for the specified annotation with only some fields
     * set to a non-default value, others voided
     * This method is used to render the same {@code Annotation} object differently (like two or more different sets of values)
     * depending on the values specified
     * @param source       {@code Annotation} instance to produce a facade for
     * @param type         Target {@code Class} of the facade (one of subtypes of the {@code Annotation} class)
     * @param voidedFields The fields to be voided, a list of non-blank Strings
     * @param <T>          Particular type of the annotation facade
     * @return Facade annotation instance, a subtype of the {@code Annotation} class
     */
    public static <T extends Annotation> T filterInstance(Annotation source, Class<T> type, List<String> voidedFields) {
        Map<String, BiFunction<Annotation, Object[], Object>> methods = new HashMap<>();
        if (voidedFields != null) {
            voidedFields.stream()
                .map(methodName -> getMethodInstance(type, methodName))
                .filter(Objects::nonNull)
                .forEach(method -> methods.put(method.getName(),
                    (annotation, args) -> method.getReturnType().equals(String.class) ? StringUtils.EMPTY : 0L));
        }
        return genericModify(source, type, methods);
    }

    /**
     * Gets a filter routine typically passed to {@link com.exadel.aem.toolkit.api.handlers.Target#attributes(Annotation, Predicate)}.
     * If {@link PropertyMapping} is present in the annotation given, the filter passes combs through the methods
     * as regulated by the property mapping; otherwise a neutral (pass-all) filtering is imposed
     * @param annotation {@code Annotation} object to use methods from
     * @return {@code Predicate<Method>} instance
     */
    public static Predicate<Method> getPropertyMappingFilter(Annotation annotation) {
        PropertyMapping propMapping = Optional.ofNullable(annotation)
            .map(Annotation::annotationType)
            .map(annotationType -> annotationType.getAnnotation(PropertyMapping.class))
            .orElse(null);

        if (propMapping == null) {
            return MAP_ALL_PROPERTIES;
        }
        return method -> {
            boolean isAllowedByPropertyMapping = ArrayUtils.isEmpty(propMapping.mappings())
                || ArrayUtils.contains(propMapping.mappings(), method.getName());
            boolean isAllowedByIgnorePropertyMapping = Sources.fromMember(method).adaptTo(IgnorePropertyMapping.class) == null;
            return isAllowedByPropertyMapping && isAllowedByIgnorePropertyMapping;
        };
    }

    /**
     * Extends an object in runtime by casting it to an extension interface and applying additional methods
     * @param value        Source object, typically final or one coming from outside the user scope
     * @param modification {@code Class} object representing an interface that {@code value} implements,
     *                     or an extending interface of such
     * @param methods      {@code Map<String, Function>} of named routines that represent the new and/or modified methods
     *                     that {@code value} must expose. Each routine accepts a source object,
     *                     and a variadic array of {@code Object}-typed arguments
     * @param <T>          The {@code Class} of the source object
     * @param <U>          The interface exposing modified methods that the {@code value} implements, or an interface bearing
     *                     newly added methods that extends one of the interfaces implemented by {@code value}
     * @param <R>          The return type of extension methods. Must be fallen back to {@code Object} type of narrower generic
     *                     type in case returns are to be differently typed
     * @return The {@code <U>}-typed extension object, or else null if {@code modification} is null, or else
     * the {@code methods} map is empty
     */
    private static <T, U, R> U genericModify(T value, Class<U> modification, Map<String, BiFunction<T, Object[], R>> methods) {
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
            new Class[]{modification},
            new ExtensionInvocationHandler<>(value, modification, methods));
        return modification.cast(result);
    }


    /**
     * Wraps up extracting method from an {@code Annotation} signature by name, with {@link NoSuchMethodException} handled
     * @param annotationType Target {@code Class} of the facade (one of subtypes of the {@code Annotation} class)
     * @param name           Method name, non-blank String expected
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
     * per the {@link AnnotationUtil#genericModify(Object, Class, Map)} signature
     * @param <T> The {@code Class} of the source object
     * @param <R> The return type of the extension methods defined for this instance
     */
    private static class ExtensionInvocationHandler<T, U, R> implements InvocationHandler {
        private static final String METHOD_TO_STRING = "toString";
        private static final String METHOD_ANNOTATION_TYPE = "annotationType";

        private final T source;
        private final Class<U> targetType;
        private final Map<String, BiFunction<T, Object[], R>> extensionMethods;

        /**
         * Class constructor
         * @param source           The object to extend
         * @param extensionMethods {@code Map} composed of extension method names, String-typed,
         *                         and the extension routines, each a function accepting a source object, and
         *                         a variadic array of Object-typed arguments
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
}
