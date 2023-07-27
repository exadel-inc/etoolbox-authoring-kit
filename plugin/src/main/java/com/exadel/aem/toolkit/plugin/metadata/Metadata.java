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
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.stream.Stream;

import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;

/**
 * Represents a generic metadata object intended to retrieve, modify, and store Java annotations properties via
 * invocation handling
 * @see MetadataHandler
 * @see Property
 */
public interface Metadata extends Annotation, Iterable<Property> {

    /**
     * Retrieves a meta-annotation of the annotation represented by the current {@code Metadata} instance by its type
     * @param type {@code Class} reference, not null
     * @param <T>  Type of the annotation to retrieve
     * @return {@code Annotation} instance, or null if not found
     */
    <T extends Annotation> T getAnnotation(Class<T> type);

    /**
     * Retrieves the first meta-annotation of the annotation represented by the current {@code Metadata} instance that
     * matches any of the provided types
     * @param types One or more non-null {@code Class} references
     * @return {@code Annotation} instance, or null if no match is found
     */
    Annotation getAnyAnnotation(Class<?>... types);

    /**
     * Gets whether the annotation reflected by the current object has a property specified by the path. The path can be
     * either a simple name or a tree-like comma-delimited or slash-delimited structure
     * @param path Path to the property: a dot-delimited or slash-delimited string
     * @return True or false
     * @see PropertyPath
     */
    boolean hasProperty(String path);

    /**
     * Retrieves a property of the annotation represented by the current {@code Metadata} instance by its path
     * @param path Path to the property: a dot-delimited or slash-delimited string
     * @return {@link Property} instance, or null if not found
     */
    Property getProperty(String path);

    /**
     * Retrieves a property of the annotation represented by the current {@code Metadata} instance by its path
     * @param path Path to the property as a {@link PropertyPath} instance
     * @return {@link Property} instance, or null if not found
     */
    Property getProperty(PropertyPath path);

    /**
     * Retrieves the value of a property of the annotation represented by the current {@code Metadata} instance by its
     * path
     * @param path Path to the property: a dot-delimited or slash-delimited string
     * @return Arbitrary nullable object
     */
    Object getValue(String path);

    /**
     * Retrieves the value of a property of the annotation represented by the current {@code Metadata} instance by its
     * path
     * @param path Path to the property as a {@link PropertyPath} instance
     * @return Arbitrary nullable object
     */
    Object getValue(PropertyPath path);

    /**
     * Modifies the value of a property of the annotation represented by the current {@code Metadata} instance by
     * storing a makeshift substitution value
     * @param path  Path to the property: a dot-delimited or slash-delimited string
     * @param value Arbitrary nullable object
     * @return The modified value
     */
    Object putValue(String path, Object value);

    /**
     * Modifies the value of a property of the annotation represented by the current {@code Metadata} instance by
     * storing a makeshift substitution value
     * @param path  Path to the property as a {@link PropertyPath} instance
     * @param value Arbitrary nullable object
     * @return The modified value
     */
    Object putValue(PropertyPath path, Object value);

    /**
     * Removes the makeshift value for the property of the annotation represented by the current {@code Metadata}
     * instance
     * @param path Path to the property: a dot-delimited or slash-delimited string
     * @return The removed value
     */
    @SuppressWarnings("UnusedReturnValue")
    Object unsetValue(String path);

    /**
     * Retrieves an {@code Iterator} over the properties of the annotation represented by the current {@code Metadata}
     * @param deepRead     {@code True} to iterate over the properties of nested annotations, if there are any
     * @param expandArrays {@code True} to expand array properties into separate yielded items
     * @return {@code Iterator} instance
     */
    Iterator<Property> iterator(boolean deepRead, boolean expandArrays);

    /**
     * Retrieves a {@code Spliterator} over the properties of the annotation represented by the current
     * {@code Metadata}
     * @param deepRead     {@code True} to iterate over the properties of nested annotations, if there are any
     * @param expandArrays {@code True} to expand array properties into separate yielded items
     * @return {@code Spliterator} instance
     */
    Spliterator<Property> spliterator(boolean deepRead, boolean expandArrays);

    /**
     * Retrieves a {@code Stream} that returns properties of the annotation represented by the current {@code Metadata}.
     * No deep-read or array destructuring is done
     * @return {@code Stream} instance
     */
    Stream<Property> stream();

    /**
     * Retrieves a {@code Stream} over the properties of the annotation represented by the current {@code Metadata}
     * @param deepRead     {@code True} to iterate over the properties of nested annotations, if there are any
     * @param expandArrays {@code True} to expand array properties into separate yielded items
     * @return {@code Stream} instance
     */
    Stream<Property> stream(boolean deepRead, boolean expandArrays);

    /**
     * Creates a {@link Metadata}-based annotation proxy for the given annotation type
     * @param type {@code Class} reference, not null
     * @param <T>  Type of the annotation to retrieve
     * @return {@code Metadata}-based @code Annotation} instance
     */
    static <T extends Annotation> T from(Class<T> type) {
        return from(type, Collections.emptyMap());
    }

    /**
     * Creates a {@link Metadata}-based annotation proxy for the given annotation type exposing the provided property
     * values
     * @param type       {@code Class} reference, not null
     * @param properties Map of properties to expose
     * @param <T>        Type of the annotation to retrieve
     * @return {@code Metadata}-based @code Annotation} instance
     */
    @SuppressWarnings("unchecked")
    static <T extends Annotation> T from(Class<T> type, Map<String, Object> properties) {
        return (T) from(null, type, properties);
    }

    /**
     * Creates a {@link Metadata}-based object for the given source annotation
     * @param source {@code Annotation} object
     * @param <T>  Type of the annotation
     * @return {@code Metadata} instance
     */
    static <T extends Annotation> Metadata from(T source) {
        return from(source, null, null);
    }

    /**
     * Creates a {@link Metadata}-based object for the given source annotation and overriding property values
     * @param source {@code Annotation} object, not null
     * @param properties Map of properties to expose
     * @param <T>  Type of the annotation
     * @return {@code Metadata} instance
     */
    static <T extends Annotation> Metadata from(T source, Map<String, Object> properties) {
        return from(source, null, properties);
    }

    /**
     * Creates a {@link Metadata}-based object for the given source annotation, type, and overriding property values
     * @param source A nullable {@code Annotation} object
     * @param type {@code Class} reference. Must not be null if {@code source} is null
     * @param properties Map of properties to expose
     * @param <T>  Type of the annotation
     * @return {@code Metadata} instance
     */
    static <T extends Annotation> Metadata from(T source, Class<T> type, Map<String, Object> properties) {
        if (source instanceof Metadata) {
            if (properties != null && !properties.isEmpty()) {
                properties.forEach((key, value) -> ((Metadata) source).putValue(key, value));
            }
            return (Metadata) source;
        }
        Class<? extends Annotation> effectiveType = source != null ? source.annotationType() : type;
        MetadataHandler<T> interfaceHandler = source != null
            ? new MetadataHandler<>(source, properties)
            : new MetadataHandler<>(type, properties);
        Object newInstance = Proxy.newProxyInstance(
            PluginRuntime.context().getReflection().getClassLoader(),
            new Class[]{effectiveType, Metadata.class},
            interfaceHandler);
        return (Metadata) newInstance;
    }
}
