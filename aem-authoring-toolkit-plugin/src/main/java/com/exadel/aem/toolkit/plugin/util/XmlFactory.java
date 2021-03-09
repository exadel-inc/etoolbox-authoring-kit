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

package com.exadel.aem.toolkit.plugin.util;

import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import com.google.common.collect.ImmutableMap;

import com.exadel.aem.toolkit.plugin.runtime.PluginXmlUtility;

/**
 * Contains utility methods for creating and transforming XML entities
 */
public class XmlFactory {
    /**
     * Security features as per XML External entity protection cheat sheet
     * @see <a href="https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html">here</a>
     */
    private static final Map<String, Boolean> DOCUMENT_BUILDER_FACTORY_SECURITY_FEATURES = ImmutableMap.of(
        "http://apache.org/xml/features/disallow-doctype-decl", true,
        "http://xml.org/sax/features/external-general-entities", false,
        "http://xml.org/sax/features/external-parameter-entities", false,
        "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

    /**
     * XML namespaces typically present in an AEM component markup
     */
    public static final Map<String, String> XML_NAMESPACES = ImmutableMap.of(
        "jcr", "http://www.jcp.org/jcr/1.0",
        "nt", "http://www.jcp.org/jcr/nt/1.0",
        "sling", "http://sling.apache.org/jcr/sling/1.0",
        "cq", "http://www.day.com/jcr/cq/1.0",
        "granite", "http://www.adobe.com/jcr/granite/1.0"
    );
    public static final String XML_NAMESPACE_PREFIX = "xmlns:";


    /**
     * Default private (hiding) constructor
     */
    private XmlFactory() {
    }

    /**
     * Creates a new {@link Document} instance compliant with XML entity protection policies
     * @return Empty XML {@code Document}
     * @throws ParserConfigurationException if one or more security features cannot be assigned to the newly created document
     */
    public static Document newDocument() throws ParserConfigurationException {
        return createDocumentBuilder().newDocument();
    }

    /**
     * Creates a {@link Transformer} instance compliant with XML security attributes
     * @return {@code Transformer} object
     * @throws TransformerConfigurationException in case security attributes cannot be set
     */
    public static Transformer newDocumentTransformer() throws TransformerConfigurationException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, StringUtils.EMPTY);
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, StringUtils.EMPTY);
        return transformerFactory.newTransformer();
    }

    /**
     * Creates a new {@link PluginXmlUtility} instance wrapped around a new {@code Document} for processing legacy
     * handlers in an isolated document context
     * @return {@code PluginXmlUtility} object
     * @throws ParserConfigurationException if one or more security features cannot be assigned to the newly created document
     */
    public static PluginXmlUtility newXmlUtility() throws ParserConfigurationException {
        Document document = newDocument();
        return new PluginXmlUtility(document);
    }

    /**
     * Called by {@link XmlFactory#newDocument()} to create an instance of XML {@code DocumentBuilder}
     * with specific XML security features set
     * @return {@link DocumentBuilder} instance
     * @throws ParserConfigurationException if one or more security features cannot be assigned to the newly created document
     */
    private static DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        for(Map.Entry<String, Boolean> feature : DOCUMENT_BUILDER_FACTORY_SECURITY_FEATURES.entrySet()) {
            dbf.setFeature(feature.getKey(), feature.getValue());
        }
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);
        return dbf.newDocumentBuilder();
    }
}
