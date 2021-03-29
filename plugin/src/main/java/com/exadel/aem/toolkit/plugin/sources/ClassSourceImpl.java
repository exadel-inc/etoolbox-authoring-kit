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

import org.apache.commons.lang3.StringUtils;

public class ClassSourceImpl extends SourceImpl {

    private final Class<?> value;

    ClassSourceImpl(Class<?> value) {
        this.value = value;
    }

    @Override
    public String getName() {
        return isValid() ? value.getName() : StringUtils.EMPTY;
    }

    @Override
    Annotation[] getDeclaredAnnotations() {
        return value != null ? value.getDeclaredAnnotations() : null;
    }

    @Override
    <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
        return value != null ? value.getDeclaredAnnotation(annotationClass) : null;
    }

    @Override
    <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
        return value != null ? value.getAnnotationsByType(annotationClass) : null;
    }

    @Override
    public boolean isValid() {
        return value != null;
    }

    @Override
    public <T> T adaptTo(Class<T> adaptation) {
        if (Class.class.equals(adaptation)) {
            return adaptation.cast(value);
        }
        return super.adaptTo(adaptation);
    }
}
