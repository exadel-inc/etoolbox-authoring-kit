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
package com.exadel.aem.toolkit.core.injectors.utils;

/**
 * Represents a value that can have a fallback
 * <p><u>Note</u>: This class is not a part of the public API and is subject to change. Do not use it in your own
 * code</p>
 */
public class Defaultable {

    public static final Defaultable EMPTY = new Defaultable(null, false);

    private final Object value;
    private boolean isFallback;

    /**
     * Initializes a {@code Defaultable} instance with the specified value and fallback flag
     * @param value      An arbitrary nullable value to set
     * @param isFallback Fallback flag
     */
    private Defaultable(Object value, boolean isFallback) {
        this.value = value;
        this.isFallback = isFallback;
    }

    /**
     * Retrieves the value
     * @return A nullable object
     */
    public Object getValue() {
        return value;
    }

    /**
     * Gets whether the contained value is null, empty, or is an explicitly declared fallback
     * @return True or false
     */
    public boolean isDefault() {
        return isFallback || TypeUtil.isEmpty(value);
    }

    /**
     * Creates a non-fallback {@code Defaultable} instance with the specified value
     * @param value Value to set
     * @return {@code Defaultable} instance
     */
    public static Defaultable of(Object value) {
        if (value instanceof Defaultable) {
            return (Defaultable) value;
        }
        return new Defaultable(value, false);
    }

    /**
     * Creates a fallback {@code Defaultable} instance with the specified value
     * @param value Value to set
     * @return {@code Defaultable} instance
     */
    public static Defaultable fallback(Object value) {
        if (value instanceof Defaultable) {
            ((Defaultable) value).isFallback = true;
            return (Defaultable) value;
        }
        return new Defaultable(value, true);
    }

    /**
     * Gets whether the provided object is not a {@code Defaultable} instance in the "fallback" state> This is usually
     * needed to validate an injectable value
     * @param value Value to check
     * @return True or false
     */
    public static boolean isNotDefault(Object value) {
        return !(value instanceof Defaultable) || !((Defaultable) value).isDefault();
    }

    /**
     * Extracts the value from the provided reference that can be a {@link Defaultable} wrapper
     * @param value Extraction source
     * @return A nullable object
     */
    public static Object unwrap(Object value) {
        if (value instanceof Defaultable) {
            return ((Defaultable) value).getValue();
        }
        return value;
    }
}
