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
package com.exadel.aem.toolkit.plugin.sources;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.lang3.ClassUtils;

import com.exadel.aem.toolkit.api.annotations.main.Setting;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.adapters.AdaptationBase;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.metadata.Metadata;
import com.exadel.aem.toolkit.plugin.metadata.Property;
import com.exadel.aem.toolkit.plugin.metadata.scripting.DataStack;
import com.exadel.aem.toolkit.plugin.metadata.scripting.ScriptingHelper;

/**
 * Presents a basic implementation of {@link Source} that exposes the metadata that is specific for the underlying class
 * or class member
 */
abstract class SourceImpl extends AdaptationBase<Source> implements Source {

    private final Map<Class<?>, Object> metadata;
    private boolean metadataProcessed = false;

    /**
     * Initializes a {@link SourceImpl} object that contains a reference to a Java entity capable of exposing
     * annotations
     * @param annotated A {@link AnnotatedElement} instance, such as a method, a field, or a class
     */
    SourceImpl(AnnotatedElement annotated) {
        super(Source.class);
        metadata = collectMetadata(annotated);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T adaptTo(Class<T> type) {
        T result = Stream.<Supplier<T>>of(
                () -> type == DataStack.class ? type.cast(getDataStack()) : null,
                () -> adaptToStoredAnnotation(type),
                () -> adaptToStoredAnnotationArray(type),
                () -> adaptToForeignAnnotation(type))
            .map(Supplier::get)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
        if (result != null) {
            return result;
        }
        return super.adaptTo(type);
    }

    /**
     * Called by {@link SourceImpl#adaptTo(Class)} to test whether it is possible to retrieve an annotation of the given
     * type stored in the current object's {@link Metadata} registry
     * @param type {@code Class} reference that represents the annotation type
     * @param <T>  Type of the annotation to retrieve
     * @return An annotation instance; or else {@code null}
     */
    private <T> T adaptToStoredAnnotation(Class<T> type) {
        if (!type.isAnnotation() || !getMetadata().containsKey(type)) {
            return null;
        }
        return type.cast(getMetadata().get(type));
    }

    /**
     * Called by {@link SourceImpl#adaptTo(Class)} to test whether it is possible to retrieve an array of annotations of
     * the given type stored in the current object's {@link Metadata} registry
     * @param type {@code Class} reference that represents the annotation type
     * @param <T>  Type of the annotation to retrieve
     * @return An array of annotations; or else {@code null}
     */
    private <T> T adaptToStoredAnnotationArray(Class<T> type) {
        if (!type.isArray()) {
            return null;
        }
        if (type.getComponentType().equals(Annotation.class)) {
            Annotation[] result = getMetadata()
                .values()
                .stream()
                .flatMap(value -> value.getClass().isArray() ? Arrays.stream((Annotation[]) value) : Stream.of(value))
                .map(Annotation.class::cast)
                .toArray(Annotation[]::new);
            return type.cast(result);
        }
        if (type.getComponentType().isAnnotation() && getMetadata().containsKey(type)) {
            Object stored = getMetadata().get(type);
            Object newArray = Array.newInstance(type.getComponentType(), Array.getLength(stored));
            for (int i = 0; i < Array.getLength(stored); i++) {
                Array.set(newArray, i, Array.get(stored, i));
            }
            return type.cast(newArray);
        }
        if (type.getComponentType().isAnnotation() && getMetadata().containsKey(type.getComponentType())) {
            Object newArray = Array.newInstance(type.getComponentType(), 1);
            Array.set(newArray, 0, getMetadata().get(type.getComponentType()));
            return type.cast(newArray);
        }
        if (type.getComponentType().isAnnotation()) {
            return type.cast(Array.newInstance(type.getComponentType(), 0));
        }
        return null;
    }

    /**
     * Called by {@link SourceImpl#adaptTo(Class)} to test whether it is possible to retrieve an annotation of the given
     * type that is mediated by the current object's {@link Metadata} registry
     * @param type {@code Class} reference that represents the annotation type
     * @param <T>  Type of the annotation to retrieve
     * @return An annotation instance; or else {@code null}
     */
    @SuppressWarnings("unchecked")
    private <T> T adaptToForeignAnnotation(Class<T> type) {
        return ClassUtils.isAssignable(type, Annotation.class)
            ? (T) getAnnotation((Class<? extends Annotation>) type)
            : null;
    }

    /**
     * Retrieves an annotation of a particular type attached to the underlying entity
     * @param type {@code Class} of the annotation to get
     * @param <T>  Annotation type reflected by the {@code type} argument
     * @return {@code T}-typed annotation object
     */
    abstract <T extends Annotation> T getAnnotation(Class<T> type);

    /**
     * Retrieves a {@link DataStack} object for the current {@link Source}. The {@code DataStack} is used to interpolate
     * scripting templates
     * @return A non-null {@code DataStack} object. Can be empty if no data was gathered via {@link Setting} annotations
     */
    abstract DataStack getDataStack();

    /**
     * Retrieves the current object's {@link Metadata} registry. If requested for the first type, interpolates inline
     * script templates in the metadata objects' values with the ToolKit's scripting engine
     * @return A non-null {@code Map} object; can be empty
     */
    private Map<Class<?>, Object> getMetadata() {
        if (!metadataProcessed) {
            metadataProcessed = true;
            applyInterpolation(this, metadata);
        }
        return metadata;
    }

    /**
     * Collects metadata from the provided {@link AnnotatedElement} instance
     * @param value The {@code AnnotatedElement} to process
     * @return A non-null {@code Map} object; can be empty
     * @see Metadata
     */
    private static Map<Class<?>, Object> collectMetadata(AnnotatedElement value) {
        Map<Class<?>, Object> result = new LinkedHashMap<>();
        for (Annotation annotation : value.getDeclaredAnnotations()) {
            // We do not collect "foreign" annotations
            if (!annotation.annotationType().getPackage().getName().startsWith(CoreConstants.ROOT_PACKAGE)
                && !PluginRuntime.context().getReflection().isHandled(annotation)) {
                continue;
            }
            Metadata entry = Metadata.from(annotation);
            result.put(annotation.annotationType(), entry);
            if (isRepeatableContainer(entry)) {
                Property repeatableValues = entry.getProperty(CoreConstants.PN_VALUE);
                Metadata[] metadataEntries = Arrays.stream((Annotation[]) repeatableValues.getValue())
                    .map(Metadata::from)
                    .toArray(Metadata[]::new);
                result.put(repeatableValues.getType(), metadataEntries);
            }
        }
        return result;
    }

    /**
     * Tests whether the provided {@link Metadata} object represents a repeatable annotation container
     * @param value The {@code Metadata} to test
     * @return True or false
     */
    private static boolean isRepeatableContainer(Metadata value) {
        if (value == null) {
            return false;
        }
        Property valueProperty = value.hasProperty(CoreConstants.PN_VALUE)
            ? value.getProperty(CoreConstants.PN_VALUE)
            : null;
        return valueProperty != null
            && valueProperty.getValue() != null
            && valueProperty.getType().isArray()
            && valueProperty.getComponentType().isAnnotation()
            && valueProperty.getComponentType().isAnnotationPresent(Repeatable.class);
    }

    /**
     * Interpolates inline script templates in the metadata objects' values with the ToolKit's scripting engine
     * @param source   The {@link Source} instance to use as the interpolation context
     * @param metadata The {@code Map} of metadata objects to process
     */
    private static void applyInterpolation(Source source, Map<Class<?>, Object> metadata) {
        for (Object value : metadata.values()) {
            if (value instanceof Metadata[] && !value.getClass().getComponentType().equals(Setting.class)) {
                for (Metadata metadataEntry : (Metadata[]) value) {
                    ScriptingHelper.interpolate(metadataEntry, source);
                }
            } else if (value instanceof Metadata) {
                ScriptingHelper.interpolate((Metadata) value, source);
            }
        }
    }
}
