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
import java.lang.reflect.Modifier;

import com.exadel.aem.toolkit.plugin.util.PluginReflectionUtility;

public class FieldSourceImpl extends SourceImpl {

    private final Field field;

    public FieldSourceImpl(Field field, Class<?> reportingClass) {
        super(reportingClass);
        this.field = field;
    }

    @Override
    public String getName() {
        return field.getName();
    }

    @Override
    public Class<?> getDeclaringClass() {
        return field.getDeclaringClass();
    }

    @Override
    Class<?> getPlainReturnType() {
        return PluginReflectionUtility.getPlainType(field);
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

    @Override
    public boolean isValid() {
        return field != null && !field.getDeclaringClass().isInterface() && !Modifier.isStatic(field.getModifiers());
    }

    @Override
    public <T> T adaptTo(Class<T> adaptation) {
        if (adaptation.equals(Field.class) || adaptation.equals(Member.class)) {
            return adaptation.cast(field);
        }
        return super.adaptTo(adaptation);
    }
}
