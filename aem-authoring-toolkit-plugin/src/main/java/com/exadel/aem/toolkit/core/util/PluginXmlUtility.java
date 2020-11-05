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
package com.exadel.aem.toolkit.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Data;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.google.common.collect.ImmutableMap;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.widgets.DataSource;
import com.exadel.aem.toolkit.api.annotations.widgets.property.Property;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utility methods to process, verify and store AEM TouchUI dialog-related data to XML markup
 */
public class PluginXmlUtility {
    public static final Map<String, String> XML_NAMESPACES = ImmutableMap.of(
            "xmlns:jcr", "http://www.jcp.org/jcr/1.0",
            "xmlns:nt", "http://www.jcp.org/jcr/nt/1.0",
            "xmlns:sling", "http://sling.apache.org/jcr/sling/1.0",
            "xmlns:cq", "http://www.day.com/jcr/cq/1.0",
            "xmlns:granite", "http://www.adobe.com/jcr/granite/1.0"
    );

    public static final String ATTRIBUTE_LIST_TEMPLATE = "[%s]";
    public static final String ATTRIBUTE_LIST_SPLIT_PATTERN = "\\s*,\\s*";
    public static final String ATTRIBUTE_LIST_SURROUND = "[]";
    public static final Pattern ATTRIBUTE_LIST_PATTERN = Pattern.compile("^\\[.+]$");

    private static final XmlTransferPolicy DEFAULT_XML_TRANSFER_POLICY = XmlTransferPolicy.SKIP;

    /**
     * Default routine to manage merging two values of an XML attribute by suppressing existing value with a non-empty new one
     */
    public static final BinaryOperator<String> DEFAULT_ATTRIBUTE_MERGER = (first, second) -> StringUtils.isNotBlank(second) ? second : first;

    public static void appendDataAttributes(Target target, Data[] data) {
        if (ArrayUtils.isEmpty(data)) {
            return;
        }
        appendDataAttributes(target, Arrays.stream(data).collect(Collectors.toMap(Data::name, Data::value)));
    }

    public static void appendDataAttributes(Target target, Map<String, String> data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        Target graniteDataNode = target.child(DialogConstants.NN_DATA);
        data.entrySet().stream()
                .filter(entry -> StringUtils.isNotBlank(entry.getKey()))
                .forEach(entry -> graniteDataNode.attribute(entry.getKey(), entry.getValue()));
    }

    /**
     * Appends {@link DataSource} value and, for compatibility reasons, deprecated {@code acsListPath}
     * and {@code acsListResourceType} values to an {@code Element} node
     * @param target TargetFacade to store data in
     * @param dataSource Provided values as a {@code DataSource} annotation
     * @param acsListPath Path to ACS Commons List in JCR repository
     * @param acsListResourceType Use this to set {@code sling:resourceType} of data source, other than standard
     * @return Appended {@code datasource} node
     */
    public static Target appendDataSource(Target target, DataSource dataSource, String acsListPath, String acsListResourceType) {
        Map<String, String> arbitraryProperties = Arrays.stream(dataSource.properties())
                .collect(Collectors.toMap(Property::name, Property::value));
        Target dataSourceElement = appendDataSource(target, dataSource.path(), dataSource.resourceType(), arbitraryProperties);
        if (dataSourceElement == null) {
            dataSourceElement = appendAcsCommonsList(target, acsListPath, acsListResourceType);
        }
        return dataSourceElement;
    }

    /**
     * Appends to the current {@code Element} node and returns a child {@code datasource} node
     * @param target Element to store data in
     * @param path Path to targetFacade
     * @param resourceType Use this to set {@code sling:resourceType} of data source
     * @return Appended {@code datasource} node, or null if the provided {@code resourceType} is invalid
     */
    private static Target appendDataSource(Target target, String path, String resourceType, Map<String, String> properties) {
        if (StringUtils.isBlank(resourceType)) {
            return null;
        }
        properties.put(DialogConstants.PN_PATH, path);

        return target.child(DialogConstants.NN_DATASOURCE)
                .attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, resourceType)
                .attributes(properties);
    }

    /**
     * Appends to the current {@code Element} node and returns a child {@code datasource} node bearing link to an ACS Commons list
     * @param target {@code TargetFacade} to store data in
     * @param path Path to ACS Commons List in JCR repository
     * @param resourceType Use this to set {@code sling:resourceType} of data source, other than standard
     * @return Appended {@code datasource} node, or null if the provided {@code path} is invalid
     */
    private static Target appendAcsCommonsList(Target target, String path, String resourceType) {
        if (StringUtils.isBlank(path)) {
            return null;
        }
        return target.child(DialogConstants.NN_DATASOURCE)
                .attribute(DialogConstants.PN_PATH, path)
                .attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, resourceType.isEmpty() ? ResourceTypes.ACS_LIST : resourceType);
    }

    /**
     * Retrieves list of {@link Element} nodes from the current document selected by {@link XPath}
     * @param xPath String xPath representation
     * @return List of {@code Element}s, or an empty list
     */
    public static List<Element> getElementNodes(String xPath, Document document) {
        XPath xPathInstance = XPathFactory.newInstance().newXPath();
        List<Element> result = new ArrayList<>();
        try {
            NodeList nodes = (NodeList) xPathInstance.evaluate(xPath, document, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node instanceof Document) {
                    result.add(((Document) node).getDocumentElement());
                } else if (node instanceof Element) {
                    result.add((Element) node);
                }
            }
            if (result.isEmpty()) {
                throw new XPathExpressionException("Resolves to null or node of non-element type");
            }
        } catch (XPathExpressionException e) {
            PluginRuntime.context().getExceptionHandler().handle(String.format("Wrong XPath argument '%s'", xPath), e);
        }
        return result;
    }

    public static Document buildXml(Target target, Document document) {
        Element root = populateDocument(target, document);
        XML_NAMESPACES.forEach((key, value) -> {
                    if (StringUtils.isNoneBlank(key, value)) root.setAttribute(key, value);
                });
        document.appendChild(root);
        return document;
    }

    private static Element populateDocument(Target target, Document document) {
        String name = NamingUtil.getValidName(target.getName());
        Element tmp = document.createElement(name);
        mapProperties(tmp, target);
        target.listChildren().forEach(child -> tmp.appendChild(populateDocument(child, document)));
        return tmp;
    }

    private static void mapProperties(Element element, Target target) {
        for (Map.Entry<String, String> entry : target.valueMap().entrySet()) {
            element.setAttribute(entry.getKey(), entry.getValue());
        }
    }

    private PluginXmlUtility() {

    }
}
