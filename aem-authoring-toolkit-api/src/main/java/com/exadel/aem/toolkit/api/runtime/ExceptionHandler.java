package com.exadel.aem.toolkit.api.runtime;

/**
 * Implements handling exceptions thrown in scope of AEM Authoring Toolkit routines for such purposes as unified
 * logging and desision whether Maven plugin executuion should continue or stop
 */
public interface ExceptionHandler {
    /**
     * Handles an exception
     * @param e {@code Exception} instance
     */
    void handle (Exception e);
    /**
     * Handles an with a supplementary message
     * @param message {@code String} value
     * @param cause {@code Exception} instance
     */
    void handle (String message, Exception cause);
}
