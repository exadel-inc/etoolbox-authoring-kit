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

package com.exadel.aem.toolkit.plugin.source;

import java.lang.annotation.Annotation;

import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;
import com.exadel.aem.toolkit.api.annotations.widgets.MultiField;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.markers._Default;
import com.exadel.aem.toolkit.plugin.adapters.AdaptationBase;

public abstract class SourceImpl extends AdaptationBase<Source> implements Source {

    private final Class<?> reportingClass;

    SourceImpl(Class<?> reportingClass) {
        super(Source.class);
        this.reportingClass = reportingClass;
    }

    @Override
    public Class<?> getReportingClass() {
        return this.reportingClass;
    }

    @Override
    public Class<?> getValueType() {
        // Retrieve the "immediate" return type
        Class<?> result = getPlainReturnType();
        // Then switch to directly specified type, if any
        if (getDeclaredAnnotation(MultiField.class) != null
            && getDeclaredAnnotation(MultiField.class).value() != _Default.class) {
            result = getDeclaredAnnotation(MultiField.class).value();
        } else if (getDeclaredAnnotation(MultiField.class) != null
            && getDeclaredAnnotation(MultiField.class).field() != _Default.class) {
            result = getDeclaredAnnotation(MultiField.class).field();
        } else if (getDeclaredAnnotation(FieldSet.class) != null
            && getDeclaredAnnotation(FieldSet.class).value() != _Default.class) {
            result = getDeclaredAnnotation(FieldSet.class).value();
        }
        return result;
    }

    abstract Class<?> getPlainReturnType();

    abstract Annotation[] getDeclaredAnnotations();

    abstract <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass);

    abstract <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass);

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
