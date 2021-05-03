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
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.plugin.utils.MemberUtil;

/**
 * Implements {@link Source} to expose the metadata that is specific for the underlying class method
 */
public class MethodSourceImpl extends MemberSourceImpl {

    private final Method method;

    /**
     * Initializes a class instance with references to the managed method and the {@code Class} the current method is
     * reported by
     * @param method         {@code Method} object
     * @param reportingClass {@code Class} reference
     */
    MethodSourceImpl(Method method, Class<?> reportingClass) {
        super(reportingClass);
        this.method = method;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return method != null ? method.getName() : StringUtils.EMPTY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getDeclaringClass() {
        return method != null ? method.getDeclaringClass() : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Class<?> getPlainReturnType() {
        return MemberUtil.getPlainType(method);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Annotation[] getDeclaredAnnotations() {
        return method != null ? method.getDeclaredAnnotations() : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
        return method != null ? method.getAnnotationsByType(annotationClass) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
        return method != null ? method.getDeclaredAnnotation(annotationClass) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
        return method != null
            && !Modifier.isStatic(method.getModifiers())
            && isWidgetAnnotationPresent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T adaptTo(Class<T> adaptation) {
        if (adaptation.equals(Method.class) || adaptation.equals(Member.class)) {
            return adaptation.cast(method);
        }
        return super.adaptTo(adaptation);
    }
}
