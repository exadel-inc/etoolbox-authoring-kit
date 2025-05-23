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
package com.exadel.aem.toolkit.plugin.runtime;

import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.ImmutableMap;

/**
 * Contains utility methods for creating and transforming XML entities
 */
class XmlFactory {

    /**
     * Security features as per XML External entity protection cheat sheet
     * @see <a href="https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html">here</a>
     */
    private static final Map<String, Boolean> DOCUMENT_BUILDER_FACTORY_SECURITY_FEATURES = ImmutableMap.of(
        XMLConstants.FEATURE_SECURE_PROCESSING, true,
        "http://apache.org/xml/features/disallow-doctype-decl", true,
        "http://xml.org/sax/features/external-general-entities", false,
        "http://xml.org/sax/features/external-parameter-entities", false,
        "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

    /**
     * Default (instantiation-restricting) constructor
     */
    private XmlFactory() {
    }

    /**
     * Creates a {@link DocumentBuilder} with specific XML security features set
     * @return {@code DocumentBuilder} object
     * @throws ParserConfigurationException in case security attributes cannot be set
     */
    public static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, StringUtils.EMPTY);
        documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, StringUtils.EMPTY);
        for (Map.Entry<String, Boolean> feature : DOCUMENT_BUILDER_FACTORY_SECURITY_FEATURES.entrySet()) {
            documentBuilderFactory.setFeature(feature.getKey(), feature.getValue());
        }
        documentBuilderFactory.setXIncludeAware(false);
        documentBuilderFactory.setExpandEntityReferences(false);
        return documentBuilderFactory.newDocumentBuilder();
    }

    /**
     * Creates a {@link Transformer} instance compliant with XML security policies
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
}
