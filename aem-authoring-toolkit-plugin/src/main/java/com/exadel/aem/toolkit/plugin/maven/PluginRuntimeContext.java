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
import com.exadel.aem.toolkit.plugin.runtime.PluginXmlUtility;
import com.exadel.aem.toolkit.plugin.runtime.ReflectionRuntime;

/**
 * An abstraction of AEM Authoring Toolkit's Maven plugin runtime context for internal use within. Additional to ts ancestor,
 * provides access to {@link ReflectionRuntime}, and the extended {@link PluginXmlUtility}
 */
public interface PluginRuntimeContext extends RuntimeContext {

    /**
     * Provides the reference to the active {@link ReflectionRuntime} instance
     * @return {@code PluginReflectionUtility} initialized for this context
     */
    ReflectionRuntime getReflection();

    @Override
    PluginXmlUtility getXmlUtility();
}
