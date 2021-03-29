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

import com.exadel.aem.toolkit.plugin.util.MemberUtil;

public class FieldSourceImpl extends MemberSourceImpl {

    private final Field field;

    public FieldSourceImpl(Field field, Class<?> reportingClass) {
        super(reportingClass);
        this.field = field;
    }

    @Override
    public String getName() {
        return field != null ? field.getName() : StringUtils.EMPTY;
    }

    @Override
    public Class<?> getDeclaringClass() {
        return field != null ? field.getDeclaringClass() : null;
    }

    @Override
    Class<?> getPlainReturnType() {
        return MemberUtil.getPlainType(field);
    }

    @Override
    Annotation[] getDeclaredAnnotations() {
        return field != null ? field.getDeclaredAnnotations() : null;
    }

    @Override
    <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
        return field != null ? field.getDeclaredAnnotation(annotationClass) : null;
    }

    @Override
    <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
        return field != null ? field.getAnnotationsByType(annotationClass) : null;
    }

    @Override
    public boolean isValid() {
        return field != null
            && !field.getDeclaringClass().isInterface()
            && !Modifier.isStatic(field.getModifiers())
            && isWidgetAnnotationPresent();
    }

    @Override
    public <T> T adaptTo(Class<T> adaptation) {
        if (adaptation.equals(Field.class) || adaptation.equals(Member.class)) {
            return adaptation.cast(field);
        }
        return super.adaptTo(adaptation);
    }
}
