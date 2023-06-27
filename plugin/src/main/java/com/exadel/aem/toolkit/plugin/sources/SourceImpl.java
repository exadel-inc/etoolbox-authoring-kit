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
import java.lang.reflect.Array;

import org.apache.commons.lang3.ArrayUtils;

import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.plugin.adapters.AdaptationBase;
import com.exadel.aem.toolkit.plugin.annotations.Metadata;
import com.exadel.aem.toolkit.plugin.annotations.ScriptingHelper;

/**
 * Presents a basic implementation of {@link Source} that exposes the metadata that is specific for the underlying class
 * or class member
 */
abstract class SourceImpl extends AdaptationBase<Source> implements Source {

    /**
     * Default constructor
     */
    SourceImpl() {
        super(Source.class);
    }

    /**
     * Retrieves annotations attached to the underlying entity
     * @return Array of {@code Annotation} objects
     */
    abstract Annotation[] getDeclaredAnnotations();

    /**
     * Retrieves annotations of a particular type attached to the underlying entity
     * @param annotationClass {@code Class} of the annotations
     * @param <T>             Annotation type reflected by the {@code annotationClass} argument
     * @return Array of {@code Annotation} objects
     */
    abstract <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass);

    /**
     * Retrieves an annotation of particular type attached to the underlying entity
     * @param annotationClass {@code Class} of the annotation to get
     * @param <T> Annotation type reflected by the {@code annotationClass} argument
     * @return {@code T}-typed annotation object
     */
    abstract <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass);

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T adaptTo(Class<T> type) {
        if (type == null) {
            return null;
        }
        if (type.isArray()) {
            if (type.getComponentType().equals(Annotation.class)) {
                return type.cast(getDeclaredAnnotations());
            } else if (type.getComponentType().isAnnotation()) {
                return adaptToAnnotationArray(type);
            }
        }
        if (type.isAnnotation()) {
            return adaptToAnnotation(type);
        }
        return super.adaptTo(type);
    }

    private <T> T adaptToAnnotationArray(Class<T> type) {
        T cachedAdaptationArray = getAdaptation(type);
        if (cachedAdaptationArray != null) {
            return type.cast(cachedAdaptationArray);
        }
        @SuppressWarnings("unchecked")
        Annotation[] annotations = getAnnotationsByType((Class<? extends Annotation>) type.getComponentType());
        if (ArrayUtils.isEmpty(annotations)) {
            return type.cast(annotations);
        }
        Object newArray = Array.newInstance(type.getComponentType(), annotations.length);
        for (int i = 0; i < annotations.length; i++) {
            Array.set(newArray, i, Metadata.from(annotations[i]));
        }
        return type.cast(newArray);
    }

    private <T> T adaptToAnnotation(Class<T> type) {
        Object cachedAdaptation = getAdaptation(type);
        if (cachedAdaptation != null) {
            return type.cast(cachedAdaptation);
        }
        @SuppressWarnings("unchecked")
        Annotation annotation = getDeclaredAnnotation((Class<? extends Annotation>) type);
        if (annotation == null) {
            return null;
        }
        Metadata metadata = Metadata.from(annotation);
        ScriptingHelper.interpolate(metadata, this);
        storeAdaptation(type, metadata);
        return type.cast(metadata);
    }
}
