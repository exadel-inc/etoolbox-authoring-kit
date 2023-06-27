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
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.plugin.utils.MemberUtil;

/**
 * Implements {@link Source} to expose the metadata that is specific for the underlying class field
 */
class FieldSourceImpl extends MemberSourceImpl {

    private final Field field;
    private String name;
    private Class<?> declaringClass;

    /**
     * Initializes a class instance with a references to the managed field
     * @param field          {@code Field} object
     */
    FieldSourceImpl(Field field) {
        this.field = field;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        if (StringUtils.isNotBlank(name)) {
            return name;
        }
        return field != null ? field.getName() : StringUtils.EMPTY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setName(String value) {
        name = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getDeclaringClass() {
        if (declaringClass != null) {
            return declaringClass;
        }
        return field != null ? field.getDeclaringClass() : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDeclaringClass(Class<?> value) {
        declaringClass = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Class<?> getPlainReturnType() {
        return MemberUtil.getComponentType(field);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Annotation[] getDeclaredAnnotations() {
        return field != null ? field.getDeclaredAnnotations() : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
        return field != null ? field.getAnnotationsByType(annotationClass) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
        return field != null ? field.getDeclaredAnnotation(annotationClass) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
        return field != null
            && !field.getDeclaringClass().isInterface()
            && !Modifier.isStatic(field.getModifiers())
            && isWidgetAnnotationPresent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T adaptTo(Class<T> type) {
        if (type.equals(Field.class) || type.equals(Member.class)) {
            return type.cast(field);
        }
        return super.adaptTo(type);
    }
}
