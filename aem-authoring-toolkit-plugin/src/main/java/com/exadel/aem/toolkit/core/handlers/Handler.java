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

package com.exadel.aem.toolkit.core.handlers;

import com.exadel.aem.toolkit.api.runtime.XmlUtility;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.PluginXmlUtility;

/**
 * Manifests the common interface for AEM Authoring Toolkit plugin {@code widget}s', {@code editConfig}s',
 * {@code container}s' handlers
 */
public interface Handler {
    /**
     * Shortcut method for getting the {@link XmlUtility} descendant associated with the current context
     * @return The {@link PluginXmlUtility} instance
     */
    default PluginXmlUtility getXmlUtil() {
        return PluginRuntime.context().getXmlUtility();
    }
}
