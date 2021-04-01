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
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import com.exadel.aem.toolkit.api.handlers.Source;

/**
 * Contains factory methods for creating {@link Source} instances
 */
public class Sources {

    /**
     * Default (instantiation-restricting) constructor
     */
    private Sources() {
    }

    /**
     * Creates a {@link Source} facade for a Java class member
     * @param value A {@code Method} or a {@code Field} for which a source facade is created
     * @return {@code Source} instance
     */
    public static Source fromMember(Member value) {
        return fromMember(value, null);
    }

    /**
     * Creates a {@link Source} facade for a Java class member
     * @param value          {@code Method} or a {@code Field} for which a source facade is created
     * @param reportingClass {@code Class<?>} pointer determining the class that "reports" to the ToolKit Maven plugin
     *                       about the current member (can be a class where this member was declared or a descendant of
     *                       some superclass that uses the member for UI rendering)
     * @return {@code Source} instance
     */
    public static Source fromMember(Member value, Class<?> reportingClass) {
        return value instanceof Field
            ? new FieldSourceImpl((Field) value, reportingClass)
            : new MethodSourceImpl((Method) value, reportingClass);
    }

    /**
     * Creates a {@link Source} facade for a Java class
     * @param value {@code Class} object for which a source facade is created
     * @return {@code Source} instance
     */
    public static Source fromClass(Class<?> value) {
        return new ClassSourceImpl(value);
    }

    /**
     * Creates a {@link Source} facade for a Java annotation
     * @param value {@code Annotation} object for which a source facade is created
     * @return {@code Source} instance
     */
    public static Source fromAnnotation(Annotation value) {
        return new AnnotationSourceImpl(value);
    }
}
