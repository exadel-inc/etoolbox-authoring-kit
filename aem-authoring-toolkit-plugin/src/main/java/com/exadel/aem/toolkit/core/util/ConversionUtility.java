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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.widgets.datepicker.DateTimeValue;

/**
 * Contains utility methods to perform {@code annotation - to - plain Java object} and {@code anntotation - to - annotation}
 * value conversions for the sake of proper dialog markup rendering
 */
public class ConversionUtility {
    private static final String METHOD_TO_STRING = "toString";
    private static final String METHOD_ANNOTATION_TYPE = "annotationType";

    private ConversionUtility() {}

    /**
     * Generates a specific {@code Annotation}-typed facade of the specified annotation with only some fields
     * set to a non-default value.
     * This method is used to render the same {@code Annotation} object differently (like two or more different sets of values)
     * depending on the values specified
     * @param source {@code Annotation} instance to produce a facade for
     * @param annotationType Target {@code Class} of the facade (one of subtypes of the {@code Annotation} class)
     * @param exposedFields The fields to be exposed in the facade, set on non-null Strings
     * @return Facade instance, subtype of the {@code Annotation} class
     */
    public static <T extends Annotation> T getFilteredAnnotation(Annotation source, Class<T> annotationType, String[] exposedFields) {
        return annotationType.cast(Proxy.newProxyInstance(annotationType.getClassLoader(),
                new Class[] {annotationType},
                getAnnotationProxyInvocationHandler(source, annotationType, exposedFields)
        ));
    }

    /**
     * Creates Java {@code Temporal} from {@code DateTimeValue} annotation
     * @param obj {@code DateTimeValue} annotation instance
     * @return Canonical Temporal instance, or null
     */
    public static Temporal getDateTimeInstance(Object obj) {
        DateTimeValue dt = (DateTimeValue) obj;
        try {
            if (StringUtils.isNotBlank(dt.timezone())) { // the following induces exception if any of DateTime parameters is invalid
                return ZonedDateTime.of(dt.year(), dt.month(), dt.day(), dt.hour(), dt.minute(), 0, 0, ZoneId.of(dt.timezone()));
            }
            return LocalDateTime.of(dt.year(), dt.month(), dt.day(), dt.hour(), dt.minute());
        } catch (DateTimeException e) {
            return null;
        }
    }

    /**
     * Creates invocation handler for the {@link ConversionUtility#getFilteredAnnotation(Annotation, Class, String[])} routine
     * @param source {@code Annotation} instance to produce a facade for
     * @param annotationType Target {@code Class} of the facade (one of subtypes of the {@code Annotation} class)
     * @param exposedFields The fields to be exposed in the facade, set on non-null Strings
     * @return {@link InvocationHandler} instance
     */
    private static InvocationHandler getAnnotationProxyInvocationHandler(Annotation source,
                                                                         Class<? extends Annotation> annotationType,
                                                                         String[] exposedFields) {
        return (proxy, method, args) -> {
            if (method.getName().equals(METHOD_ANNOTATION_TYPE)) {
                return annotationType; // for XmlUtility introspection
            }
            if (method.getName().equals(METHOD_TO_STRING)) {
                return annotationType.getCanonicalName(); // for debugging purposes
            }
            Method baseMethod = ArrayUtils.contains(exposedFields, method.getName())
                    ? annotationType.getDeclaredMethod(method.getName())
                    : null;
            if (baseMethod == null) {
                return method.getReturnType().equals(String.class) ? "" : 0L;
            }
            return baseMethod.invoke(source);
        };
    }
}
