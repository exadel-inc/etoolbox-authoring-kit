package com.exadel.aem.toolkit.core.exceptions;

/**
 * Represents the generic exception rethrown by {@link com.exadel.aem.toolkit.api.runtime.ExceptionHandler} to terminate
 * maven build workflow as required
 */
public class PluginException extends RuntimeException {
    public PluginException(String message) {
        super(message);
    }
    public PluginException(Exception cause) {
        super(cause);
    }
    public PluginException(String message, Exception cause) {
        super(message, cause);
    }
}
