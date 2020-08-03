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

package com.exadel.aem.toolkit.core.util;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.ImmutableMap;

import com.exadel.aem.toolkit.api.annotations.main.ClassField;
import com.exadel.aem.toolkit.api.markers._Super;
import com.exadel.aem.toolkit.api.markers._This;

/**
 * Contains utility methods that help to manage {@link ClassField} instances
 */
public class PluginClassFieldUtility {

    /**
     * Default (private) constructor
     */
    private PluginClassFieldUtility() {
    }

    /**
     * Used to prepare a {@code ClassField instance} in a way that the {@code source} property when set to a placeholder
     * is populated with an appropriate class reference, and the {@code field} property guaranteed to expose an actual
     * field name
     * @param source {@link ClassField} instance to populate
     * @param currentField The current {@code Field} instance to work with
     * @return The proxied {@code ClassField} possessing actual values
     */
    static ClassField populateDefaults(ClassField source, Field currentField) {
        if (!ObjectUtils.allNotNull(source, currentField)) {
            return source;
        }
        return PluginObjectUtility.modify(
                source,
                ClassField.class,
                ImmutableMap.of(
                        DialogConstants.PN_SOURCE_CLASS, cls -> populateClassPlaceholder((Class<?>) cls, currentField),
                        DialogConstants.PN_FIELD, value -> StringUtils.defaultIfEmpty(value.toString(), currentField.getName())
                ));
    }

    /**
     * Used to prepare a {@code ClassField instance} in a way that the {@code source} property when set to a placeholder
     * is populated with an appropriate class reference
     * @param source {@link ClassField} instance to populate
     * @param currentClass The current {@code Class} instance to work with
     * @return The proxied {@code ClassField} possessing actual values
     */
    public static ClassField populateDefaults(ClassField source, Class<?> currentClass) {
        if (!ObjectUtils.allNotNull(source, currentClass)) {
            return source;
        }
        return PluginObjectUtility.modify(
                source,
                ClassField.class,
                ImmutableMap.of(
                        DialogConstants.PN_SOURCE_CLASS, cls -> populateClassPlaceholder((Class<?>) cls, currentClass)
                ));
    }

    /**
     * Called by {@link PluginClassFieldUtility#populateDefaults(ClassField, Field)} to replace a placeholder
     * in {@code source} property with an actual class reference
     * @param source The class reference as specified in a {@code ClassField} instance
     * @param currentField The current {@code Field} instance to work with
     * @return Same class reference if actual class provided, or a replacement class reference if a placeholder placeholder
     */
    private static Class<?> populateClassPlaceholder(Class<?> source, Field currentField) {
        if (source.equals(_This.class)) {
            return currentField.getDeclaringClass();
        } else if (source.equals(_Super.class)) {
            return getSuperclassContainingField(currentField.getDeclaringClass(), currentField.getName());
        }
        return source;
    }


    /**
     * Called by {@link PluginClassFieldUtility#populateDefaults(ClassField, Class)} to replace a placeholder
     * in {@code source} property with an actual class reference
     * @param source The class reference as specified in a {@code ClassField} instance
     * @param currentClass The current {@code Class<?>} instance to work with
     * @return Same class reference if actual class provided, or a replacement class reference if a placeholder placeholder
     */
    private static Class<?> populateClassPlaceholder(Class<?> source, Class<?> currentClass) {
        if (source.equals(_This.class)) {
            return currentClass;
        } else if (source.equals(_Super.class)) {
            return currentClass.getSuperclass() != null ? currentClass.getSuperclass() : currentClass;
        }
        return source;
    }

    /**
     * Called by {@link PluginClassFieldUtility#populateClassPlaceholder(Class, Field)} to find the closest superclass
     * of the target class that contains the specified field
     * @param currentClass The class to start analysis from
     * @param fieldName The field to search for
     * @return {@code Class<?>} instance, or null
     */
    private static Class<?> getSuperclassContainingField(Class<?> currentClass, String fieldName) {
        Class<?> result = currentClass.getSuperclass();
        while (result != null) {
            if (Arrays.stream(result.getDeclaredFields()).anyMatch(field -> field.getName().equals(fieldName))) {
                return result;
            }
            result = result.getSuperclass();
        }
        return null;
    }
}
