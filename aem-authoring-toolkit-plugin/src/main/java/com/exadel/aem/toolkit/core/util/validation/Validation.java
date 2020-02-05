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
package com.exadel.aem.toolkit.core.util.validation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.meta.Validator;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestriction;
import com.exadel.aem.toolkit.core.exceptions.ValidationException;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;

/**
 * Performs validation of annotations properties' values with use of specific {@link Validator}s
 */
public class Validation {
    private static final String LOG_PLAIN_VALUE_PATTERN = "Property '%s' of @%s is set to a wrong value: '%s' provided, %s";
    private static final String LOG_ANNOTATION_PATTERN = "Annotation @%s(%s) is invalid: %s";
    private static final String ARGUMENT_LIST_OPENER = "(";
    private static final String ARGUMENT_LIST_FINISHER = ")";

    private static final Validator NO_RESTRICTION = new PermissiveValidator();

    private Method reflectedMethod;
    private Validator testRoutine;

    /**
     * Default constructor
     *
     * @param testRoutine {@link Validator} instance to be used for testing
     */
    private Validation(Validator testRoutine) {
        this.testRoutine = testRoutine;
    }

    /**
     * Retrieves appropriate {@code Validation} for specific annotation property
     *
     * @param type       Annotation type
     * @param methodName Annotation's property name
     * @return {@code Validation} instance
     */
    public static Validation forMethod(Class<? extends Annotation> type, String methodName) {
        try {
            return forMethod(type.getDeclaredMethod(methodName));
        } catch (NoSuchMethodException e) {
            return new Validation(NO_RESTRICTION);
        }
    }

    /**
     * Retrieves appropriate {@code Validation} for specific annotation property
     * @param method {@code Method} instance representing the annotation property
     * @return {@code Validation} instance
     */
    private static Validation forMethod(Method method) {
        String restriction = null;
        if (method.isAnnotationPresent(ValueRestriction.class)) {
            restriction = method.getDeclaredAnnotation(ValueRestriction.class).value();
        } else if (ClassUtils.isAssignable(PluginReflectionUtility.getMethodPlainType(method), Annotation.class)
                && PluginReflectionUtility.getMethodPlainType(method).isAnnotationPresent(ValueRestriction.class)) {
            restriction = PluginReflectionUtility.getMethodPlainType(method).getDeclaredAnnotation(ValueRestriction.class).value();
        }
        Validation checker = new Validation(getTestRoutine(restriction));
        checker.reflectedMethod = method;
        return checker;
    }

    /**
     * Retrieves appropriate {@code Validation} for specific annotation type
     * @param type Annotation type
     * @return {@code Validation} instance
     */
    public static Validation forType(Class<? extends Annotation> type) {
        String restriction = type.isAnnotationPresent(ValueRestriction.class)
                ? type.getDeclaredAnnotation(ValueRestriction.class).value()
                : null;
        return new Validation(getTestRoutine(restriction));
    }

    /**
     * Gets whether specific value is validated
     * @param value Raw value
     * @return True or false
     */
    public boolean test(Object value) {
        if (!testRoutine.isApplicableTo(value)) {
            return true;
        }
        boolean result = testRoutine.test(value);
        if (!result) {
            PluginRuntime.context().getExceptionHandler().handle(new ValidationException(getLogMessage(value)));
        }
        return result;
    }

    /**
     * Retrieves {@link Validator} instance for specific class name
     * @param name Validator name
     * @return {@code Validator} instance
     */
    private static Validator getTestRoutine(String name) {
        return PluginRuntime.context().getReflectionUtility().getValidators().getOrDefault(name, NO_RESTRICTION);
    }

    /**
     * Formats log warnings according to particular {@link Validator}s output
     * @param value Value to test
     * @return Message string
     */
    private String getLogMessage(Object value) {
        if (value != null && ClassUtils.isAssignable(value.getClass(), Annotation.class)) {
            String argumentSubstring = StringUtils.substringBetween(value.toString(), ARGUMENT_LIST_OPENER, ARGUMENT_LIST_FINISHER);
            if (argumentSubstring == null) argumentSubstring = value.toString();
            return String.format(LOG_ANNOTATION_PATTERN,
                    ((Annotation) value).annotationType().getSimpleName(),
                    argumentSubstring,
                    testRoutine.getWarningMessage());
        }
        return String.format(LOG_PLAIN_VALUE_PATTERN,
                reflectedMethod.getName(),
                reflectedMethod.getDeclaringClass().getSimpleName(),
                value,
                this.testRoutine.getWarningMessage());
    }
}

