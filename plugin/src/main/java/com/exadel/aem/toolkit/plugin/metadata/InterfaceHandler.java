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

/**
 * Implements {@link InvocationHandler} to provide a {@link Metadata} instance that exposes the properties of a
 * {@code T}-typed source object (usually, an annotation
 * @param <T> Type of the source object
 */
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

    private static final String OPENING_SQUARE = CoreConstants.ARRAY_OPENING;
    private static final String CLOSING_SQUARE = CoreConstants.ARRAY_CLOSING;

    private static final String TYPE_EXCEPTION_TEMPLATE = "Trying to set a value of type %s to property %s";
    private static final String VALUE_EXCEPTION_TEMPLATE = "Invalid value address %s";

    private static final int HASH_INITIAL_NUMBER = 17;
    private static final int HASH_MULTIPLIER = 37;

    private final T source;
    private final Class<?> type;
    private final Map<String, Object> properties;

    /**
     * Constructs an instance of {@code InterfaceHandler} class with its type and the dictionary of property values set
     * @param type       Type of the source object
     * @param properties Dictionary of property values
     */
    InterfaceHandler(Class<T> type, Map<String, Object> properties) {
        this(null, type, properties);
    }

    /**
     * Constructs an instance of {@code InterfaceHandler} class with the source object and the dictionary of property
     * values set
     * @param source     The object used as the source of property values
     * @param properties Dictionary of property values used to override and/or supplement those of the source object
     */
    InterfaceHandler(T source, Map<String, Object> properties) {
        this(
            source,
            source instanceof Annotation ? ((Annotation) source).annotationType() : source.getClass(),
            properties);
    }

    /**
     * Constructs an instance of {@code InterfaceHandler} class with the source object, its type, and the dictionary of
     * property values set
     * @param source     The object used as the source of property values
     * @param type       Type of the source object
     * @param properties Dictionary of property values used to override and/or supplement those of the source object
     */
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

    /**
     * Called from {@link InterfaceHandler#invoke(Object, Method, Object[])} to check if the method requested for
     * invocation is one of the standard OOTB methods of a Java object, and if so, retrieves the return value of such a
     * method
     * @param method The method to check
     * @param args   An array of objects containing the values of the arguments passed to
     *               {@link InterfaceHandler#invoke(Object, Method, Object[])}
     * @return {@code InvocationResult} instance containing either the return value or the
     * {@link InvocationResult#NOT_DONE} which effectively tells that invocation attempts should continue
     */
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

    /**
     * Called from {@link InterfaceHandler#invoke(Object, Method, Object[])} to check if the method requested for
     * invocation is one of the methods handled by the current class, and if so, retrieves the return value of such a
     * method
     * @param method The method to check
     * @param args   An array of objects containing the values of the arguments passed to
     *               {@link InterfaceHandler#invoke(Object, Method, Object[])}
     * @return Either an {@code InvocationResult} instance containing the return value or the
     * {@link InvocationResult#NOT_DONE} which effectively tells that invocation attempts should continue
     */
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

    /**
     * Tests if the requested method is {@code getAnnotation()} and retrieves the annotation value
     * @param method The method to check
     * @param args   An array of objects containing the values of the arguments passed to
     *               {@link InterfaceHandler#invoke(Object, Method, Object[])}
     * @param <A>    Type of the annotation
     * @return Either an {@code InvocationResult} instance containing the return value or the
     * {@link InvocationResult#NOT_DONE} which effectively tells that invocation attempts should continue
     */
    private <A extends Annotation> InvocationResult tryInvokeGetAnnotation(Method method, Object[] args) {
        if (matchesNameAndArguments(method, args, METHOD_GET_ANNOTATION, Class.class)) {
            @SuppressWarnings("unchecked")
            Annotation result = type.getDeclaredAnnotation((Class<A>) args[0]);
            return InvocationResult.done(result);
        }
        return InvocationResult.NOT_DONE;
    }

    /**
     * Tests if the requested method is {@code getAnyAnnotation()} and retrieves the matching annotation value. Consumes
     * an array of annotation types
     * @param method The method to check
     * @param args   An array of objects containing the values of the arguments passed to
     *               {@link InterfaceHandler#invoke(Object, Method, Object[])}
     * @param <A>    Type of the annotation
     * @return Either an {@code InvocationResult} instance containing the return value or the
     * {@link InvocationResult#NOT_DONE} which effectively tells that invocation attempts should continue
     */
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

    /**
     * Tests if the requested method is {@code hasProperty()} and retrieves whether the given property is present
     * @param method The method to check
     * @param args   An array of objects containing the values of the arguments passed to
     *               {@link InterfaceHandler#invoke(Object, Method, Object[])}
     * @return Either an {@code InvocationResult} instance containing the return value or the
     * {@link InvocationResult#NOT_DONE} which effectively tells that invocation attempts should continue
     */
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

    /**
     * Tests if the requested method is {@code getValue()} and retrieves the value of a property of the source object by
     * the given name or path
     * @param method The method to check
     * @param args   An array of objects containing the values of the arguments passed to
     *               {@link InterfaceHandler#invoke(Object, Method, Object[])}
     * @return {@code InvocationResult} instance containing either the return value or the
     * {@link InvocationResult#NOT_DONE} which effectively tells that invocation attempts should continue
     */
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

    /**
     * Tests if the requested method is {@code getProperty()} and retrieves the property of the source object by the
     * given name or path
     * @param method The method to check
     * @param args   An array of objects containing the values of the arguments passed to
     *               {@link InterfaceHandler#invoke(Object, Method, Object[])}
     * @return {@code InvocationResult} instance containing either the return value or the
     * {@link InvocationResult#NOT_DONE} which tells that invocation attempts should continue
     */
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

    /**
     * Tests if the requested method is {@code putValue()} and assigns a value to the property of the source object by
     * the given name or path
     * @param method The method to check
     * @param args   An array of objects containing the values of the arguments passed to
     *               {@link InterfaceHandler#invoke(Object, Method, Object[])}
     * @return {@code InvocationResult} instance containing either the return value of the method called or the
     * {@link InvocationResult#NOT_DONE} which tells that invocation attempts should continue
     */
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

    /**
     * Tests if the requested method is {@code unsetValue()} and clears the "overriding" value previously assigned to a
     * property of the source object
     * @param method The method to check
     * @param args   An array of objects containing the values of the arguments passed to
     *               {@link InterfaceHandler#invoke(Object, Method, Object[])}
     * @return {@code InvocationResult} instance containing either the return value of the method called or the
     * {@link InvocationResult#NOT_DONE} which tells that invocation attempts should continue
     */
    private InvocationResult tryInvokeUnsetValue(Method method, Object[] args) {
        if (matchesNameAndArguments(method, args, METHOD_UNSET, String.class)) {
            Object result = putValue((String) args[0], null);
            return InvocationResult.done(result);
        }
        return InvocationResult.NOT_DONE;
    }

    /**
     * Tests if the requested method is {@code iterator()} and performs appropriate invocation
     * @param method The method to check
     * @param args   An array of objects containing the values of the arguments passed to
     *               {@link InterfaceHandler#invoke(Object, Method, Object[])}
     * @return {@code InvocationResult} instance containing either the return value of the method called or the
     * {@link InvocationResult#NOT_DONE} which tells that invocation attempts should continue
     */
    private InvocationResult tryInvokeIterator(Method method, Object[] args) {
        if (matchesNameAndArguments(method, args, METHOD_ITERATOR)
            || matchesNameAndArguments(method, args, METHOD_ITERATOR, boolean.class, boolean.class)) {
            Iterator result = getIterator(args);
            return InvocationResult.done(result);
        }
        return InvocationResult.NOT_DONE;
    }

    /**
     * Tests if the requested method is {@code forEach()} and performs appropriate invocation
     * @param method The method to check
     * @param args   An array of objects containing the values of the arguments passed to
     *               {@link InterfaceHandler#invoke(Object, Method, Object[])}
     * @return {@code InvocationResult} instance containing either the return value of the method called or the
     * {@link InvocationResult#NOT_DONE} which tells that invocation attempts should continue
     */
    private InvocationResult tryInvokeForEach(Method method, Object[] args) {
        if (matchesNameAndArguments(method, args, METHOD_FOR_EACH, Consumer.class)) {
            @SuppressWarnings("unchecked")
            Consumer<? super Property> consumer = (Consumer<? super Property>) args[0];
            new Iterator(false, false).forEachRemaining(consumer);
            return InvocationResult.done(null);
        }
        return InvocationResult.NOT_DONE;
    }

    /**
     * Tests if the requested method is {@code spliterator()} and performs appropriate invocation
     * @param method The method to check
     * @param args   An array of objects containing the values of the arguments passed to
     *               {@link InterfaceHandler#invoke(Object, Method, Object[])}
     * @return {@code InvocationResult} instance containing either the return value of the method called or the
     * {@link InvocationResult#NOT_DONE} which tells that invocation attempts should continue
     */
    private InvocationResult tryInvokeSpliterator(Method method, Object[] args) {
        if (matchesNameAndArguments(method, args, METHOD_SPLITERATOR)
            || matchesNameAndArguments(method, args, METHOD_SPLITERATOR, boolean.class, boolean.class)) {
            Spliterator<Property> result = getSpliterator(args);
            return InvocationResult.done(result);
        }
        return InvocationResult.NOT_DONE;
    }

    /**
     * Tests if the requested method is {@code stream()} and performs appropriate invocation
     * @param method The method to check
     * @param args   An array of objects containing the values of the arguments passed to
     *               {@link InterfaceHandler#invoke(Object, Method, Object[])}
     * @return {@code InvocationResult} instance containing either the return value of the method called or the
     * {@link InvocationResult#NOT_DONE} which effectively tells that invocation attempts should continue
     */
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

    /**
     * Retrieves an {@link Iterator} instance that can be used to iterate through the properties of the source object
     * @param args An array of objects containing the values of the arguments passed to
     *             {@link InterfaceHandler#invoke(Object, Method, Object[])}. We expect the 0th argument to be the flag
     *             determining whether array values should be iterated as separate entities, or else the array is
     *             yielded as is
     * @return {@code Iterator} instance
     */
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

    /**
     * Retrieves an {@link Spliterator} instance that can be used to iterate through the properties of the source
     * object
     * @param args An array of objects containing per the contract of the
     *             {@link InterfaceHandler#invoke(Object, Method, Object[])} method
     * @return {@code Spliterator} instance
     */
    private Spliterator<Property> getSpliterator(Object[] args) {
        Iterator iterator = getIterator(args);
        return Spliterators.spliteratorUnknownSize(iterator, 0);
    }

    /* -----------------------
       <Metadata> #get logic
       ----------------------- */

    /**
     * Retrieves a {@link Property} object by the given path
     * @param path           The path within the current object to construct the property from
     * @param throwOnMissing {@code True} to throw an exception if the property is not found
     * @return A nullable {@code Property} object
     */
    private Property getProperty(String path, boolean throwOnMissing) {
        return getProperty(PropertyPath.parse(path), throwOnMissing);
    }

    /**
     * Retrieves a {@link Property} object by the given path
     * @param path           The {@link PropertyPath} instance that manifests the path within the current object to
     *                       construct the property from
     * @param throwOnMissing {@code True} to throw an exception if the property is not found
     * @return A nullable {@code Property} object
     * @see PropertyPath
     */
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

    /**
     * Retrieves a {@link Property} object by the given path
     * @param path                   The "complete" path within the current object to construct the property from
     * @param name                   A string representing the current path chunk
     * @param throwOnMissingMethod   {@code True} to throw an exception if the property is not found
     * @param substituteMissingValue {@code True} to substitute a {@code }
     * @return A nullable {@code Property} object
     * @see PropertyPath
     */
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
     * a {@link Metadata} is created from just type and aims to make sure that as many annotation properties as possible
     * do not return {@code null}. Note: currently this method is not comprehensive since it does not cover all the
     * possible annotation properties' types
     * @param method               {@code Method} instance
     * @param createMissingObjects {@code True} to generate missing objects, such as nested arrays of annotation
     *                             instances. Useful when querying annotation data but must be turned off when data is
     *                             being stored. In the latter case, the stored data will end up in a "detached" ad-hoc
     *                             object
     * @return A nullable value
     */
    @SuppressWarnings("unchecked")
    private static Object getDefaultReturnValue(Method method, boolean createMissingObjects) {
        if (method.getDefaultValue() != null) {
            return method.getDefaultValue();
        }
        if (method.getReturnType().isArray() && createMissingObjects) {
            return Array.newInstance(method.getReturnType().getComponentType(), 0);
        }
        if (method.getReturnType().isAnnotation() && createMissingObjects) {
            return Metadata.from((Class<? extends Annotation>) method.getReturnType());
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

    /**
     * Assigns a value to the property of the source object by the given path
     * @param path  The path that manifests a property of the current object
     * @param value The value to assign
     * @return The value assigned
     */
    private Object putValue(String path, Object value) {
        return putValue(PropertyPath.parse(path), value);
    }

    /**
     * Assigns a value to the property of the source object by the given path
     * @param path  {@link PropertyPath} instance that manifests a property of the current object
     * @param value The value to assign
     * @return The value assigned
     */
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
            Object modifiedValue = appendToArrayIfNeeded(
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

    /**
     * Called by {@link InterfaceHandler#putValue(PropertyPath, Object)} to assign a value to a property of the source
     * manifested by a compound (tree-like) path
     * @param path  {@link PropertyPath} instance that manifests a property of the current object
     * @param value The value to assign
     * @return The value assigned
     */
    @SuppressWarnings("unchecked")
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
            existingValue = appendToArrayIfNeeded(
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
                : (Metadata) Metadata.from((Class<? extends Annotation>) currentProperty.getComponentType());
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

    /**
     * Called by a property-assigning routine to convert the provided source object into an array or else extend the
     * provided array and append to it a new {@link Metadata} object (probably a proxied annotation) built upon the
     * given {@code contentType}. This method can be used to construct a new metadata instance and fill in its
     * array-typed properties via notation like {@code /my/property[0] = "value", /my/property[1} = "value2, etc
     * @param source        The array-typed object to modify
     * @param componentType The type of an element of the array
     * @param index         The index of the element to modify
     * @return The modified array or the original object if not of an array type or else the index is invalid
     */
    @SuppressWarnings("unchecked")
    private static Object appendToArrayIfNeeded(Object source, Class<?> componentType, int index) {
        int sourceLength = (source == null || !source.getClass().isArray()) ? 0 : Array.getLength(source);
        if (index < sourceLength) {
            return source;
        }
        Object newArray = Array.newInstance(componentType, sourceLength + 1);
        for (int i = 0; i < sourceLength; i++) {
            Array.set(newArray, i, Array.get(source, i));
        }
        if (componentType.isAnnotation()) {
            Array.set(newArray, sourceLength, Metadata.from((Class<? extends Annotation>) componentType));
        }
        return newArray;
    }

    /* ----------------
       Validation logic
       ---------------- */

    /**
     * Called by a property-assigning routine to validate that the value to assign to a property is of the same type as
     * the property itself
     * @param element The {@link PropertyPathElement} instance that manifests the terminal (last-in-the path) member
     *                within the source object to assign the value to
     * @param path    The {@link PropertyPath} instance that manifests the path within the source object
     * @param value   The value to assign
     * @return True or false
     */
    private boolean validateValueType(PropertyPathElement element, PropertyPath path, Object value) {
        return validateValueType(element, path, value, false);
    }

    /**
     * Called by a property-assigning routine to validate that the value to assign to a property is of the same type as
     * the property itself
     * @param element     The {@link PropertyPathElement} instance that manifests the terminal (last-in-the path) member
     *                    within the source object to assign the value to
     * @param path        The {@link PropertyPath} instance that manifests the path within the source object
     * @param value       The value to assign
     * @param lookUpArray {@code True} to test the component type of the array-typed property
     * @return True or false
     */
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

    /**
     * Called by a property-assigning routine to validate that the index of the array-typed property is within the
     * bounds of the array
     * @param element A {@link PropertyPathElement} instance that signifies the terminal (last-in-the path) member
     *                within the source object to assign the value to. It is expected to bear a valid index which is
     *                used for validation of {@code target}
     * @param path    The {@link PropertyPath} instance that manifests the path within the source object
     * @param target  The array-typed or else convertible to array value which is checked with the index
     * @return True if the index falls within the array bounds or else false
     */
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

    /**
     * Invokes the given method on the source object and returns the result or else {@code null} if the invocation
     * failed with an exception
     * @param method The method to invoke
     * @param source The source object to invoke the method on
     * @return A nullable value
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (source == null && properties.isEmpty()) {
            return String.valueOf(type);
        }
        StringBuilder result = new StringBuilder(CoreConstants.SEPARATOR_AT)
            .append(type.getName())
            .append(DialogConstants.OPENING_CURLY);
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
        return StringUtils.stripEnd(result.toString(), DialogConstants.SEPARATOR_SEMICOLON) + DialogConstants.CLOSING_CURLY;
    }

    /**
     * Called by {@link InterfaceHandler#toString()} to convert the given array to a string representation
     * @param array The array to convert
     * @return A string representation of the array; can be an empty string but never {@code null}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(HASH_INITIAL_NUMBER, HASH_MULTIPLIER)
            .append(source)
            .append(type)
            .append(properties)
            .toHashCode();
    }

    /* ---------------
       Utility classes
       --------------- */

    /**
     * Implements {@link java.util.Iterator} to provide sequential access to properties of an object (usually a proxied
     * annotation), including (optionally) nested properties and array elements
     */
    private class Iterator implements java.util.Iterator<Property> {

        private final boolean deepRead;
        private final boolean expandArrays;
        private final Queue<Property> properties;

        /**
         * Constructs a new {@code Iterator} instance
         * @param deepRead     {@code True} to read nested properties
         * @param expandArrays {@code True} to expand array elements into separate elements of iteration
         */
        Iterator(boolean deepRead, boolean expandArrays) {
            this.deepRead = deepRead;
            this.expandArrays = expandArrays;
            this.properties = new LinkedList<>();
            collect(null, StringUtils.EMPTY, this.properties);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext() {
            return !properties.isEmpty();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Property next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return properties.remove();
        }

        /**
         * Analyzes the given annotation and adds its properties to the given queue
         * @param target     The annotation to analyze
         * @param pathPrefix The part of the path to prepend to the property names. Usually signifies nested properties
         * @param collection The queue to add the properties to. It will be afterwards iterated with
         *                   {@link Iterator#next()}
         */
        private void collect(Annotation target, String pathPrefix, Queue<Property> collection) {
            Method[] methods = target != null
                ? target.annotationType().getDeclaredMethods()
                : type.getDeclaredMethods();
            for (Method method : methods) {
                collect(target, method, pathPrefix, collection);
            }
        }

        /**
         * Adds a property manifested by the given {@code Annotation} and {@code Method} the given queue
         * @param target     The annotation being analyzed
         * @param method     The method to retrieve a value from
         * @param pathPrefix The part of the path to prepend to the property names. Usually signifies nested properties
         * @param collection The queue to add the properties to. It will be afterwards iterated with
         *                   {@link Iterator#next()}
         */
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

        /**
         * Invokes the given method on the source object and returns the result or else {@code null} if the invocation
         * fails
         * @param method The method to invoke
         * @return A nullable value
         */
        private Object invokeInCurrentObjectSilently(Method method) {
            if (InterfaceHandler.this.properties != null && InterfaceHandler.this.properties.containsKey(method.getName())) {
                return InterfaceHandler.this.properties.get(method.getName());
            }
            return source != null ? invokeSilently(method, source) : null;
        }

        /**
         * Joins two property path chunks with a slash
         * @param left  The left part of the path
         * @param right The right part of the path
         * @return A string representing the joined path
         */
        private String joinPathChunks(String left, String right) {
            return left
                + (StringUtils.isNoneEmpty(left, right) ? CoreConstants.SEPARATOR_SLASH : StringUtils.EMPTY)
                + right;
        }
    }
}
