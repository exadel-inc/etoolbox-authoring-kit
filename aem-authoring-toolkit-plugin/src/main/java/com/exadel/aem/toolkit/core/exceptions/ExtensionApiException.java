package com.exadel.aem.toolkit.core.exceptions;

public class ExtensionApiException extends RuntimeException {
    public ExtensionApiException(Class<?> customType, Exception cause) {
        super("Could not invoke " + customType.getName(), cause);
    }
}
