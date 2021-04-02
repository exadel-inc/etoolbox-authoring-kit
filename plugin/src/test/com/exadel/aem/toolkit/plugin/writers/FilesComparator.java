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
package com.exadel.aem.toolkit.plugin.writers;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

import com.exadel.aem.toolkit.plugin.utils.XmlFactory;

class FilesComparator {
    private static final Logger LOG = LoggerFactory.getLogger(FilesComparator.class);
    private static final int LOG_INDENT = 7;

    private FilesComparator() {
    }

    static boolean compareXml(String actual, String expected, String resourcePath)
            throws IOException, SAXException, ParserConfigurationException, XPathExpressionException, TransformerException {
        Diff diff = DiffBuilder.compare(Input.fromString(expected))
                .withTest(Input.fromString(actual)).normalizeWhitespace().build();
        if (!diff.hasDifferences()) {
            return true;
        }
        Document actualDocument = getXmlDocument(actual);
        Document expectedDocument = getXmlDocument(expected);
        XPath xPathInstance = XPathFactory.newInstance().newXPath();

        final StringBuilder differences = new StringBuilder("difference(s) detected at ")
                .append(resourcePath).append(System.lineSeparator());
        final String indent = StringUtils.repeat(' ', LOG_INDENT);
        int differencesCount = 0;
        for (Difference difference : diff.getDifferences()) {
            differences.append(System.lineSeparator())
                    .append(indent)
                    .append(difference.getComparison().toString())
                    .append(System.lineSeparator());
            if (difference.getComparison().getControlDetails().getXPath() != null) {
                differences.append(indent)
                        .append("Needed ").append(getXmlString(expectedDocument, xPathInstance, difference.getComparison().getControlDetails().getXPath()))
                        .append(System.lineSeparator());
            }
            if (difference.getComparison().getTestDetails().getXPath() != null) {
                differences.append(indent)
                        .append("Actual ").append(getXmlString(actualDocument, xPathInstance, difference.getComparison().getTestDetails().getXPath()))
                        .append(System.lineSeparator());
            }
            differencesCount++;
        }
        if (differencesCount > 0) {
            LOG.warn("{} {}", differencesCount, differences);
        }
        return differencesCount == 0;
    }

    private static Document getXmlDocument(String xmlString) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xmlString)));
    }

    private static String getXmlString(Document document, XPath instance, String xPath) throws XPathExpressionException, TransformerException {
        Element element = (Element)instance.evaluate(StringUtils.substringBeforeLast(xPath, "/@"), document, XPathConstants.NODE);
        return getXmlString(element);
    }

    private static String getXmlString(Element element) throws TransformerException {
        XmlFactory.XML_NAMESPACES.forEach((key, value) -> element.setAttribute(XmlFactory.XML_NAMESPACE_PREFIX + key, value));
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        Transformer transformer = transformerFactory.newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(element), new StreamResult(writer));
        String output = writer.toString();
        return output.substring(output.indexOf("?>") + 2)
                .replaceAll("xmlns:\\w+=\".+?\"", "")
                .replaceAll("(?s)>\\s+<", "><")
                .replaceAll("(?s)\\s+", " ");
    }
}
