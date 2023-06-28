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
package com.exadel.aem.toolkit.plugin.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.exadel.aem.toolkit.api.markers._Default;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.exceptions.ReflectionException;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;

class InterfaceHandler<T> implements InvocationHandler {

    private static final String METHOD_ANNOTATION_TYPE = "annotationType";
    private static final String METHOD_EQUALS = "equals";
    private static final String METHOD_FOR_EACH = "forEach";
    private static final String METHOD_GET = "getValue";
    private static final String METHOD_GET_ANNOTATION = "getAnnotation";
    private static final String METHOD_GET_ANY_ANNOTATION = "getAnyAnnotation";
    private static final String METHOD_GET_PROPERTY = "getProperty";
    private static final String METHOD_HAS_PROPERTY = "hasProperty";
    private static final String METHOD_HASH_CODE = "hashCode";
    private static final String METHOD_ITERATOR = "iterator";
    private static final String METHOD_PUT = "putValue";
    private static final String METHOD_SPLITERATOR = "spliterator";
    private static final String METHOD_STREAM = "stream";
    private static final String METHOD_TO_STRING = "toString";
    private static final String METHOD_UNSET = "unsetValue";

    private static final String FIELD_SOURCE = "__source";
    private static final String FIELD_PROPERTIES = "__properties";

    private static final String OPENING_CURLY = "{";
    private static final String CLOSING_CURLY = "}";
    private static final String OPENING_SQUARE = "[";
    private static final String CLOSING_SQUARE = "]";

    private static final String TYPE_EXCEPTION_TEMPLATE = "Trying to set a value of type %s to property %s";
    private static final String VALUE_EXCEPTION_TEMPLATE = "Invalid value address %s";

    private final T source;
    private final Class<?> type;
    private final Map<String, Object> properties;

    InterfaceHandler(Class<T> type, Map<String, Object> properties) {
        this(null, type, properties);
    }

    InterfaceHandler(T source, Map<String, Object> properties) {
        this(
            source,
            source instanceof Annotation ? ((Annotation) source).annotationType() : source.getClass(),
            properties);
    }

    private InterfaceHandler(T source, Class<?> type, Map<String, Object> properties) {
        this.source = source;
        this.type = type;
        this.properties = new HashMap<>();
        if (properties != null) {
            properties.forEach((key, value) -> putValue(PropertyPath.parse(key), value));
        }
    }

    /* ----------
       Invocation
       ---------- */

    /**
     * {@inheritDoc}
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        InvocationResult invocation = tryInvokeStandardMember(method, args);
        if (invocation.isDone()) {
            return invocation.getResult();
        }
        invocation = tryInvokeMetadataMember(method, args);
        if (invocation.isDone()) {
            return invocation.getResult();
        }
        return getProperty(method.getName(), method.getName(), true, true).getValue();
    }

    private InvocationResult tryInvokeStandardMember(Method method, Object[] args) {
        if (method.getName().equals(METHOD_ANNOTATION_TYPE)) {
            return InvocationResult.done(type);
        }
        if (method.getName().equals(METHOD_EQUALS) && ArrayUtils.isNotEmpty(args)) {
            return InvocationResult.done(equals(args[0]));
        }
        if (method.getName().equals(METHOD_HASH_CODE)) {
            return InvocationResult.done(hashCode());
        }
        if (method.getName().equals(METHOD_TO_STRING)) {
            return InvocationResult.done(toString());
        }
        return InvocationResult.NOT_DONE;
    }

    private InvocationResult tryInvokeMetadataMember(Method method, Object[] args) {
        return Stream.<BiFunction<Method, Object[], InvocationResult>>of(
            this::tryInvokeGetAnnotation,
            this::tryInvokeGetAnyAnnotation,
            this::tryInvokeHasProperty,
            this::tryInvokeGetValue,
            this::tryInvokeGetProperty,
            this::tryInvokePutValue,
            this::tryInvokeUnsetValue,
            this::tryInvokeIterator,
            this::tryInvokeForEach,
            this::tryInvokeSpliterator,
            this::tryInvokeStream)
            .map(func -> func.apply(method, args))
            .filter(InvocationResult::isDone)
            .findFirst()
            .orElse(InvocationResult.NOT_DONE);
    }

    private <A extends Annotation> InvocationResult tryInvokeGetAnnotation(Method method, Object[] args) {
        if (matchesNameAndArguments(method, args, METHOD_GET_ANNOTATION, Class.class)) {
            @SuppressWarnings("unchecked")
            Annotation result = type.getDeclaredAnnotation((Class<A>) args[0]);
            return InvocationResult.done(result);
        }
        return InvocationResult.NOT_DONE;
    }

    private <A extends Annotation> InvocationResult tryInvokeGetAnyAnnotation(Method method, Object[] args) {
        if (matchesNameAndArguments(method, args, METHOD_GET_ANY_ANNOTATION, Class[].class)) {
            for (Class<?> cls : (Class<?>[]) args[0]) {
                @SuppressWarnings("unchecked")
                Annotation result = type.getDeclaredAnnotation((Class<A>) cls);
                if (result != null) {
                    return InvocationResult.done(result);
                }
            }
            return InvocationResult.done(null);
        }
        return InvocationResult.NOT_DONE;
    }

    private InvocationResult tryInvokeHasProperty(Method method, Object[] args) {
        if (matchesNameAndArguments(method, args, METHOD_HAS_PROPERTY, String.class)) {
            Property result = getProperty((String) args[0], false);
            return InvocationResult.done(result.getValue() != null);
        } else if (matchesNameAndArguments(method, args, METHOD_HAS_PROPERTY, PropertyPath.class)) {
            Property result = getProperty((PropertyPath) args[0], false);
            return InvocationResult.done(result.getValue() != null);
        }
        return InvocationResult.NOT_DONE;
    }

    private InvocationResult tryInvokeGetValue(Method method, Object[] args) {
        if (matchesNameAndArguments(method, args, METHOD_GET, String.class)) {
            Property result = getProperty((String) args[0], false);
            return InvocationResult.done(result.getValue());
        } else if (matchesNameAndArguments(method, args, METHOD_GET, PropertyPath.class)) {
            Property result = getProperty((PropertyPath) args[0], false);
            return InvocationResult.done(result.getValue());
        }
        return InvocationResult.NOT_DONE;
    }

    private InvocationResult tryInvokeGetProperty(Method method, Object[] args) {
        if (matchesNameAndArguments(method, args, METHOD_GET_PROPERTY, String.class)) {
            Property result = getProperty((String) args[0], true);
            return InvocationResult.done(result);
        } else if (matchesNameAndArguments(method, args, METHOD_GET_PROPERTY, PropertyPath.class)) {
            Property result = getProperty((PropertyPath) args[0], true);
            return InvocationResult.done(result);
        }
        return InvocationResult.NOT_DONE;
    }

    private InvocationResult tryInvokePutValue(Method method, Object[] args) {
        if (matchesNameAndArguments(method, args, METHOD_PUT, String.class, Object.class)) {
            Object result = putValue((String) args[0], args[1]);
            return InvocationResult.done(result);
        } else if (matchesNameAndArguments(method, args, METHOD_PUT, PropertyPath.class, Object.class)) {
            Object result = putValue((PropertyPath) args[0], args[1]);
            return InvocationResult.done(result);
        }
        return InvocationResult.NOT_DONE;
    }

    private InvocationResult tryInvokeUnsetValue(Method method, Object[] args) {
        if (matchesNameAndArguments(method, args, METHOD_UNSET, String.class)) {
            Object result = putValue((String) args[0], null);
            return InvocationResult.done(result);
        }
        return InvocationResult.NOT_DONE;
    }

    private InvocationResult tryInvokeIterator(Method method, Object[] args) {
        if (matchesNameAndArguments(method, args, METHOD_ITERATOR)
            || matchesNameAndArguments(method, args, METHOD_ITERATOR, boolean.class, boolean.class)) {
            Iterator result = getIterator(args);
            return InvocationResult.done(result);
        }
        return InvocationResult.NOT_DONE;
    }

    private InvocationResult tryInvokeForEach(Method method, Object[] args) {
        if (matchesNameAndArguments(method, args, METHOD_FOR_EACH, Consumer.class)) {
            @SuppressWarnings("unchecked")
            Consumer<? super Property> consumer = (Consumer<? super Property>) args[0];
            new Iterator(false, false).forEachRemaining(consumer);
            return InvocationResult.done(null);
        }
        return InvocationResult.NOT_DONE;
    }

    private InvocationResult tryInvokeSpliterator(Method method, Object[] args) {
        if (matchesNameAndArguments(method, args, METHOD_SPLITERATOR)
            || matchesNameAndArguments(method, args, METHOD_SPLITERATOR, boolean.class, boolean.class)) {
            Spliterator<Property> result = getSpliterator(args);
            return InvocationResult.done(result);
        }
        return InvocationResult.NOT_DONE;
    }

    private InvocationResult tryInvokeStream(Method method, Object[] args) {
        if (matchesNameAndArguments(method, args, METHOD_STREAM)
            || matchesNameAndArguments(method, args, METHOD_STREAM, boolean.class, boolean.class)) {
            Spliterator<Property> spliterator = getSpliterator(args);
            Stream<Property> stream = StreamSupport.stream(spliterator, false);
            return InvocationResult.done(stream);
        }
        return InvocationResult.NOT_DONE;
    }

    private static boolean matchesNameAndArguments(Method method, Object[] args, String name, Class<?>... argTypes) {
        if (!StringUtils.equals(method.getName(), name)) {
            return false;
        }
        if (ArrayUtils.isEmpty(argTypes)) {
            return true;
        }
        int argsLength = ArrayUtils.getLength(args);
        if (argsLength < argTypes.length) {
            return false;
        }
        for (int i = 0; i < argTypes.length; i++) {
            if (args[i] == null || !ClassUtils.isAssignable(args[i].getClass(), argTypes[i])) {
                return false;
            }
        }
        return true;
    }

    /* ---------------
       <Iterable> logic
       --------------- */

    private Iterator getIterator(Object[] args) {
        boolean deepRead = false;
        boolean expandArrays = false;
        if (ArrayUtils.isNotEmpty(args)
            && args[0] != null
            && ClassUtils.isAssignable(args[0].getClass(), boolean.class)) {
            deepRead = (boolean) args[0];
        }
        if (ArrayUtils.getLength(args) > 1
            && args[1] != null
            && ClassUtils.isAssignable(args[0].getClass(), boolean.class)) {
            expandArrays = (boolean) args[1];
        }
        return new Iterator(deepRead, expandArrays);
    }

    private Spliterator<Property> getSpliterator(Object[] args) {
        Iterator iterator = getIterator(args);
        return Spliterators.spliteratorUnknownSize(iterator, 0);
    }

    /* -----------------------
       <Metadata> #get logic
       ----------------------- */

    private Property getProperty(String path, boolean throwOnMissing) {
        return getProperty(PropertyPath.parse(path), throwOnMissing);
    }

    private Property getProperty(PropertyPath path, boolean throwOnMissing) {
        PropertyPathElement element = path.getElements().remove();
        String name = element.getName();
        if (FIELD_SOURCE.equals(name)) {
            return new Property(name, source);
        } else if (FIELD_PROPERTIES.equals(name)) {
            return new Property(name, properties);
        }
        Property result = getProperty(path.getPath(), name, throwOnMissing, true);
        if (result.getValue() == null) {
            return result;
        }
        if (result.getType().isArray() && element.hasIndex()) {
            if (element.getIndex() < Array.getLength(result.getValue())) {
                result.setValue(Array.get(result.getValue(), element.getIndex()));
            } else {
                result.setValue(null);
                return result;
            }
        }
        if (result.getComponentType().isAnnotation() && !path.getElements().isEmpty()) {
            return Metadata.from((Annotation) result.getValue()).getProperty(path);
        }
        return result;
    }

    private Property getProperty(String path, String name, boolean throwOnMissingMethod, boolean substituteMissingValue) {
        try {
            Method method = type.getDeclaredMethod(name);
            Object value = getDefaultReturnValue(method, substituteMissingValue);
            if (source != null) {
                value = method.invoke(source);
            }
            if (properties != null && properties.containsKey(name)) {
                value = properties.get(method.getName());
                value = value != null ? value : getDefaultReturnValue(method, substituteMissingValue);
            }
            return new MethodBackedProperty(path, method, value);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            if (throwOnMissingMethod) {
                PluginRuntime
                    .context()
                    .getExceptionHandler()
                    .handle(new ReflectionException(String.format(VALUE_EXCEPTION_TEMPLATE, name), e));
            }
        }
        return Property.EMPTY;
    }

    /**
     * Retrieves a default value for a method based on the method return type. This method mainly address the case when
     * a {@link Metadata} is created from just type and aims to make sure that as many annotation properties as
     * possible do not return {@code null}. Note: currently this method is not comprehensive since it does not cover all
     * the possible annotation properties' types
     * @param method               {@code Method} instance
     * @param createMissingObjects {@code True} to generate missing objects, such as nested arrays of annotation
     *                             instances. Useful when querying annotation data but must be turned off when
     *                             data is being stored. In the latter case, the stored data will end up in a "detached"
     *                             ad-hoc object
     * @return A nullable value
     */
    private static Object getDefaultReturnValue(Method method, boolean createMissingObjects) {
        if (method.getDefaultValue() != null) {
            return method.getDefaultValue();
        }
        if (method.getReturnType().isArray() && createMissingObjects) {
            return Array.newInstance(method.getReturnType().getComponentType(), 0);
        }
        if (method.getReturnType().isAnnotation() && createMissingObjects) {
            return Metadata.from(method.getReturnType());
        }
        if (method.getReturnType().equals(String.class)) {
            return StringUtils.EMPTY;
        }
        if (method.getReturnType().equals(Class.class)) {
            return _Default.class;
        }
        if (ClassUtils.primitiveToWrapper(method.getReturnType()).equals(Boolean.class)) {
            return false;
        }
        if (ClassUtils.primitiveToWrapper(method.getReturnType()).equals(Integer.class)) {
            return 0;
        }
        if (ClassUtils.primitiveToWrapper(method.getReturnType()).equals(Long.class)) {
            return 0L;
        }
        if (ClassUtils.primitiveToWrapper(method.getReturnType()).equals(Float.class)) {
            return 0f;
        }
        if (ClassUtils.primitiveToWrapper(method.getReturnType()).equals(Double.class)) {
            return 0d;
        }
        return null;
    }

    /* -----------------------
       <Metadata> #put logic
       ----------------------- */

    private Object putValue(String path, Object value) {
        return putValue(PropertyPath.parse(path), value);
    }

    private Object putValue(PropertyPath path, Object value) {
        if (path.getElements().size() > 1) {
            return putInTree(path, value);
        }
        PropertyPathElement element = path.getElements().remove();
        Property currentProperty = getProperty(path.getPath(), element.getName(), true, false);
        if (Property.EMPTY.equals(currentProperty)) {
            // Probably a nonexistent property name. An exception is already handled
            return null;
        }

        boolean mustWriteToArray = currentProperty.getType().isArray() && element.hasIndex();
        if (mustWriteToArray) {
            if (!validateValueType(element, path, value, true)
                || !validateArrayBounds(element, path, currentProperty.getValue())) {
                return null;
            }
            Object modifiedValue = expandArrayIfNeeded(
                currentProperty.getValue(),
                currentProperty.getComponentType(),
                element.getIndex());
            properties.put(element.getName(), modifiedValue);
            Array.set(modifiedValue, element.getIndex(), value);
            return value;
        }

        if (validateValueType(element, path, value)) {
            return properties.put(element.getName(), value);
        }
        return null;
    }

    private Object putInTree(PropertyPath path, Object value) {
        PropertyPathElement element = path.getElements().remove();
        Property currentProperty = getProperty(path.getPath(), element.getName(), true, false);
        if (Property.EMPTY.equals(currentProperty)) {
            // Probably a nonexistent property name. An exception is already handled
            return null;
        }

        Object existingValue = currentProperty.getValue();
        boolean mustWriteToArray = currentProperty.getType().isArray() && element.hasIndex();
        if (mustWriteToArray) {
            if (!validateValueType(element, path, currentProperty.getValue(), true)
                || !validateArrayBounds(element, path, currentProperty.getValue())) {
                return null;
            }
            existingValue = expandArrayIfNeeded(
                currentProperty.getValue(),
                currentProperty.getComponentType(),
                element.getIndex());
            properties.put(element.getName(), existingValue);
            existingValue = Array.get(existingValue, element.getIndex());
        }
        if (!currentProperty.getComponentType().isAnnotation()) {
            PluginRuntime
                .context()
                .getExceptionHandler()
                .handle(new ReflectionException(String.format(VALUE_EXCEPTION_TEMPLATE, path)));
            return null;
        }

        Metadata metadata;
        if (!(existingValue instanceof Metadata)) {
            metadata = existingValue != null
                ? Metadata.from((Annotation) existingValue)
                : (Metadata) Metadata.from(currentProperty.getComponentType());
            if (mustWriteToArray) {
                Array.set(properties.get(element.getName()), element.getIndex(), metadata);
            } else {
                properties.put(element.getName(), metadata);
            }
        } else {
            metadata = (Metadata) existingValue;
        }
        return metadata.putValue(path, value);
    }

    private static Object expandArrayIfNeeded(Object source, Class<?> componentType, int index) {
        int sourceLength = (source == null || !source.getClass().isArray()) ? 0 : Array.getLength(source);
        if (index < sourceLength) {
            return source;
        }
        Object newArray = Array.newInstance(componentType, sourceLength + 1);
        for (int i = 0; i < sourceLength; i++) {
            Array.set(newArray, i, Array.get(source, i));
        }
        if (componentType.isAnnotation()) {
            Array.set(newArray, sourceLength, Metadata.from(componentType));
        }
        return newArray;
    }

    /* ----------------
       Validation logic
       ---------------- */

    private boolean validateValueType(PropertyPathElement element, PropertyPath path, Object value) {
        return validateValueType(element, path, value, false);
    }

    private boolean validateValueType(PropertyPathElement element, PropertyPath path, Object value, boolean lookUpArray) {
        if (value == null) {
            return true;
        }
        boolean result;
        try {
            Method method = type.getDeclaredMethod(element.getName());
            Class<?> methodType = method.getReturnType();
            if (methodType.isArray() && !value.getClass().isArray() && lookUpArray) {
                methodType = methodType.getComponentType();
            }
            result = ClassUtils.isAssignable(value.getClass(), methodType);
        } catch (NoSuchMethodException e) {
            // An exception is not expected here because the method name has already been trialed
            return false;
        }
        if (!result) {
            PluginRuntime
                .context()
                .getExceptionHandler()
                .handle(new ReflectionException(String.format(
                    TYPE_EXCEPTION_TEMPLATE,
                    value.getClass().getSimpleName(),
                    path)));
        }
        return result;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean validateArrayBounds(PropertyPathElement element, PropertyPath path, Object target) {
        int length = (target == null || !target.getClass().isArray()) ? 0 : Array.getLength(target);
        if (element.getIndex() > length) {
            PluginRuntime
                .context()
                .getExceptionHandler()
                .handle(new ReflectionException(String.format(VALUE_EXCEPTION_TEMPLATE, path)));
            return false;
        }
        return true;
    }

    /* -----------------
       Silent invocation
       ----------------- */

    private static Object invokeSilently(Method method, Object source) {
        try {
            return method.invoke(source);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    /* -------------
       Serialization
       ------------- */

    @Override
    public String toString() {
        if (source == null && properties.isEmpty()) {
            return String.valueOf(type);
        }
        StringBuilder result = new StringBuilder(CoreConstants.SEPARATOR_AT).append(type.getName()).append(OPENING_CURLY);
        for (Method method : type.getDeclaredMethods()) {
            Object methodValue = source != null ? invokeSilently(method, source) : null;
            methodValue = properties.getOrDefault(method.getName(), methodValue);
            boolean isDefaultMethodValue = false;
            if (methodValue == null) {
                methodValue = getDefaultReturnValue(method, true);
                isDefaultMethodValue = true;
            }
            result.append(method.getName())
                .append(CoreConstants.EQUALITY_SIGN)
                .append(isDefaultMethodValue ? "(default) " : StringUtils.EMPTY);
            if (methodValue != null && methodValue.getClass().isArray()) {
                result.append(toArrayString(methodValue));
            } else if (methodValue != null && methodValue.getClass().isAnnotation()) {
                result.append(Metadata.from((Annotation) methodValue));
            } else {
                result.append(methodValue);
            }
            result.append(DialogConstants.SEPARATOR_SEMICOLON);
        }
        return StringUtils.stripEnd(result.toString(), DialogConstants.SEPARATOR_SEMICOLON) + CLOSING_CURLY;
    }

    private static String toArrayString(Object array) {
        StringBuilder result = new StringBuilder(OPENING_SQUARE);
        for (int i = 0; i < Array.getLength(array); i++) {
            Object entry = Array.get(array, i);
            if (entry instanceof Annotation) {
                result.append(Metadata.from((Annotation) entry));
            } else {
                result.append(entry);
            }
            result.append(CoreConstants.SEPARATOR_COMMA);
        }
        return StringUtils.strip(result.toString(), CoreConstants.SEPARATOR_COMMA) + CLOSING_SQUARE;
    }

    /* -------------------------
       Standard method overrides
       ------------------------- */

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Metadata)) {
            return false;
        }
        Metadata that = (Metadata) o;
        return new EqualsBuilder()
            .append(source, that.getValue(FIELD_SOURCE))
            .append(type, that.annotationType())
            .append(properties, that.getValue(FIELD_PROPERTIES))
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(source)
            .append(type)
            .append(properties)
            .toHashCode();
    }

    /* ---------------
       Utility classes
       --------------- */

    private class Iterator implements java.util.Iterator<Property> {

        private final boolean deepRead;
        private final boolean expandArrays;
        private final Queue<Property> collection;

        public Iterator(boolean deepRead, boolean expandArrays) {
            this.deepRead = deepRead;
            this.expandArrays = expandArrays;
            this.collection = new LinkedList<>();
            collect(null, StringUtils.EMPTY, this.collection);
        }

        @Override
        public boolean hasNext() {
            return !collection.isEmpty();
        }

        @Override
        public Property next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return collection.remove();
        }

        private void collect(Annotation target, String pathPrefix, Queue<Property> collection) {
            Method[] methods = target != null
                ? target.annotationType().getDeclaredMethods()
                : type.getDeclaredMethods();
            for (Method method : methods) {
                collect(target, method, pathPrefix, collection);
            }
        }

        private void collect(Annotation target, Method method, String pathPrefix, Queue<Property> collection) {
            String path = joinPathChunks(pathPrefix, method.getName());
            Class<?> propertyType = method.getReturnType();
            Object value = target != null
                ? invokeSilently(method, target)
                : invokeInCurrentObjectSilently(method);
            if (value == null) {
                value = getDefaultReturnValue(method, true);
            }
            if (deepRead && propertyType.isAnnotation()) {
                collect((Annotation) value, path, collection);
            } else if (expandArrays && propertyType.isArray()) {
                int length = Array.getLength(value);
                for (int i = 0; i < length; i++) {
                    String indexedPath = path + OPENING_SQUARE + i + CLOSING_SQUARE;
                    if (deepRead && propertyType.getComponentType().isAnnotation()) {
                        collect((Annotation) Array.get(value, i), indexedPath, collection);
                    } else {
                        collection.add(new Property(indexedPath, Array.get(value, i)));
                    }
                }
            } else {
                collection.add(new MethodBackedProperty(path, method, value));
            }
        }

        private Object invokeInCurrentObjectSilently(Method method) {
            if (properties != null && properties.containsKey(method.getName())) {
                return properties.get(method.getName());
            }
            return source != null ? invokeSilently(method, source) : null;
        }

        private String joinPathChunks(String left, String right) {
            return left
                + (StringUtils.isNoneEmpty(left, right) ? CoreConstants.SEPARATOR_SLASH : StringUtils.EMPTY)
                + right;
        }
    }
}
