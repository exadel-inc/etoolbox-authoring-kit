package com.exadel.aem.toolkit.core.maven;

import com.exadel.aem.toolkit.api.runtime.ExceptionHandler;
import com.exadel.aem.toolkit.core.exceptions.PluginException;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;
import com.exadel.aem.toolkit.core.util.PluginXmlUtility;

class EmptyRuntimeContext implements PluginRuntimeContext {
    private static final String NOT_INITIALIZED_EXCEPTION_MESSAGE = "Plugin was not properly initialized";

    @Override
    public PluginReflectionUtility getReflectionUtility() {
        throw new PluginException(NOT_INITIALIZED_EXCEPTION_MESSAGE);
    }

    @Override
    public ExceptionHandler getExceptionHandler() {
        throw new PluginException(NOT_INITIALIZED_EXCEPTION_MESSAGE);
    }

    @Override
    public PluginXmlUtility getXmlUtility() {
        throw new PluginException(NOT_INITIALIZED_EXCEPTION_MESSAGE);
    }
}
