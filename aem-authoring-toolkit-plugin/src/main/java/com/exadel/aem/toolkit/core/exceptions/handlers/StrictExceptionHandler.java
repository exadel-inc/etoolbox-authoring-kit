package com.exadel.aem.toolkit.core.exceptions.handlers;

import com.exadel.aem.toolkit.core.exceptions.PluginException;

class StrictExceptionHandler extends AbstractExceptionHandler {
    @Override
    public void handle(String message, Exception cause) {
        throw new PluginException(message, cause);
    }
}
