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

import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.plugin.adapters.AdaptationBase;

/**
 * Presents a basic implementation of {@link Source} that exposes the metadata that is specific for the underlying class
 * or class member
 */
public abstract class SourceImpl extends AdaptationBase<Source> implements Source {

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
    public <T> T adaptTo(Class<T> adaptation) {
        if (adaptation == null) {
            return null;
        }
        if (adaptation.isArray()) {
            if (adaptation.getComponentType().equals(Annotation.class)) {
                return adaptation.cast(getDeclaredAnnotations());
            } else if (adaptation.getComponentType().isAnnotation()) {
                @SuppressWarnings("unchecked")
                Class<? extends Annotation> annotationClass = (Class<? extends Annotation>) adaptation.getComponentType();
                return adaptation.cast(getAnnotationsByType(annotationClass));
            }
        }
        if (adaptation.isAnnotation()) {
            @SuppressWarnings("unchecked")
            Class<? extends Annotation> annotationClass = (Class<? extends Annotation>) adaptation;
            return adaptation.cast(getDeclaredAnnotation(annotationClass));
        }
        return super.adaptTo(adaptation); // Retrieves adaptation value, if present, or null
    }
}
