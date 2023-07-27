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
package com.exadel.aem.toolkit.plugin.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.function.Predicate;

import com.exadel.aem.toolkit.plugin.utils.MemberUtil;

/**
 * Extends {@link Property} to manifest a property that represents a {@link Method} instance: e.g., an annotation
 * method
 */
class MethodBackedProperty extends Property {

    private final Method method;

    /**
     * Initializes a {@code MethodBackedProperty} instance with the specified path, method, and value
     * @param path   The path of the property. A non-null value is expected
     * @param method The method which is the property source. A non-null value is expected
     * @param value  The initial value of the property
     */
    MethodBackedProperty(String path, Method method, Object value) {
        super(path, value);
        this.method = method;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return method.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Annotation> T getAnnotation(Class<T> type) {
        return method.getDeclaredAnnotation(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getDefaultValue() {
        return method.getDefaultValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getType() {
        return method.getReturnType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getComponentType() {
        return MemberUtil.getComponentType(method);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matches(Predicate<Method> filter) {
        return filter == null || filter.test(method);
    }
}
