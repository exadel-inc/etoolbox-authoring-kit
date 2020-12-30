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

package com.exadel.aem.toolkit.core.source;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

public class SourceFieldImpl extends SourceBase {

    private final Field field;

    public SourceFieldImpl(Field field, Class<?> processedClass) {
        super(processedClass);
        this.field = field;
    }

    @Override
    public String getName() {
        return field.getName();
    }

    @Override
    Class<?> getPlainType() {
        Class<?> result;
        result = getSourceType();
        if (ClassUtils.isAssignable(result, Collection.class)) {
            return getGenericType(result);
        }
        return result;
    }

    @Override
    public <T> T adaptTo(Class<T> target) {
        if (target.equals(Field.class)) {
            return target.cast(field);
        }
        return super.adaptTo(target);
    }
    
    private Class<?> getGenericType(Class<?> defaultValue) {
        try {
            ParameterizedType fieldGenericType;
            fieldGenericType = (ParameterizedType) field.getGenericType();
            Type[] typeArguments = fieldGenericType.getActualTypeArguments();
            if (ArrayUtils.isEmpty(typeArguments)) {
                return defaultValue;
            }
            return (Class<?>) typeArguments[0];
        } catch (TypeNotPresentException | MalformedParameterizedTypeException e) {
            return defaultValue;
        }
    }

    @Override
    Class<?> getSourceType() {
        return field.getType().isArray() ? field.getType().getComponentType() : field.getType();
    }

    @Override
    Annotation[] getDeclaredAnnotations() {
        return field.getDeclaredAnnotations();
    }

    @Override
    <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
        return field.getDeclaredAnnotation(annotationClass);
    }

    @Override
    <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
        return field.getAnnotationsByType(annotationClass);
    }
}
