/*
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.exadel.aem.toolkit.core.maven;

import java.util.List;

import com.exadel.aem.toolkit.api.runtime.ExceptionHandler;
import com.exadel.aem.toolkit.core.exceptions.handlers.PluginExceptionHandlers;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;
import com.exadel.aem.toolkit.core.util.PluginXmlUtility;

/**
 * The implementation of {@link PluginRuntimeContext} for the AEM Authoring Toolkit plugin instance that
 * has been properly initialized
 */
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

