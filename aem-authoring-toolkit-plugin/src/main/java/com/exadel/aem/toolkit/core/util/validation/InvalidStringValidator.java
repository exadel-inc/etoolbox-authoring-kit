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

import com.exadel.aem.toolkit.api.annotations.meta.Validator;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * {@link Validator} implementation for testing all String-typed annotation properties have only
 * latins symbols, numbers, '.', '-'
 */
public class InvalidStringValidator implements Validator {
    private static final String MSG_VALID_CHARACTERS = "valid characters are: latin symbols, numbers, '.', '-'";

    /**
     * Tests that all String-typed properties of the provided annotation don't have invalid characters
     * @param obj Annotation instance
     * @return True or false
     */
    @Override
    public boolean test(Object obj) {
        if (!isApplicableTo(obj)) {
            return false;
        }
        Annotation annotation = (Annotation) obj;
        return Arrays.stream(annotation.annotationType().getDeclaredMethods())
                .filter(method -> method.getReturnType().equals(String.class))
                .allMatch(method -> checkMethodValue(method, annotation));
    }

    /**
     * Tests single String-typed property value have only latin symbols, numbers, '.'. '-'
     * @param method {@code Method} instance representing annotation property
     * @param annotation Target annotation
     * @return True or false
     */
    private static boolean checkMethodValue(Method method, Annotation annotation) {
        try {
            return Pattern.matches("[-.\\w]+", method.invoke(annotation).toString()) &&
                    !StringUtils.containsOnly(method.invoke(annotation).toString(), "-.");
        } catch (IllegalAccessException | InvocationTargetException e) {
            return true;
        }
    }

    /**
     * Returns whether this object is of {@code Annotation} type
     * @param obj Tested value
     * @return True or false
     */
    @Override
    public boolean isApplicableTo(Object obj) {
        return obj instanceof Annotation;
    }

    @Override
    public String getWarningMessage() {
        return MSG_VALID_CHARACTERS;
    }
}
