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
import java.util.Optional;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.handlers.Adaptable;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.util.PluginNamingUtility;
import com.exadel.aem.toolkit.plugin.util.XmlFactory;

@Adaptable(Target.class)
public class DomAdapter {

    private final Target target;
    private Document document;
    private Element documentElement;

    public DomAdapter(Target target) {
        this.target = target;
    }

    public DomAdapter useExisting(Document document) {
        // If no valid document passed, trigger creation of a new Document with its root element populated with
        // Target data, by calling DomAdapter#getDocument(), then return
        if (document == null) {
            getDocument();
            return this;
        }
        // But if a document passed, store it and leave it as is, but create an "overriding" documentElement that
        // will be returned upon DomAdapter#getDocumentElement() call
        this.document = document;
        this.documentElement = createDocumentElement(target);
        return this;
    }

    public Document getDocument() {
        if (document != null) {
            return document;
        }
        try {
            document = XmlFactory.newDocument();
            documentElement = createDocumentElement(target);
            XmlFactory.XML_NAMESPACES.forEach((key, value) -> documentElement.setAttribute(XmlFactory.XML_NAMESPACE_PREFIX + key, value));
            document.appendChild(documentElement);

        } catch (ParserConfigurationException e) {
            PluginRuntime.context().getExceptionHandler().handle(e);
        }
        return document;
    }

    public Element getDocumentElement() {
        if (documentElement != null) {
            return documentElement;
        }
        return Optional.ofNullable(document).map(Document::getDocumentElement).orElse(null);
    }

    private Element createDocumentElement(Target target) {
        String name = PluginNamingUtility.getValidName(target.getName());
        Element element = document.createElement(name);
        for (Map.Entry<String, String> entry : target.getAttributes().entrySet()) {
            element.setAttribute(entry.getKey(), entry.getValue());
        }
        target.getChildren().forEach(child -> element.appendChild(createDocumentElement(child)));
        return element;
    }
}
