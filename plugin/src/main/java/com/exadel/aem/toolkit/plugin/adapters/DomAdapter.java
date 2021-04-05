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
package com.exadel.aem.toolkit.plugin.adapters;

import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.handlers.Adapts;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.utils.NamingUtil;
import com.exadel.aem.toolkit.plugin.utils.XmlFactory;

/**
 * Implements {@link Adapts} for rendering a {@link Target} instance into a DOM {@code Document}
 */
@Adapts(Target.class)
public class DomAdapter {

    private final Target target;

    /**
     * Constructor per {@link Adapts} contract
     * @param target {@code Target} object that will be used as a source of data for rendering
     */
    public DomAdapter(Target target) {
        this.target = target;
    }

    /**
     * Retrieves a {@code Document} populated with data for the {@code Target} passed upon class initialization
     * @param sourceDocument Document used as a factory for new elements
     * @return {@code Document} instance, or null in case an exception occurred when creating the return value
     */
    public Document composeDocument(Document sourceDocument) {
        if (target == null) {
            return sourceDocument;
        }
        if (sourceDocument == null) {
            try {
                sourceDocument = XmlFactory.newDocument();
            } catch (ParserConfigurationException e) {
                PluginRuntime.context().getExceptionHandler().handle(e);
                return null;
            }
        }
        sourceDocument.appendChild(createElement(sourceDocument, target, true));
        return sourceDocument;
    }

    /**
     * Retrieves an {@code Element} populated with data for the {@code Target} passed upon class initialization.
     * The element is created via the provided {@code Document} instance but stays "unattached" to form an isolated
     * structure that may be used e.g. in legacy handlers
     * @param sourceDocument Document used as a factory for new elements
     * @return {@code Document} instance, or null in case an exception occurred when creating the return value
     */
    public Element composeElement(Document sourceDocument) {
        if (target == null) {
            return sourceDocument != null ? sourceDocument.getDocumentElement() : null;
        }
        if (sourceDocument == null) {
            try {
                sourceDocument = XmlFactory.newDocument();
            } catch (ParserConfigurationException e) {
                PluginRuntime.context().getExceptionHandler().handle(e);
                return null;
            }
        }
        return createElement(sourceDocument, target, true);
    }

    /**
     * Implements creating a DOM element with the {@code Document} source and {@code Target} provided
     * @param sourceDocument Document used as a factory for new elements
     * @param target         {@code Target} instance holding the data for rendering
     * @param isRoot         True to create a document element; false to create an ordinary nested element
     * @return {@code Element} instance
     */
    private static Element createElement(Document sourceDocument, Target target, boolean isRoot) {
        String name = NamingUtil.getValidNodeName(target.getName());
        Element element = sourceDocument.createElement(name);
        for (Map.Entry<String, String> entry : target.getAttributes().entrySet()) {
            element.setAttribute(entry.getKey(), entry.getValue());
        }
        target.getChildren().forEach(child -> element.appendChild(createElement(sourceDocument, child, false)));
        if (isRoot) {
            XmlFactory.XML_NAMESPACES.forEach((key, value) ->
                element.setAttribute(XmlFactory.XML_NAMESPACE_PREFIX + key, value));
        }
        return element;
    }
}
