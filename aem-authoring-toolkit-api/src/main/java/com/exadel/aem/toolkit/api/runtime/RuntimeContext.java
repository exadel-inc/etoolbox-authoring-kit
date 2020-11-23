package com.exadel.aem.toolkit.api.runtime;

/**
 * An abstraction of AEM Authoring Toolkit's Maven plugin runtime context. Provides access to {@link XmlUtility}, and
 * {@link ExceptionHandler}
 */
public interface RuntimeContext {
    /**
     * Provides the reference to the active {@link ExceptionHandler} instance
     * @return {@code ExceptionHandler} initialized for this context
     */
    ExceptionHandler getExceptionHandler();
    /**
     * Provides the reference to the active {@link XmlUtility} instance
     * @return {@code XmlUtility} initialized for this context
     */
    XmlUtility getXmlUtility();
}