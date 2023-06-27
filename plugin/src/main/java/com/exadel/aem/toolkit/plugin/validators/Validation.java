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
package com.exadel.aem.toolkit.plugin.validators;

import java.lang.annotation.Annotation;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.meta.Validator;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestriction;
import com.exadel.aem.toolkit.plugin.annotations.Property;
import com.exadel.aem.toolkit.plugin.exceptions.ValidationException;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;

/**
 * Performs validation of annotations properties' values using a specific {@link Validator}s
 */
public class Validation {
    private static final String LOG_PLAIN_VALUE_PATTERN = "Property %s is set to a wrong value: '%s' provided, %s";
    private static final String LOG_ANNOTATION_PATTERN = "Annotation @%s(%s) is invalid: %s";
    private static final String ARGUMENT_LIST_OPENER = "(";
    private static final String ARGUMENT_LIST_FINISHER = ")";

    private static final Validator NO_RESTRICTION = new PermissiveValidator();

    private final Validator testRoutine;
    private String propertyPath;

    /**
     * Initializes a class instance
     * @param testRoutine {@link Validator} instance to be used for testing
     */
    private Validation(Validator testRoutine) {
        this.testRoutine = testRoutine;
    }

    /**
     * Gets whether the specified value is validated
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
     * Retrieves an appropriate {@code Validation} for the specified annotation property
     * @param property {@link Property} instance representing the annotation property
     * @return {@code Validation} instance
     */
    public static Validation forProperty(Property property) {
        String restriction = null;
        if (property.getAnnotation(ValueRestriction.class) != null) {
            restriction = property.getAnnotation(ValueRestriction.class).value();
        }
        Validation checker = new Validation(getTestRoutine(restriction));
        checker.propertyPath = property.getPath();
        return checker;
    }

    /**
     * Retrieves an appropriate {@code Validation} for the specified annotation type
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
     * Returns the default (fully permissive) {@code Validation}
     * @return Default {@code Validation}
     */
    public static Validation defaultChecker() {
        return new Validation(NO_RESTRICTION);
    }

    /**
     * Retrieves {@link Validator} instance for specific class name
     * @param name Validator name
     * @return {@code Validator} instance
     */
    private static Validator getTestRoutine(String name) {
        return PluginRuntime
            .context()
            .getReflection()
            .getValidators()
            .stream()
            .filter(validator -> validator.getClass().getName().equals(name))
            .findFirst()
            .orElse(NO_RESTRICTION);
    }

    /**
     * Formats log warnings according to the particular {@link Validator}'s output
     * @param value Value to test
     * @return Message string
     */
    private String getLogMessage(Object value) {
        if (value != null && ClassUtils.isAssignable(value.getClass(), Annotation.class)) {
            String argumentSubstring = StringUtils.substringBetween(value.toString(), ARGUMENT_LIST_OPENER, ARGUMENT_LIST_FINISHER);
            if (argumentSubstring == null) {
                argumentSubstring = value.toString();
            }
            return String.format(LOG_ANNOTATION_PATTERN,
                    ((Annotation) value).annotationType().getSimpleName(),
                    argumentSubstring,
                    testRoutine.getWarningMessage());
        }
        return String.format(LOG_PLAIN_VALUE_PATTERN,
                propertyPath,
                value,
                testRoutine.getWarningMessage());
    }
}
