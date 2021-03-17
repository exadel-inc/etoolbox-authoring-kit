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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.exadel.aem.toolkit.api.annotations.meta.MapProperties;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.Scope;
import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Data;
import com.exadel.aem.toolkit.api.runtime.XmlUtility;
import com.exadel.aem.toolkit.plugin.exceptions.ReflectionException;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.target.AttributeHelper;
import com.exadel.aem.toolkit.plugin.util.AnnotationUtil;
import com.exadel.aem.toolkit.plugin.util.DialogConstants;
import com.exadel.aem.toolkit.plugin.util.NamingUtil;
import com.exadel.aem.toolkit.plugin.util.StringUtil;
import com.exadel.aem.toolkit.plugin.util.validation.Validation;

/**
 * Utility methods to process, verify and store AEM TouchUI dialog-related data to XML markup
 */
public class XmlRuntime implements XmlUtility {

    /**
     * Default routine to manage merging two values of an XML attribute by suppressing existing value with a non-empty new one
     */
    public static final BinaryOperator<String> DEFAULT_ATTRIBUTE_MERGER = (first, second) -> StringUtils.isNotBlank(second) ? second : first;


    /* ---------------------------------
       Instance members and constructors
       --------------------------------- */

    private final Document document;

    /**
     * Default constructor
     * @param document {@code Document} instance to be used as a node factory within this instance
     */
    public XmlRuntime(Document document) {
        this.document = document;
    }

    /**
     * Retrieves the current {@code Document}
     * @return {@code Document} instance
     */
    public Document getDocument() {
        return document;
    }


    /* ----------------------------
       XmlUtility interface members
       ---------------------------- */

    /*
        Element creation
     */

    @Override
    public Element createNodeElement(String name, String nodeType, Map<String, String> properties, String resourceType) {
        Element element = getDocument().createElement(getValidName(name));
        if (nodeType == null) {
            nodeType = DialogConstants.NT_UNSTRUCTURED;
        }
        element.setAttribute(DialogConstants.PN_PRIMARY_TYPE, nodeType);
        if (resourceType != null) {
            element.setAttribute(DialogConstants.PN_SLING_RESOURCE_TYPE, resourceType);
        }
        if (properties != null) {
            properties.forEach((key, value) -> {
                if (StringUtils.isNoneBlank(key, value)) element.setAttribute(key, value);
            });
        }
        return element;
    }

    @Override
    public Element createNodeElement(String name, String nodeType, Map<String, String> properties) {
        return createNodeElement(name, nodeType, properties, null);
    }

    @Override
    public Element createNodeElement(String name, Map<String, String> properties, String resourceType) {
        return createNodeElement(name, null, properties, resourceType);
    }

    @Override
    public Element createNodeElement(String name, String resourceType) {
        return createNodeElement(name, null, new HashMap<>(), resourceType);
    }

    @Override
    public Element createNodeElement(String name, Map<String, String> properties) {
        return createNodeElement(name, null, properties);
    }

    @Override
    public Element createNodeElement(String name) {
        return createNodeElement(name, null, new HashMap<>());
    }

    @Override
    public Element createNodeElement(String name, Annotation source) {
        return createNodeElement(annotation -> name, source);
    }

    /**
     * Creates named XML {2code Element} node from specified {@code Annotation} using specified name provider
     * @param nameProvider Function that processes {@code Annotation} instance to produce valid node name
     * @param source Annotation to be rendered to XML
     * @return {@code Element} instance
     */
    private Element createNodeElement(Function<Annotation, String> nameProvider, Annotation source) {
        if (!Validation.forType(source.annotationType()).test(source)) {
            return null;
        }
        Element newNode = createNodeElement(nameProvider.apply(source));
        Arrays.stream(source.annotationType().getDeclaredMethods())
                .forEach(method -> AttributeHelper.forXmlTarget().forAnnotationProperty(source, method).setTo(newNode));
        return newNode;
    }


    /*
        Element naming
     */

    @Override
    public String getValidName(String name) {
        return NamingUtil.getValidNodeName(name, DialogConstants.NN_ITEM);
    }

    @Override
    public String getValidSimpleName(String name) {
        return NamingUtil.getValidNodeName(name, DialogConstants.NN_ITEM);
    }

    @Override
    public String getValidFieldName(String name) {
        return NamingUtil.getValidNodeName(name, DialogConstants.NN_FIELD);
    }

    @Override
    public String getUniqueName(String name, String defaultValue, Element context) {
        return NamingUtil.getUniqueName(name, defaultValue, context);
    }


    /*
        Setting arbitrary attributes
     */

    @Override
    public void setAttribute(Element element, String name, Double value) {
        AttributeHelper
            .forXmlTarget()
            .forNamedValue(name, Double.class)
            .setValue(value, element);
    }

    @Override
    public void setAttribute(Element element, String name, Long value) {
        AttributeHelper
            .forXmlTarget()
            .forNamedValue(name, Long.class)
            .setValue(value, element);
    }

    @Override
    public void setAttribute(Element element, String name, Boolean value) {
        AttributeHelper
            .forXmlTarget()
            .forNamedValue(name, Boolean.class)
            .setValue(value, element);
    }

    @Override
    public void setAttribute(Element element, String name, String value) {
        AttributeHelper
            .forXmlTarget()
            .forNamedValue(name, String.class)
            .setValue(value, element);
    }

    @Override
    public void setAttribute(Element element, String name, List<String> values) {
        setAttribute(element, name, values, XmlRuntime::mergeStringAttributes);
    }

    @Override
    public void setAttribute(Element element, String name, List<String> values, BinaryOperator<String> attributeMerger) {
        AttributeHelper
            .forXmlTarget()
            .forNamedValue(name, String.class)
            .withMerger(attributeMerger)
            .setValue(values, element);
    }

    @Override
    public void setAttribute(Element element, String name, Annotation source) {
        setAttribute(element, name, source, DEFAULT_ATTRIBUTE_MERGER);
    }

    @Override
    public void setAttribute(Element element, String name, Annotation source, BinaryOperator<String> attributeMerger) {
        setAttribute(() -> element, name, source, attributeMerger);
    }

    /**
     * Stores property value of a specific {@code Annotation} as an XML attribute
     * @param elementSupplier Routine that generates and/or returns an {@code Element} node
     * @param name Attribute name, same as annotation property name
     * @param source Annotation to look fo a value in
     */
    public void setAttribute(Supplier<Element> elementSupplier, String name, Annotation source) {
        setAttribute(elementSupplier, name, source, DEFAULT_ATTRIBUTE_MERGER);
    }

    /**
     * Stores property value of a specific {@code Annotation} as an XML attribute
     * @param elementSupplier Routine that generates and/or returns an {@code Element} node
     * @param name Attribute name, same as annotation property name
     * @param source Annotation to look for a value in
     * @param attributeMerger Function that manages an existing attribute value and a new one
     *                        in case when a new value is set to an existing {@code Element}
     */
    private static void setAttribute(Supplier<Element> elementSupplier,
                                     String name,
                                     Annotation source,
                                     BinaryOperator<String> attributeMerger) {
        try {
            Method sourceMethod = source.annotationType().getDeclaredMethod(name);
            if (!AnnotationUtil.propertyIsNotDefault(source, sourceMethod)) {
                return;
            }
            PropertyRendering propertyRendering = sourceMethod.getAnnotation(PropertyRendering.class);
            String effectiveName = name;
            if (propertyRendering != null) {
                effectiveName = StringUtils.defaultIfBlank(propertyRendering.name(), name);
            }
            AttributeHelper
                .forXmlTarget()
                .forAnnotationProperty(source, sourceMethod)
                .withName(effectiveName)
                .withMerger(attributeMerger)
                .setTo(elementSupplier.get());
        } catch (NoSuchMethodException e) {
            PluginRuntime.context().getExceptionHandler().handle(new ReflectionException(source.getClass(), name));
        }
    }


    /*
        Mapping annotation properties
     */

    @Override
    public void mapProperties(Element element, Annotation annotation) {
        mapProperties(element, annotation, Collections.emptyList());
    }

    @Override
    public void mapProperties(Element element, Annotation annotation, Scope scope) {
        List<String> skippedFields = Arrays.stream(annotation.annotationType().getDeclaredMethods())
                .filter(m -> !fitsInScope(m, scope))
                .map(Method::getName)
                .collect(Collectors.toList());
        mapProperties(element, annotation, skippedFields);
    }

    @Override
    public void mapProperties(Element element, Annotation annotation, List<String> skipped) {
        MapProperties mapProperties = annotation.annotationType().getDeclaredAnnotation(MapProperties.class);
        if (mapProperties == null) {
            return;
        }
        String prefix = annotation.annotationType().getAnnotation(MapProperties.class).prefix();
        String nodePrefix = prefix.contains(DialogConstants.PATH_SEPARATOR)
                ? StringUtils.substringBeforeLast(prefix, DialogConstants.PATH_SEPARATOR)
                : StringUtils.EMPTY;

        Element effectiveElement = getRequiredElement(element, nodePrefix);

        Arrays.stream(annotation.annotationType().getDeclaredMethods())
                .filter(m -> {
                    if (mapProperties.value().length == 0) {
                        return true;
                    }
                    if (ArrayUtils.contains(mapProperties.value(), m.getName())) {
                        return true;
                    }
                    return !ArrayUtils.contains(mapProperties.value(), DialogConstants.NEGATION + m.getName());
                })
                .filter(m -> !skipped.contains(m.getName()))
                .forEach(m -> populateProperty(m, effectiveElement, annotation));
    }

    /**
     * Sets value of a particular {@code Annotation} property to an {@code Element} node
     * @param method {@code Method} instance representing a property of an annotation
     * @param element Element node
     * @param annotation Annotation to look for a value in
     */
    private static void populateProperty(Method method, Element element, Annotation annotation) {
        String name = method.getName();
        boolean ignorePrefix = false;
        if (method.isAnnotationPresent(PropertyRendering.class)) {
            PropertyRendering propertyRendering = method.getAnnotation(PropertyRendering.class);
            name = StringUtils.defaultIfBlank(propertyRendering.name(), name);
            ignorePrefix = propertyRendering.ignorePrefix();
        }
        String prefix = annotation.annotationType().getAnnotation(MapProperties.class).prefix();
        String namePrefix = prefix.contains(DialogConstants.PATH_SEPARATOR)
                ? StringUtils.substringAfterLast(prefix, DialogConstants.PATH_SEPARATOR)
                : prefix;
        if (!ignorePrefix && StringUtils.isNotBlank(prefix)) {
            name = namePrefix + name;
        }
        BinaryOperator<String> merger = XmlRuntime::mergeStringAttributes;
        AttributeHelper
            .forXmlTarget()
            .forAnnotationProperty(annotation, method)
            .withName(name)
            .withMerger(merger)
            .setTo(element);
    }

    /**
     * Retrieves the required {@code Element} of the specified node by its relative path.
     * @param element Current element node
     * @param path Relative path to required element
     * @return Element instance
     * */
    private Element getRequiredElement(Element element, String path) {
        if (StringUtils.isEmpty(path)) {
            return element;
        }
        return Pattern.compile(DialogConstants.PATH_SEPARATOR)
                .splitAsStream(path)
                .reduce(element, this::getParentOrChildElement, (prev, next) -> next);
    }

    /**
     * Gets whether this annotation method falls within the specified {@link Scope}. True if no scope specified
     * for method (that is, the method is applicable to any scope
     * @param method {@code Method} instance representing a property of an annotation
     * @param scope {@code PropertyScope} value
     * @return True or false
     */
    private static boolean fitsInScope(Method method, Scope scope) {
        if (!method.isAnnotationPresent(PropertyRendering.class)) {
            return true;
        }
        return Arrays.asList(method.getAnnotation(PropertyRendering.class).scope()).contains(scope);
    }


    /*
        Child elements
     */

    @Override
    public Element appendNonemptyChildElement(Element parent, Element child) {
        if (parent == null || isBlankElement(child)) {
            return null;
        }
        return appendNonemptyChildElement(parent, child, DEFAULT_ATTRIBUTE_MERGER);
    }

    /**
     * Tries to append provided {@code Element} node as a child to a parent {@code Element} node.
     * Appended node must be non-empty, i.e. containing at least one attribute that is not a {@code jcr:primaryType},
     * or a child node
     * If child node with same name already exists, it is updated with attribute values of the arriving node
     * @param parent Element to serve as parent
     * @param child Element to serve as child
     * @param attributeMerger Function that manages an existing attribute value and a new one
     *                        in case when a new value is set to an existing {@code Element}
     * @return Appended child
     */
    public Element appendNonemptyChildElement(Element parent, Element child, BinaryOperator<String> attributeMerger) {
        if (parent == null || isBlankElement(child)) {
            return null;
        }
        Element existingChild = getChildElement(parent, child.getNodeName());
        if (existingChild == null) {
            return (Element) parent.appendChild(child.cloneNode(true));
        }
        Node grandchild = child.getFirstChild();
        while (grandchild != null) {
            appendNonemptyChildElement(existingChild, (Element) grandchild, attributeMerger);
            grandchild = grandchild.getNextSibling();
        }
        return mergeAttributes(existingChild, child, attributeMerger);
    }

    /**
     * Merges attributes of two {@code Element} nodes, e.g. when a child node is appended to a parent node that already
     * has another child with same name, the existing child is updated with values from the newcomer. Way of merging
     * is defined by {@param attributeMerger} routine
     * @param first First (e.g. existing) Element node
     * @param second Second (e.g. rendered anew) Element node
     * @param attributeMerger Function that manages an existing attribute value and a new one
     * @return {@code Element} node with merged attribute values
     */
    private static Element mergeAttributes(Element first, Element second, BinaryOperator<String> attributeMerger) {
        NamedNodeMap newAttributes = second.getAttributes();
        for (int i = 0; i < newAttributes.getLength(); i++) {
            AttributeHelper
                .forXmlTarget()
                .forNamedValue(newAttributes.item(i).getNodeName(), String.class)
                .withMerger(attributeMerger)
                .setValue(newAttributes.item(i).getNodeValue(), first);
        }
        return first;
    }

    /**
     * Merges string attributes of two {@code Element} nodes, e.g. when a child node is appended to a parent node that already
     * has another child with same name, the existing child is updated with values from the newcomer.
     * Default way of merging is to replace first string with non-blank second string if they do not look like JCR lists,
     * or merge lists otherwise
     * @param first First (e.g. existing) Element node
     * @param second Second (e.g. rendered anew) Element node
     * @return {@code Element} node with merged attribute values
     */
    private static String mergeStringAttributes(String first, String second) {
        if (!StringUtil.isCollection(first) || !StringUtil.isCollection(second)) {
            return DEFAULT_ATTRIBUTE_MERGER.apply(first, second);
        }
        Set<String> result = StringUtil.parseSet(first);
        result.addAll(StringUtil.parseSet(second));
        return StringUtil.format(result, String.class);
    }

    @Override
    public Element getOrAddChildElement(Element parent, String childName) {
        return getChildElement(parent,
                childName,
                parentElement -> (Element) parentElement.appendChild(createNodeElement(childName)));
    }

    @Override
    public Element getChildElement(Element parent, String childName) {
        return getChildElement(parent, childName, p -> null);
    }

    /**
     * Retrieve child {@code Element} node of the specified node by name
     * @param parent Element to analyze
     * @param childName Name of child to look for
     * @param fallbackSupplier Routine that returns a fallback Element instance if parent node does not exist or child not found
     * @return Element instance
     */
    public Element getChildElement(Element parent, String childName, UnaryOperator<Element> fallbackSupplier) {
        if (parent == null) {
            return fallbackSupplier.apply(null);
        }
        if (childName.contains(DialogConstants.PATH_SEPARATOR)) {
            return Arrays.stream(StringUtils.split(childName, DialogConstants.PATH_SEPARATOR))
                    .reduce(parent, (p, c) -> getChildElement(p, c, fallbackSupplier), (c1, c2) -> c2);
        }
        Node child = parent.getFirstChild();
        while (child != null) {
            if (child instanceof Element && child.getNodeName().equals(childName)) {
                return (Element) child;
            }
            child = child.getNextSibling();
        }
        return fallbackSupplier.apply(parent);
    }

    /**
     * Retrieve parent or child {@code Element} node of the specified node by name
     * @param element Current element node
     * @param nodeName Name of the required element
     * @return Element instance
     */
    private Element getParentOrChildElement(Element element, String nodeName) {
        if (nodeName.contains(DialogConstants.PARENT_PATH_INDICATOR)){
            return (Element)element.getParentNode();
        }
        return getOrAddChildElement(element, nodeName);
    }

    /**
     * Retrieves list of {@link Element} nodes from the current document selected by {@link XPath}
     * @param xPath String xPath representation
     * @param document The document to search for nodes
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

    /**
     * Gets whether current {@code Element} is null, or is blank, i.e. has neither attributes, save for JCR type, nor children
     * @param element Element to check
     * @return True or false
     */
    private static boolean isBlankElement(Element element) {
        if (element == null) {
            return true;
        }
        if (element.hasChildNodes() || element.getAttributes().getLength() > 1) return false;
        return DialogConstants.PN_PRIMARY_TYPE.equals(element.getAttributes().item(0).getNodeName());
    }


    /*
        Data attributes
     */

    @Override
    public void appendDataAttributes(Element element, Data[] data) {
        if (ArrayUtils.isEmpty(data)) {
            return;
        }
        appendDataAttributes(element, Arrays.stream(data).collect(Collectors.toMap(Data::name, Data::value)));
    }

    @Override
    public void appendDataAttributes(Element element, Map<String, String> data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        Element graniteDataNode = getOrAddChildElement(element,
                DialogConstants.NN_GRANITE_DATA);
        data.entrySet().stream()
                .filter(entry -> StringUtils.isNotBlank(entry.getKey()))
                .forEach(entry -> graniteDataNode.setAttribute(entry.getKey(), entry.getValue()));
    }
}