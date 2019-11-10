package com.exadel.aem.toolkit.core.maven;

import java.util.List;

import com.exadel.aem.toolkit.api.runtime.ExceptionHandler;
import com.exadel.aem.toolkit.core.exceptions.handlers.PluginExceptionHandlers;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;
import com.exadel.aem.toolkit.core.util.PluginXmlUtility;

class LoadedRuntimeContext implements PluginRuntimeContext {
    private final PluginReflectionUtility pluginReflections;
    private final ExceptionHandler exceptionHandler;
    private final PluginXmlUtility xmlUtility;

    LoadedRuntimeContext(List<String> classPathElements, String packageBase, String criticalExceptions) {
        this.pluginReflections = PluginReflectionUtility.fromCodeScope(classPathElements, packageBase);
        this.exceptionHandler = PluginExceptionHandlers.getHandler(criticalExceptions);
        this.xmlUtility = new PluginXmlUtility();
    }

    @Override
    public PluginReflectionUtility getReflectionUtility() {
        return pluginReflections;
    }

    @Override
    public ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    @Override
    public PluginXmlUtility getXmlUtility() {
        return xmlUtility;
    }
}

