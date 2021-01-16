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
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collection;

import org.apache.commons.lang3.ClassUtils;

import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;
import com.exadel.aem.toolkit.api.annotations.widgets.MultiField;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.markers._Default;

public abstract class SourceBase implements Source {

    private final Class<?> processedClass;

    SourceBase(Class<?> processedClass) {
        this.processedClass = processedClass;
    }

    @Override
    public <T> T adaptTo(Class<T> target) {
        if (target == null) {
            return null;
        }
        if (target.isAnnotation()) {
            return target.cast(getDeclaredAnnotation((Class<? extends Annotation>) target));
        }
        if (target.isArray()) {
            if (target.getComponentType().equals(Annotation.class)) {
                return target.cast(getDeclaredAnnotations());
            } else {
                return target.cast(getAnnotationsByType((Class<? extends Annotation>) target.getComponentType()));
            }
        }
        return null;
    }

    @Override
    public Class<?> getProcessedClass() {
        return this.processedClass;
    }

    @Override
    public Class<?> getContainerClass() {
        // Extract underlying source's type as is
        Class<?> result = getSourceType();
        // Try to retrieve collection's parameter type
        if (ClassUtils.isAssignable(result, Collection.class)) {
            result = getPlainType();
        }
        // Switch to directly specified type, if any
        if (getDeclaredAnnotation(MultiField.class) != null
            && getDeclaredAnnotation(MultiField.class).field() != _Default.class) {
            result = getDeclaredAnnotation(MultiField.class).field();
        } else if (getDeclaredAnnotation(FieldSet.class) != null
            && getDeclaredAnnotation(FieldSet.class).source() != _Default.class) {
            result = getDeclaredAnnotation(FieldSet.class).source();
        }
        return result;
    }

    abstract Class<?> getPlainType();

    abstract Class<?> getSourceType();

    abstract Annotation[] getDeclaredAnnotations();

    abstract <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass);

    abstract <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass);

    public static Source fromMember(Member member, Class<?> processedClass) {
        return member instanceof Field
            ? new SourceFieldImpl((Field) member, processedClass)
            : new SourceMethodImpl((Method) member, processedClass);
    }
}
