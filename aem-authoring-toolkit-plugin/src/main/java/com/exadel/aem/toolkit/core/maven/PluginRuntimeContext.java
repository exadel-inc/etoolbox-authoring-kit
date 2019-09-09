package com.exadel.aem.toolkit.core.maven;

import com.exadel.aem.toolkit.api.runtime.RuntimeContext;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;
import com.exadel.aem.toolkit.core.util.PluginXmlUtility;

public interface PluginRuntimeContext extends RuntimeContext {
    PluginReflectionUtility getReflectionUtility();
    PluginXmlUtility getXmlUtility();
}
