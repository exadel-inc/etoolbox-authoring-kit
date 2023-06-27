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
package com.exadel.aem.toolkit.plugin.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Predicate;

public class Property {

    static final Property EMPTY = new Property(null, null);

    private String path;
    private Object value;

    Property(String path, Object value) {
        this.path = path;
        this.value = value;
    }

    public String getName() {
        return getPath();
    }

    public String getPath() {
        return path;
    }

    public Class<?> getType() {
        return getValue() != null ? getValue().getClass() : null;
    }

    public Class<?> getComponentType() {
        Class<?> result = getType();
        if (result == null) {
            return null;
        }
        return result.isArray() ? result.getComponentType() : result;
    }

    public <T extends Annotation> T getAnnotation(Class<T> type) {
        return null;
    }

    public Object getValue() {
        return value;
    }

    public Object getDefaultValue() {
        return getValue();
    }

    public boolean valueIsDefault() {
        return Objects.deepEquals(getValue(), getDefaultValue());
    }

    void setValue(Object value) {
        this.value = value;
    }

    public boolean matches(Predicate<Method> filter) {
        return false;
    }
}
