package com.exadel.aem.toolkit.api.runtime;

import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.handlers.Source;

/**
 * An abstraction of AEM Authoring Toolkit's Maven plugin runtime context. Provides access to {@link XmlUtility}, and
 * {@link ExceptionHandler}
 *
 * @deprecated Since AEM Authoring Toolkit v. 2.0.0 users are encouraged to use new custom handlers API that is based
 * on {@link Source} and {@link Target} objects handling. Legacy API will be revoked in the versions to come
 */
@Deprecated
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
