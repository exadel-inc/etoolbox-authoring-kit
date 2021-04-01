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

import com.exadel.aem.toolkit.api.runtime.RuntimeContext;
import com.exadel.aem.toolkit.plugin.runtime.ReflectionContextHelper;
import com.exadel.aem.toolkit.plugin.runtime.XmlContextHelper;

/**
 * An abstraction of the ToolKit Maven plugin runtime context for internal use. In addition to its ancestor's functionality,
 * provides access to {@link ReflectionContextHelper}, and the extended {@link XmlContextHelper}
 */
@SuppressWarnings("deprecation") // RuntimeContext support is retained for compatibility and will be removed
                                 // in a version after 2.0.2
public interface PluginRuntimeContext extends RuntimeContext {

    /**
     * Provides the reference to the active {@link ReflectionContextHelper} instance
     * @return {@code PluginReflectionUtility} initialized for this context
     */
    ReflectionContextHelper getReflection();

    @Override
    XmlContextHelper getXmlUtility();

    /**
     * Creates and returns an {@link XmlContextHelper} instance wrapped around a new XML document
     * @return {@code XmlRuntime} exposing a new XML document
     */
    XmlContextHelper newXmlUtility();
}
