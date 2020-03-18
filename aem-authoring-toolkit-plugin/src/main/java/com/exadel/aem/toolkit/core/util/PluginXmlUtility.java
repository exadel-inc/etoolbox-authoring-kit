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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import com.exadel.aem.toolkit.api.annotations.widgets.DataSource;
import com.exadel.aem.toolkit.api.annotations.widgets.property.Property;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.google.common.collect.ImmutableMap;

import com.exadel.aem.toolkit.api.annotations.meta.IgnorePropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyName;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyScope;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Data;
import com.exadel.aem.toolkit.api.annotations.widgets.common.XmlScope;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RteFeatures;
import com.exadel.aem.toolkit.api.runtime.XmlUtility;
import com.exadel.aem.toolkit.core.exceptions.ReflectionException;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.validation.Validation;

/**
 * Utility methods to process, verify and store AEM TouchUI dialog-related data to XML markup
 */
public class PluginXmlUtility implements XmlUtility {
    static final Map<String, String> XML_NAMESPACES = ImmutableMap.of(
            "xmlns:jcr", "http://www.jcp.org/jcr/1.0",
            "xmlns:nt", "http://www.jcp.org/jcr/nt/1.0",
            "xmlns:sling", JcrResourceConstants.SLING_NAMESPACE_URI,
            "xmlns:cq", "http://www.day.com/jcr/cq/1.0",
            "xmlns:granite", "http://www.adobe.com/jcr/granite/1.0"
    );

    public static final String ATTRIBUTE_LIST_TEMPLATE = "[%s]";
    public static final String ATTRIBUTE_LIST_SPLIT_PATTERN = "\\s*,\\s*";
    public static final String ATTRIBUTE_LIST_SURROUND = "[]";
    public static final Pattern ATTRIBUTE_LIST_PATTERN = Pattern.compile("^\\[.+]$");

    /**
     * Default routine to manage merging two values of an XML attribute by suppressing existing value with a non-empty new one
     */
    public static final BinaryOperator<String> DEFAULT_ATTRIBUTE_MERGER = (first, second) -> StringUtils.isNotBlank(second) ? second : first;

    private Document document;
    private String namePrefix = DialogConstants.RELATIVE_PATH_PREFIX;

    private XmlNamingHelper fieldNameHelper = XmlNamingHelper.forFieldName(this);
    private XmlNamingHelper simpleNameHelper = XmlNamingHelper.forSimpleName(this);
    private XmlNamingHelper namespaceNameHelper = XmlNamingHelper.forNamespaceAndName(this);

    /**
     * Initializes new {@link Document} instance shipped with the root element
     *
     * @param builder        {@link DocumentBuilder} to create new XML document
     * @param componentClass {@code Class} instance representing source object for this document
     * @return Root {@link Element}
     */
    Element newDocumentRoot(DocumentBuilder builder, Class<?> componentClass) {
        document = builder.newDocument();
        document.setUserData(DialogConstants.PN_COMPONENT_CLASS, componentClass, null);
        Element rootElement = createNodeElement(DialogConstants.NN_ROOT, XML_NAMESPACES);
        document.appendChild(rootElement);
        return rootElement;
    }

    /**
     * Retrieves current {@link Document} that is involved in {@code createNodeElement} routines
     *
     * @return {@code Document} instance
     */
    Document getCurrentDocument() {
        return document;
    }

    /**
     * Retrieves name prefix added to all {@code Element}s' tag names in current context
     *
     * @return Prefix as a string, default is "./"
     */
    public String getNamePrefix() {
        return this.namePrefix;
    }

    /**
     * Sets name prefix added to all Elements' tag names in current context
     *
     * @param namePrefix String value
     */
    public void setNamePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    @Override
    public Element createNodeElement(String name, String nodeType, Map<String, String> properties, String resourceType) {
        Element element = document.createElement(getValidName(name));
        if (nodeType == null) {
            nodeType = DialogConstants.NT_UNSTRUCTURED;
        }
        element.setAttribute(DialogConstants.PN_PRIMARY_TYPE, nodeType);
        if (resourceType != null) {
            element.setAttribute(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, resourceType);
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
     * Creates named XML {@code Element} node wrapping several child element nodes from an array of {@code Annotation} instance
     *
     * @param name                  Tag name of the wrapper XML node
     * @param childNodeNameProvider Function that generates child node's tag name based on corresponding Annotation instance
     * @param sources               Array of annotations to be rendered to XML
     * @return {@code Element} instance
     */
    public Element createNodeElement(String name, Function<Annotation, String> childNodeNameProvider, Annotation[] sources) {
        Element newNode = createNodeElement(name);
        Arrays.stream(sources)
                .forEach(source -> appendNonemptyChildElement(newNode, createNodeElement(childNodeNameProvider, source)));
        return newNode;
    }

    /**
     * Creates named XML {2code Element} node from specified {@code Annotation} using specified name provider
     *
     * @param nameProvider Function that processes {@code Annotation} instance to produce valid node name
     * @param source       Annotation to be rendered to XML
     * @return {@code Element} instance
     */
    private Element createNodeElement(Function<Annotation, String> nameProvider, Annotation source) {
        if (!Validation.forType(source.annotationType()).test(source)) {
            return null;
        }
        Element newNode = createNodeElement(nameProvider.apply(source));
        Arrays.stream(source.annotationType().getDeclaredMethods())
                .forEach(method -> XmlAttributeSettingHelper.forMethod(source, method).setAttribute(newNode));
        return newNode;
    }

    @Override
    public String getValidName(String name) {
        return namespaceNameHelper.getValidName(name, DialogConstants.NN_ITEM);
    }

    @Override
    public String getValidSimpleName(String name) {
        return simpleNameHelper.getValidName(name, DialogConstants.NN_ITEM);
    }

    @Override
    public String getValidFieldName(String name) {
        return fieldNameHelper.getValidName(name, DialogConstants.NN_FIELD);
    }

    @Override
    public String getUniqueName(String name, String defaultValue, Element context) {
        return simpleNameHelper.getUniqueName(name, defaultValue, context);
    }

    @Override
    public void setAttribute(Element element, String name, Double value) {
        XmlAttributeSettingHelper.forNamedValue(name, Double.class).setAttribute(element, value);
    }

    @Override
    public void setAttribute(Element element, String name, Long value) {
        XmlAttributeSettingHelper.forNamedValue(name, Long.class).setAttribute(element, value);
    }

    @Override
    public void setAttribute(Element element, String name, Boolean value) {
        XmlAttributeSettingHelper.forNamedValue(name, Boolean.class).setAttribute(element, value);
    }

    @Override
    public void setAttribute(Element element, String name, String value) {
        XmlAttributeSettingHelper.forNamedValue(name, String.class).setAttribute(element, value);
    }

    @Override
    public void setAttribute(Element element, String name, List<String> values) {
        setAttribute(element, name, values, PluginXmlUtility::mergeStringAttributes);
    }

    @Override
    public void setAttribute(Element element, String name, List<String> values, BinaryOperator<String> attributeMerger) {
        XmlAttributeSettingHelper.forNamedValue(name, String.class).withMerger(attributeMerger).setAttribute(element, values);
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
     *
     * @param elementSupplier Routine that generates and/or returns an {@code Element} node
     * @param name            Attribute name, same as annotation property name
     * @param source          Annotation to look fo a value in
     */
    public void setAttribute(Supplier<Element> elementSupplier, String name, Annotation source) {
        setAttribute(elementSupplier, name, source, DEFAULT_ATTRIBUTE_MERGER);
    }

    /**
     * Stores property value of a specific {@code Annotation} as an XML attribute
     *
     * @param elementSupplier Routine that generates and/or returns an {@code Element} node
     * @param name            Attribute name, same as annotation property name
     * @param source          Annotation to look for a value in
     * @param attributeMerger Function that manages an existing attribute value and a new one
     *                        in case when a new value is set to an existing {@code Element}
     */
    private static void setAttribute(Supplier<Element> elementSupplier,
                                     String name,
                                     Annotation source,
                                     BinaryOperator<String> attributeMerger) {
        try {
            Method sourceMethod = source.annotationType().getDeclaredMethod(name);
            if (!PluginReflectionUtility.annotationPropertyIsNotDefault(source, sourceMethod)) {
                return;
            }
            String effectiveName = sourceMethod.isAnnotationPresent(PropertyName.class) && StringUtils.isNotBlank(sourceMethod.getAnnotation(PropertyName.class).value())
                    ? sourceMethod.getAnnotation(PropertyName.class).value()
                    : name;
            XmlAttributeSettingHelper.forMethod(source, sourceMethod)
                    .withName(effectiveName)
                    .withMerger(attributeMerger)
                    .setAttribute(elementSupplier.get());
        } catch (NoSuchMethodException e) {
            PluginRuntime.context().getExceptionHandler().handle(new ReflectionException(source.getClass(), name));
        }
    }

    @Override
    public void mapProperties(Element element, Annotation annotation) {
        mapProperties(element, annotation, Collections.emptyList());
    }

    @Override
    public void mapProperties(Element element, Annotation annotation, XmlScope scope) {
        List<String> skippedFields = Arrays.stream(annotation.annotationType().getDeclaredMethods())
                .filter(m -> !fitsInScope(m, scope))
                .map(Method::getName)
                .collect(Collectors.toList());
        mapProperties(element, annotation, skippedFields);
    }

    @Override
    public void mapProperties(Element element, Annotation annotation, List<String> skipped) {
        PropertyMapping propMapping = annotation.annotationType().getDeclaredAnnotation(PropertyMapping.class);
        if (propMapping == null) {
            return;
        }
        String prefix = annotation.annotationType().getAnnotation(PropertyMapping.class).prefix();
        String nodePrefix = prefix.contains(DialogConstants.PATH_SEPARATOR)
                ? StringUtils.substringBeforeLast(prefix, DialogConstants.PATH_SEPARATOR)
                : StringUtils.EMPTY;

        Element currentElement = StringUtils.isEmpty(nodePrefix)
                ? element
                : Pattern.compile(DialogConstants.PATH_SEPARATOR).splitAsStream(nodePrefix)
                .reduce(element, this::getOrAddChildElement, (prev, next) -> next);
        Arrays.stream(annotation.annotationType().getDeclaredMethods())
                .filter(m -> ArrayUtils.isEmpty(propMapping.mappings()) || ArrayUtils.contains(propMapping.mappings(), m.getName()))
                .filter(m -> !m.isAnnotationPresent(IgnorePropertyMapping.class))
                .filter(m -> !skipped.contains(m.getName()))
                .forEach(m -> populateProperty(m, currentElement, annotation));
    }

    /**
     * Sets value of a particular {@code Annotation} property to an {@code Element} node
     *
     * @param method     {@code Method} instance representing a property of an annotation
     * @param element    Element node
     * @param annotation Annotation to look for a value in
     */
    private static void populateProperty(Method method, Element element, Annotation annotation) {
        String name = method.getName();
        boolean ignorePrefix = false;
        if (method.isAnnotationPresent(PropertyName.class)) {
            PropertyName propertyName = method.getAnnotation(PropertyName.class);
            name = propertyName.value();
            ignorePrefix = propertyName.ignorePrefix();
        }
        String prefix = annotation.annotationType().getAnnotation(PropertyMapping.class).prefix();
        String namePrefix = prefix.contains(DialogConstants.PATH_SEPARATOR)
                ? StringUtils.substringAfterLast(prefix, DialogConstants.PATH_SEPARATOR)
                : prefix;
        if (!ignorePrefix && StringUtils.isNotBlank(prefix)) {
            name = namePrefix + name;
        }
        BinaryOperator<String> merger = PluginXmlUtility::mergeStringAttributes;
        XmlAttributeSettingHelper.forMethod(annotation, method)
                .withName(name)
                .withMerger(merger)
                .setAttribute(element);
    }

    /**
     * Gets whether this annotation method falls within the specified {@link PropertyScope}. True if no scope specified
     * for method (that is, the method is applicable to any scope
     *
     * @param method {@code Method} instance representing a property of an annotation
     * @param scope  {@code PropertyScope} value
     * @return True or false
     */
    private static boolean fitsInScope(Method method, XmlScope scope) {
        if (!method.isAnnotationPresent(PropertyScope.class)) {
            return true;
        }
        return Arrays.asList(method.getAnnotation(PropertyScope.class).value()).contains(scope);
    }

    /**
     * Tries to append provided {@code Element} node as a child to a parent {@code Element} node.
     * Appended node must be non-empty, i.e. containing at least one attribute that is not a {@code jcr:primaryType},
     * or a child node
     * If child node with same name already exists, it is updated with attribute values of the newcomer node
     *
     * @param parent          Routine than provides Element to serve as parent
     * @param child           Element to serve as child
     * @param attributeMerger Function that manages an existing attribute value and a new one
     *                        in case when a new value is set to an existing {@code Element}
     */
    public void appendNonemptyChildElement(Supplier<Element> parent, Element child, BinaryOperator<String> attributeMerger) {
        if (isBlankElement(child)) {
            return;
        }
        appendNonemptyChildElement(parent.get(), child, attributeMerger);
    }

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
     *
     * @param parent          Element to serve as parent
     * @param child           Element to serve as child
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
     *
     * @param first           First (e.g. existing) Element node
     * @param second          Second (e.g. rendered anew) Element node
     * @param attributeMerger Function that manages an existing attribute value and a new one
     * @return {@code Element} node with merged attribute values
     */
    private static Element mergeAttributes(Element first, Element second, BinaryOperator<String> attributeMerger) {
        NamedNodeMap newAttributes = second.getAttributes();
        for (int i = 0; i < newAttributes.getLength(); i++) {
            XmlAttributeSettingHelper.forNamedValue(newAttributes.item(i).getNodeName(), String.class)
                    .withMerger(attributeMerger)
                    .setAttribute(first, newAttributes.item(i).getNodeValue());
        }
        return first;
    }

    /**
     * Merges string attributes of two {@code Element} nodes, e.g. when a child node is appended to a parent node that already
     * has another child with same name, the existing child is updated with values from the newcomer.
     * Default way of merging is to replace first string with non-blank second string if they do not look like JCR lists,
     * or merge lists otherwise
     *
     * @param first  First (e.g. existing) Element node
     * @param second Second (e.g. rendered anew) Element node
     * @return {@code Element} node with merged attribute values
     */
    public static String mergeStringAttributes(String first, String second) {
        if (!ATTRIBUTE_LIST_PATTERN.matcher(first).matches() || !ATTRIBUTE_LIST_PATTERN.matcher(second).matches()) {
            return DEFAULT_ATTRIBUTE_MERGER.apply(first, second);
        }
        Set<String> result = new HashSet<>(Arrays.asList(StringUtils
                .strip(first, ATTRIBUTE_LIST_SURROUND)
                .split(ATTRIBUTE_LIST_SPLIT_PATTERN)));
        result.addAll(new HashSet<>(Arrays.asList(StringUtils
                .strip(second, ATTRIBUTE_LIST_SURROUND)
                .split(ATTRIBUTE_LIST_SPLIT_PATTERN))));
        return String.format(ATTRIBUTE_LIST_TEMPLATE, String.join(RteFeatures.FEATURE_SEPARATOR, result));
    }

    @Override
    public Element getOrAddChildElement(Element parent, String child) {
        return getChildElementNode(parent,
                child,
                parentElement -> (Element) parentElement.appendChild(createNodeElement(child)));
    }

    @Override
    public Element getChildElement(Element parent, String child) {
        return getChildElementNode(parent, child, p -> null);
    }

    /**
     * Retrieve child {@code Element} node of the specified node by name
     *
     * @param parent           Element to analyze
     * @param childName        Name of child to look for
     * @param fallbackSupplier Routine that returns a fallback Element instance if parent node does not exist or child not found
     * @return Element instance
     */
    public Element getChildElementNode(Element parent, String childName, UnaryOperator<Element> fallbackSupplier) {
        if (parent == null) {
            return fallbackSupplier.apply(null);
        }
        if (childName.contains(DialogConstants.PATH_SEPARATOR)) {
            return Arrays.stream(StringUtils.split(childName, DialogConstants.PATH_SEPARATOR))
                    .reduce(parent, (p, c) -> getChildElementNode(p, c, fallbackSupplier), (c1, c2) -> c2);
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
     * Retrieves list of {@link Element} nodes from the current document selected by {@link XPath}
     *
     * @param xPath String xPath representation
     * @return List of {@code Element}s, or an empty list
     */
    List<Element> getElementNodes(String xPath) {
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
     *
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
                DialogConstants.NN_DATA);
        data.entrySet().stream()
                .filter(entry -> StringUtils.isNotBlank(entry.getKey()))
                .forEach(entry -> graniteDataNode.setAttribute(entry.getKey(), entry.getValue()));
    }

    @Override
    public void appendDataSource(Element element, DataSource dataSource) {
        appendDataSource(element, dataSource.path(), dataSource.resourceType(), Arrays.stream(dataSource.properties()).collect(Collectors.toMap(Property::name, Property::value)));
    }

    @Override
    public Element appendDataSource(Element element, String path, String resourceType, Map<String, String> properties) {
        if (StringUtils.isBlank(resourceType)) {
            return null;
        }
        properties.put(DialogConstants.PN_PATH, path);
        Element dataSourceElement = createNodeElement(DialogConstants.NN_DATASOURCE, null,
                properties, resourceType);
        return (Element) element.appendChild(dataSourceElement);
    }

    @Override
    public Element appendAcsCommonsList(Element element, String path, String resourceType) {
        if (StringUtils.isBlank(path)) {
            return null;
        }
        Element dataSourceElement = createNodeElement(DialogConstants.NN_DATASOURCE, null,
                new ImmutableMap.Builder<String, String>().put(DialogConstants.PN_PATH, path).build(),
                resourceType.isEmpty() ? ResourceTypes.ACS_LIST : resourceType);
        return (Element) element.appendChild(dataSourceElement);
    }
}
