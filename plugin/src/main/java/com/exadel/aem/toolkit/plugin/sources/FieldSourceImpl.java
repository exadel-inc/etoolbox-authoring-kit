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
public class FieldSourceImpl extends MemberSourceImpl {

    private final Field field;

    /**
     * Initializes a class instance with references to the managed field and the {@code Class} the current field is
     * reported by
     * @param field          {@code Field} object
     * @param reportingClass {@code Class} reference
     */
    public FieldSourceImpl(Field field, Class<?> reportingClass) {
        super(reportingClass);
        this.field = field;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return field != null ? field.getName() : StringUtils.EMPTY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getDeclaringClass() {
        return field != null ? field.getDeclaringClass() : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Class<?> getPlainReturnType() {
        return MemberUtil.getPlainType(field);
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
    public <T> T adaptTo(Class<T> adaptation) {
        if (adaptation.equals(Field.class) || adaptation.equals(Member.class)) {
            return adaptation.cast(field);
        }
        return super.adaptTo(adaptation);
    }
}
