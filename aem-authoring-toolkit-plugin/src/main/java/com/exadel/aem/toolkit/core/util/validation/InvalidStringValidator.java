package com.exadel.aem.toolkit.core.util.validation;

import com.exadel.aem.toolkit.api.annotations.meta.Validator;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.regex.Pattern;

public class InvalidStringValidator implements Validator {

    private static final String MSG_VALID_CHARACTERS = "valid characters are latin symbols and numbers";

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
     * Tests single String-typed propertiy value for being not blank
     *
     * @param method     {@code Method} instance representing annotation property
     * @param annotation Target annotation
     * @return True or false
     */
    private static boolean checkMethodValue(Method method, Annotation annotation) {
        try {
            return Pattern.matches("[a-zA-Z0-9]+", method.invoke(annotation).toString());
        } catch (IllegalAccessException | InvocationTargetException e) {
            return true;
        }
    }

    @Override
    public boolean isApplicableTo(Object obj) {
        return obj instanceof Annotation;
    }

    @Override
    public String getWarningMessage() {
        return MSG_VALID_CHARACTERS;
    }
}
