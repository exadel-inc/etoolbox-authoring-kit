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
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Contains a property representation for the {@link Metadata} instance. Provides methods to access and modify the
 * property's value as well as retrieve the property's metadata such as type, component type, and annotations
 */
public class Property {

    static final Property EMPTY = new Property(null, null);

    private final String path;
    private Object value;

    /**
     * Constructs a new {@link Property} instance with the specified path and value
     * @param path  The path of the property. Can be {@code null}
     * @param value The initial value of the property. Can be {@code null}
     */
    Property(String path, Object value) {
        this.path = path;
        this.value = value;
    }

    /**
     * @return Retrieves the name of the property. Equals to the value of {@link Property#getPath()} unless overridden
     * in a derived class
     */
    public String getName() {
        return getPath();
    }

    /**
     * Retrieves the path of the property. It can be presented in either "plain" or "hierarchical"
     * (slash-delimited) form. In the latter case, it contains information about the nested {@link Metadata}
     * entities enclosed in the current metadata, e.g., an annotation-typed property
     * @return The path of the property
     */
    public String getPath() {
        return path;
    }

    /**
     * Retrieves the type of the property's value
     * @return {@link Class} object representing the type of the property's value, or {@code null} if the value is
     * missing
     */
    public Class<?> getType() {
        return getValue() != null ? getValue().getClass() : null;
    }

    /**
     * Retrieves the component type of the property's value. This information  is must know, e.g., if the value is of an
     * array type
     * @return The {@link Class} object that represents the component type of the property's value if it is an array or
     * the "direct" type of the property's value otherwise. Returns {@code null} if the value is {@code null}
     */
    public Class<?> getComponentType() {
        Class<?> result = getType();
        if (result == null) {
            return null;
        }
        return result.isArray() ? result.getComponentType() : result;
    }

    /**
     * Retrieves the annotation of the specified type attached to the class member that is reflected by this
     * {@link Property}
     * @param type The {@link Class} object that represents the type of the annotation to retrieve
     * @param <T> The type of the annotation to retrieve
     * @return The annotation of the specified type, or {@code null} if no such annotation is present
     */
    public <T extends Annotation> T getAnnotation(Class<T> type) {
        return null;
    }

    /**
     * Retrieves the value of the property
     * @return The value of the property
     */
    public Object getValue() {
        return value;
    }

    /**
     * Retrieves the default value of the property
     * @return The default value of the property
     */
    public Object getDefaultValue() {
        return getValue();
    }

    /**
     * Checks if the value of the property is equal to its default value
     * @return {@code True} if the value of the property is its default value, {@code false} otherwise
     */
    public boolean valueIsDefault() {
        return Objects.deepEquals(getValue(), getDefaultValue());
    }

    /**
     * Sets the value of the property
     * @param value The new value for the property. Can be {@code null}
     */
    void setValue(Object value) {
        this.value = value;
    }

    /**
     * Checks if the property matches the specified filter
     * @param filter The {@link Predicate} to use for the check
     * @return {@code true} if the property matches the filter, {@code false} otherwise
     */
    public boolean matches(Predicate<Method> filter) {
        return false;
    }
}
