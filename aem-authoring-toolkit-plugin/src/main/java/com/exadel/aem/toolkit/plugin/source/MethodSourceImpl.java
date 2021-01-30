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
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.exadel.aem.toolkit.plugin.util.PluginReflectionUtility;

public class MethodSourceImpl extends SourceImpl {

    private final Method method;

    MethodSourceImpl(Method method, Class<?> reportingClass) {
        super(reportingClass);
        this.method = method;
    }

    @Override
    public String getName() {
        return method.getName();
    }

    @Override
    public Class<?> getDeclaringClass() {
        return method.getDeclaringClass();
    }

    @Override
    Class<?> getPlainReturnType() {
        return PluginReflectionUtility.getPlainType(method);
    }

    @Override
    Annotation[] getDeclaredAnnotations() {
        return method.getDeclaredAnnotations();
    }

    @Override
    <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
        return method.getDeclaredAnnotation(annotationClass);
    }

    @Override
    <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
        return method.getAnnotationsByType(annotationClass);
    }

    @Override
    public boolean isValid() {
        return method != null && !Modifier.isStatic(method.getModifiers());
    }

    @Override
    public <T> T adaptTo(Class<T> adaptation) {
        if (adaptation.equals(Method.class) || adaptation.equals(Member.class)) {
            return adaptation.cast(method);
        }
        return super.adaptTo(adaptation);
    }
}
