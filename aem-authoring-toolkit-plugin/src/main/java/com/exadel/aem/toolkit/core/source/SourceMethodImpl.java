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

import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class SourceMethodImpl extends SourceBase {

    private final Method method;

    SourceMethodImpl(Method method, Class<?> processedClass) {
        super(processedClass);
        this.method = method;
    }
    @Override
    public String getName() {
        return method.getName();
    }

    @Override
    Class<?> getPlainType() {
        return PluginReflectionUtility.getPlainMethodType(method);
    }

    @Override
    Class<?> getSourceType() {
        return method.getReturnType();
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
}
