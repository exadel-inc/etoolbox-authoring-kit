package com.exadel.aem.toolkit.core.exceptions.handlers;

import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PermissiveExceptionHandler extends AbstractExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger("AEM Authoring Toolkit");

    /**
     * Logs the handled exception with a brief stacktrace. Checked exceptions are logged with error messages, and unchecked
     * exceptions are logged with warnings
     * @param message Attached exception message
     * @param cause Base exception
     */
    @Override
    public void handle(String message, Exception cause) {
        if (ClassUtils.isAssignable(cause.getClass(), RuntimeException.class)) {
            LOG.warn(message, cause);
        } else {
            LOG.error(message, cause);
        }
    }

}
