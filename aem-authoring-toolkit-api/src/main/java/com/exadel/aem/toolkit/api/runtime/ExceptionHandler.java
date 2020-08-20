package com.exadel.aem.toolkit.api.runtime;

/**
 * Implements handling exceptions thrown in scope of AEM Authoring Toolkit routines. This is done for such purposes
 * as unified logging, or making decision whether Maven plugin execution should continue or stop
 */
public interface ExceptionHandler {

    /**
     * Handles an exception
     * @param e {@code Exception} instance
     */
    default void handle (Exception e) {
        handle(e.getMessage(), e);
    }

    /**
     * Handles an exception with a supplementary message
     * @param message {@code String} value
     * @param cause {@code Exception} instance
     */
    void handle (String message, Exception cause);

    /**
     * Gets whether an exception of the specified class would cause the AEM Authoring Toolkit's Maven plugin to terminate.
     * Should the user choose to skip this particular exception or all the exceptions in the plugins configuration,
     * this function must return false, otherwise it returns true
     * @param exceptionType Class of {@link Exception} to test on
     * @return True or false
     */
    boolean shouldTerminateOn(Class<? extends Exception> exceptionType);
}
