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
package com.exadel.aem.toolkit.core.handlers.widget.common;

import java.util.function.BiConsumer;

import com.exadel.aem.toolkit.api.handlers.SourceFacade;
import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Attribute;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;

/**
 * Handler for storing {@link Attribute} properties to a Granite UI widget XML node
 */
public class AttributesHandler implements BiConsumer<SourceFacade, Element> {
    /**
     * Processes the user-defined data and writes it to XML entity
     * @param sourceFacade Current {@code SourceFacade} instance
     * @param element XML element
     */
    @Override
    @SuppressWarnings({"deprecation", "squid:S1874"})
    // "clas" attribute processing remains for compatibility reasons until v.2.0.0
    public void accept(SourceFacade sourceFacade, Element element) {
        Attribute attribute = sourceFacade.adaptTo(Attribute.class);
        if (attribute == null) {
            return;
        }
        PluginRuntime.context().getXmlUtility().appendDataAttributes(element, attribute.data());
    }
}
