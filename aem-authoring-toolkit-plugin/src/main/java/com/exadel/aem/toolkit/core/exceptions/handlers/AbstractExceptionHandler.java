package com.exadel.aem.toolkit.core.exceptions.handlers;

import com.exadel.aem.toolkit.api.runtime.ExceptionHandler;

abstract class AbstractExceptionHandler implements ExceptionHandler {
    @Override
    public void handle(Exception e) {
        handle(e.getMessage(), e);
    }
}
