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
import java.util.stream.Stream;

import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.adapters.AdaptationBase;
import com.exadel.aem.toolkit.plugin.annotations.Metadata;
import com.exadel.aem.toolkit.plugin.annotations.Property;
import com.exadel.aem.toolkit.plugin.annotations.ScriptingHelper;

/**
 * Presents a basic implementation of {@link Source} that exposes the metadata that is specific for the underlying class
 * or class member
 */
abstract class SourceImpl extends AdaptationBase<Source> implements Source {

    private final Map<Class<?>, Object> metadata;

    /**
     * Initializes a {@link SourceImpl} object with reference to a Java entity capable of exposing annotations
     * @param annotated A {@link AnnotatedElement} instance, such as a method, a field, or a class
     */
    SourceImpl(AnnotatedElement annotated) {
        super(Source.class);
        metadata = collectMetadata(annotated);
        applyInterpolation(this, metadata);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T adaptTo(Class<T> type) {
        T result = tryAdaptToMetadataEntry(type);
        if (result != null) {
            return result;
        }
        return super.adaptTo(type);
    }

    private <T> T tryAdaptToMetadataEntry(Class<T> type) {
        if (type.isArray()) {
            if (type.getComponentType().equals(Annotation.class)) {
                Annotation[] result = metadata
                    .values()
                    .stream()
                    .flatMap(value -> value.getClass().isArray() ? Arrays.stream((Annotation[]) value) : Stream.of(value))
                    .map(Annotation.class::cast)
                    .toArray(Annotation[]::new);
                return type.cast(result);
            }
            if (type.getComponentType().isAnnotation() && metadata.containsKey(type)) {
                Object stored = metadata.get(type);
                Object newArray = Array.newInstance(type.getComponentType(), Array.getLength(stored));
                for (int i = 0; i < Array.getLength(stored); i++) {
                    Array.set(newArray, i, Array.get(stored, i));
                }
                return type.cast(newArray);
            }
            if (type.getComponentType().isAnnotation() && metadata.containsKey(type.getComponentType())) {
                Object newArray = Array.newInstance(type.getComponentType(), 1);
                Array.set(newArray, 0, metadata.get(type.getComponentType()));
                return type.cast(newArray);
            }
            if (type.getComponentType().isAnnotation()) {
                return type.cast(Array.newInstance(type.getComponentType(), 0));
            }
        }
        if (type.isAnnotation() && metadata.containsKey(type)) {
            return type.cast(metadata.get(type));
        }
        return null;
    }

    private static Map<Class<?>, Object> collectMetadata(AnnotatedElement value) {
        Map<Class<?>, Object> result = new LinkedHashMap<>();
        for (Annotation annotation : value.getDeclaredAnnotations()) {
            if (!annotation.annotationType().getPackage().getName().startsWith(CoreConstants.ROOT_PACKAGE)) {
                result.put(annotation.annotationType(), annotation);
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

    private static boolean isRepeatableContainer(Metadata value) {
        Property valueProperty = value.hasProperty(CoreConstants.PN_VALUE)
            ? value.getProperty(CoreConstants.PN_VALUE)
            : null;
        return valueProperty != null
            && valueProperty.getValue() != null
            && valueProperty.getType().isArray()
            && valueProperty.getComponentType().isAnnotation()
            && valueProperty.getComponentType().isAnnotationPresent(Repeatable.class);
    }

    private static void applyInterpolation(Source source, Map<Class<?>, Object> metadata) {
        metadata.forEach((key, value) -> {
            if (!key.isArray() && value instanceof Metadata) {
                ScriptingHelper.interpolate((Metadata) value, source);
            }
        });
    }
}
