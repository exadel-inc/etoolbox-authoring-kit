package com.exadel.aem.toolkit.core.exceptions;

/**
 * Represents the plugin-specific exception due to one of exceptions produces by {@code java.lang.reflect} routines
 */
public class ReflectionException extends RuntimeException {
    public ReflectionException(String message) {
        super(message);
    }
    public ReflectionException(String message, Exception inner) {
        super(message, inner);
    }
    public ReflectionException(Class<?> clazz, String fieldName) {
        super(String.format("Field '%s' not present in %s", fieldName, clazz));
    }

}
