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
package com.exadel.aem.toolkit.plugin.maven;

import com.exadel.aem.toolkit.api.runtime.ExceptionHandler;
import com.exadel.aem.toolkit.plugin.exceptions.PluginException;
import com.exadel.aem.toolkit.plugin.runtime.ReflectionContextHelper;
import com.exadel.aem.toolkit.plugin.runtime.XmlContextHelper;

/**
 * The fallback implementation of {@link PluginRuntimeContext} for the ToolKit Maven plugin instance that
 * has not been properly and completely initialized
 */
class EmptyRuntimeContext implements PluginRuntimeContext {
    private static final String NOT_INITIALIZED_EXCEPTION_MESSAGE = "Plugin was not properly initialized";

    /**
     * Throws a {@code PluginException} upon call since the runtime has not been initialized
     */
    @Override
    public ReflectionContextHelper getReflection() {
        throw new PluginException(NOT_INITIALIZED_EXCEPTION_MESSAGE);
    }

    /**
     * Throws a {@code PluginException} upon call since the runtime has not been initialized
     */
    @Override
    public ExceptionHandler getExceptionHandler() {
        throw new PluginException(NOT_INITIALIZED_EXCEPTION_MESSAGE);
    }

    /**
     * Throws a {@code PluginException} upon call since the runtime has not been initialized
     */
    @Override
    public XmlContextHelper getXmlUtility() {
        throw new PluginException(NOT_INITIALIZED_EXCEPTION_MESSAGE);
    }

    /**
     * Throws a {@code PluginException} upon call since the runtime has not been initialized
     */
    @Override
    public XmlContextHelper newXmlUtility() {
        throw new PluginException(NOT_INITIALIZED_EXCEPTION_MESSAGE);
    }
}
