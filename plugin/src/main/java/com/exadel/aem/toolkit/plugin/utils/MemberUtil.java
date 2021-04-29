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
package com.exadel.aem.toolkit.plugin.utils;

import java.lang.reflect.Field;
import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;

/**
 * Contains utility methods for retrieving info about Java class members
 */
public class MemberUtil {

    /**
     * Default (instantiation-restricting) constructor
     */
    private MemberUtil() {
    }

    /**
     * Retrieves the type represented by the provided Java class {@code Member}. If the method is designed to provide
     * a primitive value or a singular object, its "direct" type is returned. If the method represents a collection,
     * type of array's element is returned
     * @param member The member to analyze, a {@link Field} or {@link Method} reference expected
     * @return Appropriate {@code Class} instance or null if an invalid {@code Member} provided
     */
    public static Class<?> getPlainType(Member member) {
        if (!(member instanceof Field) && !(member instanceof Method)) {
            return null;
        }
        Class<?> result = member instanceof Field
            ? ((Field) member).getType()
            : ((Method) member).getReturnType();
        if (result.isArray()) {
            result = result.getComponentType();
        }
        if (ClassUtils.isAssignable(result, Collection.class)) {
            return getGenericType(member, result);
        }
        return result;
    }

    /**
     * Retrieves the underlying parameter type of the provided Java class {@code Member}. If the method is an array
     * or a collection, the item (parameter) type is returned; otherwise, the mere method type is returned
     * @param member       The member to analyze, a {@link Field} or {@link Method} reference expected
     * @param defaultValue The value to return if parameter type extraction fails
     * @return Extracted {@code Class} instance, or the {@code defaultValue}
     */
    private static Class<?> getGenericType(Member member, Class<?> defaultValue) {
        try {
            ParameterizedType fieldGenericType = member instanceof Field
                ? (ParameterizedType) ((Field) member).getGenericType()
                : (ParameterizedType) ((Method) member).getGenericReturnType();
            Type[] typeArguments = fieldGenericType.getActualTypeArguments();
            if (ArrayUtils.isEmpty(typeArguments)) {
                return defaultValue;
            }
            return (Class<?>) typeArguments[0];
        } catch (TypeNotPresentException | MalformedParameterizedTypeException e) {
            return defaultValue;
        }
    }
}
