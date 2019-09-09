package com.exadel.aem.toolkit.core.exceptions.handlers;

import java.util.List;

import com.exadel.aem.toolkit.core.exceptions.PluginException;

class SelectiveExceptionHandler extends PermissiveExceptionHandler {
    private List<String> criticalExceptions;

    SelectiveExceptionHandler(List<String> criticalExceptions) {
        this.criticalExceptions = criticalExceptions;
    }

    /**
     * Handles exception with additional checking whether to terminate plugin execution
     * (by re-throwing more generic {@code PluginException}) or not
     * @param message Attached exception message
     * @param cause   Base exception
     */
    @Override
    public void handle(String message, Exception cause) {
        if (criticalExceptions.stream().anyMatch(exName -> cause.getClass().getName().equalsIgnoreCase(exName))) {
            throw new PluginException(cause);
        }
        super.handle(message, cause);
    }
}
