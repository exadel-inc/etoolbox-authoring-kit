package com.exadel.aem.toolkit.api.runtime;

/**
 * An abstraction of AEM Authoring Toolkit's Maven plugin runtime context. Provides access to {@link XmlUtility}, and
 * {@link ExceptionHandler}
 */
public interface RuntimeContext {
    /**
     * Gets the active {@link ExceptionHandler} instance
     */
    ExceptionHandler getExceptionHandler();
    /**
     * Gets the active {@link XmlUtility} instance
     */
    XmlUtility getXmlUtility();
}