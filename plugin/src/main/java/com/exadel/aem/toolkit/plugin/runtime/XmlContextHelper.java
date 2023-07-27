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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.exadel.aem.toolkit.api.annotations.meta.AnnotationRendering;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Data;
import com.exadel.aem.toolkit.api.runtime.XmlUtility;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.metadata.Metadata;
import com.exadel.aem.toolkit.plugin.metadata.Property;
import com.exadel.aem.toolkit.plugin.metadata.RenderingFilter;
import com.exadel.aem.toolkit.plugin.targets.AttributeHelper;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;
import com.exadel.aem.toolkit.plugin.utils.NamingUtil;
import com.exadel.aem.toolkit.plugin.utils.StringUtil;
import com.exadel.aem.toolkit.plugin.validators.Validation;

/**
 * Processes, verifies, and stores Granite UI-related data to XML markup
 */
@SuppressWarnings("deprecation") // XmlUtility is retained for compatibility reasons (used in legacy custom handlers)
// This will be retired in a version after 2.0.2
public class XmlContextHelper implements XmlUtility {

    /**
     * Default routine to manage the merging of two values of an XML attribute by suppressing existing value in favor of
     * a non-empty new one
     */
    private static final BinaryOperator<String> DEFAULT_ATTRIBUTE_MERGER = (first, second) -> StringUtils.isNotBlank(second) ? second : first;

    /* ---------------------------------
       Instance members and constructors
       --------------------------------- */

    private final Document document;

    /**
     * Default constructor
     * @param document {@code Document} instance to be used as a node factory within this instance
     */
    public XmlContextHelper(Document document) {
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

    /**
     * {@inheritDoc}
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
                if (StringUtils.isNoneBlank(key, value)) {
                    element.setAttribute(key, value);
                }
            });
        }
        return element;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Element createNodeElement(String name, String nodeType, Map<String, String> properties) {
        return createNodeElement(name, nodeType, properties, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Element createNodeElement(String name, Map<String, String> properties, String resourceType) {
        return createNodeElement(name, null, properties, resourceType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Element createNodeElement(String name, String resourceType) {
        return createNodeElement(name, null, new HashMap<>(), resourceType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Element createNodeElement(String name, Map<String, String> properties) {
        return createNodeElement(name, null, properties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Element createNodeElement(String name) {
        return createNodeElement(name, null, new HashMap<>());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Element createNodeElement(String name, Annotation source) {
        return createNodeElement(annotation -> name, source);
    }

    /**
     * Creates a named XML {@code Element} node from the specified {@code Annotation} using the specified name provider
     * @param nameProvider Function that processes {@code Annotation} instance to produce valid node name
     * @param source       Annotation to be rendered to XML
     * @return {@code Element} instance
     */
    private Element createNodeElement(Function<Annotation, String> nameProvider, Annotation source) {
        if (!Validation.forType(source.annotationType()).test(source)) {
            return null;
        }
        Element newNode = createNodeElement(nameProvider.apply(source));
        Metadata.from(source).forEach(property ->
            AttributeHelper.forXmlTarget().forAnnotationProperty(source, property).setTo(newNode));
        return newNode;
    }

    /*
        Element naming
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValidName(String name) {
        return NamingUtil.getValidNodeName(name, CoreConstants.NN_ITEM);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValidSimpleName(String name) {
        return NamingUtil.getValidNodeName(name, CoreConstants.NN_ITEM);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValidFieldName(String name) {
        return NamingUtil.getValidNodeName(name, DialogConstants.NN_FIELD);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUniqueName(String name, String defaultValue, Element context) {
        return NamingUtil.getUniqueName(name, defaultValue, context);
    }


    /*
        Setting arbitrary attributes
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(Element element, String name, Double value) {
        AttributeHelper
            .forXmlTarget()
            .forNamedValue(name, Double.class)
            .setValue(value, element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(Element element, String name, Long value) {
        AttributeHelper
            .forXmlTarget()
            .forNamedValue(name, Long.class)
            .setValue(value, element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(Element element, String name, Boolean value) {
        AttributeHelper
            .forXmlTarget()
            .forNamedValue(name, Boolean.class)
            .setValue(value, element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(Element element, String name, String value) {
        AttributeHelper
            .forXmlTarget()
            .forNamedValue(name, String.class)
            .setValue(value, element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(Element element, String name, List<String> values) {
        setAttribute(element, name, values, XmlContextHelper::mergeStringAttributes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(Element element, String name, List<String> values, BinaryOperator<String> attributeMerger) {
        AttributeHelper
            .forXmlTarget()
            .forNamedValue(name, String.class)
            .withMerger(attributeMerger)
            .setValue(values, element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(Element element, String name, Annotation source) {
        setAttribute(element, name, source, DEFAULT_ATTRIBUTE_MERGER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(Element element, String name, Annotation source, BinaryOperator<String> attributeMerger) {
        setAttribute(() -> element, name, source, attributeMerger);
    }

    /**
     * Stores property value of a specific {@code Annotation} as an XML attribute
     * @param elementSupplier Routine that generates and/or returns an {@code Element} node
     * @param name            Attribute name, same as the annotation property name
     * @param source          Annotation to look for a value in
     */
    public void setAttribute(Supplier<Element> elementSupplier, String name, Annotation source) {
        setAttribute(elementSupplier, name, source, DEFAULT_ATTRIBUTE_MERGER);
    }

    /**
     * Stores property value of a specific {@code Annotation} as an XML attribute
     * @param elementSupplier Routine that generates and/or returns an {@code Element} node
     * @param name            Attribute name, same as the annotation property name
     * @param source          Annotation to look for a value in
     * @param attributeMerger A function that manages an existing attribute value and a new one in the case when a new
     *                        value is set to an existing {@code Element}
     */
    private static void setAttribute(Supplier<Element> elementSupplier,
                                     String name,
                                     Annotation source,
                                     BinaryOperator<String> attributeMerger) {
        Property property = Metadata.from(source)
            .stream()
            .filter(prop -> StringUtils.equals(name, prop.getName()))
            .findFirst()
            .orElse(null);
        if (property == null || property.valueIsDefault()) {
            return;
        }
        PropertyRendering propertyRendering = property.getAnnotation(PropertyRendering.class);
        String effectiveName = name;
        if (propertyRendering != null) {
            effectiveName = StringUtils.defaultIfBlank(propertyRendering.name(), name);
        }
        AttributeHelper
            .forXmlTarget()
            .forAnnotationProperty(source, property)
            .withName(effectiveName)
            .withMerger(attributeMerger)
            .setTo(elementSupplier.get());
    }

    /*
        Mapping annotation properties
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void mapProperties(Element element, Annotation annotation) {
        mapProperties(element, annotation, Collections.emptyList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mapProperties(Element element, Annotation annotation, String scope) {
        List<String> skippedFields = Metadata.from(annotation)
            .stream()
            .filter(prop -> !fitsInScope(prop, scope))
            .map(Property::getName)
            .collect(Collectors.toList());
        mapProperties(element, annotation, skippedFields);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mapProperties(Element element, Annotation annotation, List<String> skipped) {
        Annotation prefixHolder = Metadata.from(annotation)
            .getAnyAnnotation(AnnotationRendering.class, PropertyMapping.class);
        if (prefixHolder == null) {
            return;
        }
        String prefix = String.valueOf(Metadata.from(prefixHolder).getValue(DialogConstants.PN_PREFIX));
        String nodePrefix = prefix.contains(CoreConstants.SEPARATOR_SLASH)
            ? StringUtils.substringBeforeLast(prefix, CoreConstants.SEPARATOR_SLASH)
            : StringUtils.EMPTY;

        Element effectiveElement = getRequiredElement(element, nodePrefix);

        RenderingFilter renderingFilter = new RenderingFilter(annotation);
        Metadata.from(annotation)
            .stream()
            .filter(property -> property.matches(renderingFilter))
            .filter(property -> !skipped.contains(property.getName()))
            .forEach(property -> populateProperty(property, nodePrefix, effectiveElement, annotation));
    }

    /**
     * Sets value of a particular {@code Annotation} property to an {@code Element} node
     * @param property   {@link Property} instance representing a property of an annotation
     * @param prefix     String that prepends the rendered property name
     * @param element    Element node
     * @param annotation Annotation to look for a value in
     */
    private static void populateProperty(Property property, String prefix, Element element, Annotation annotation) {
        String name = property.getName();
        boolean ignorePrefix = false;
        PropertyRendering propertyRendering = property.getAnnotation(PropertyRendering.class);
        if (propertyRendering != null) {
            name = StringUtils.defaultIfBlank(propertyRendering.name(), name);
            ignorePrefix = propertyRendering.ignorePrefix();
        }

        if (!ignorePrefix && StringUtils.isNotBlank(prefix)) {
            name = prefix + name;
        }
        BinaryOperator<String> merger = XmlContextHelper::mergeStringAttributes;
        AttributeHelper
            .forXmlTarget()
            .forAnnotationProperty(annotation, property)
            .withName(name)
            .withMerger(merger)
            .setTo(element);
    }

    /**
     * Retrieves the required {@code Element} of the specified node by its relative path.
     * @param element Current element node
     * @param path    Relative path to required element
     * @return Element instance
     */
    private Element getRequiredElement(Element element, String path) {
        if (StringUtils.isEmpty(path)) {
            return element;
        }
        return Pattern.compile(CoreConstants.SEPARATOR_SLASH)
            .splitAsStream(path)
            .reduce(element, this::getParentOrChildElement, (prev, next) -> next);
    }

    /**
     * Gets whether this annotation method falls within the specified scope. True if no scope specified for method (that
     * is, the method is applicable to any scope
     * @param property {@link Property} instance representing a property of the annotation
     * @param scope  String representing a valid scope
     * @return True or false
     * @see com.exadel.aem.toolkit.api.annotations.meta.Scopes
     */
    private static boolean fitsInScope(Property property, String scope) {
        PropertyRendering propertyRendering = property.getAnnotation(PropertyRendering.class);
        if (propertyRendering == null) {
            return true;
        }
        return Arrays.asList(propertyRendering.scope()).contains(scope);
    }


    /*
        Child elements
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public Element appendNonemptyChildElement(Element parent, Element child) {
        if (parent == null || isBlankElement(child)) {
            return null;
        }
        return appendNonemptyChildElement(parent, child, DEFAULT_ATTRIBUTE_MERGER);
    }

    /**
     * Tries to append provided {@code Element} node as a child to a parent {@code Element} node. The appended node must
     * be non-empty, i.e. containing at least one attribute that is not a {@code jcr:primaryType}, or a child node If a
     * child node with the same name already exists, it is updated with attribute values of the arriving node
     * @param parent          Element to serve as parent
     * @param child           Element to serve as child
     * @param attributeMerger Function that manages an existing attribute value and a new one in the case when a new
     *                        value is set to an existing {@code Element}
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
     * has another child with the same name, the existing child is updated with values from the newcomer. Way of merging
     * is defined by the {@code attributeMerger} routine
     * @param first           First (e.g. existing) Element node
     * @param second          Second (e.g. rendered anew) Element node
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
     * Merges string attributes of two {@code Element} nodes, e.g. when a child node is appended to a parent node that
     * already has another child with the same name, the existing child is updated with values from the newcomer. The
     * default way of merging is to replace the first string with a non-blank second string if they do not look like JCR
     * lists, or merge lists otherwise
     * @param first  First (e.g. existing) Element node
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Element getOrAddChildElement(Element parent, String childName) {
        return getChildElement(parent,
            childName,
            parentElement -> (Element) parentElement.appendChild(createNodeElement(childName)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Element getChildElement(Element parent, String childName) {
        return getChildElement(parent, childName, p -> null);
    }

    /**
     * Retrieve child {@code Element} node of the specified node by name
     * @param parent           Element to analyze
     * @param childName        Name of the child to look for
     * @param fallbackSupplier A routine that returns a fallback Element instance if parent node does not exist or child
     *                         not found
     * @return Element instance
     */
    public Element getChildElement(Element parent, String childName, UnaryOperator<Element> fallbackSupplier) {
        if (parent == null) {
            return fallbackSupplier.apply(null);
        }
        if (childName.contains(CoreConstants.SEPARATOR_SLASH)) {
            return Arrays.stream(StringUtils.split(childName, CoreConstants.SEPARATOR_SLASH))
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
     * @param element  Current element node
     * @param nodeName Name of the required element
     * @return Element instance
     */
    private Element getParentOrChildElement(Element element, String nodeName) {
        if (nodeName.contains(CoreConstants.PARENT_PATH)) {
            return (Element) element.getParentNode();
        }
        return getOrAddChildElement(element, nodeName);
    }

    /**
     * Gets whether current {@code Element} is null, or is blank, i.e. has neither attributes, save for JCR type, nor
     * children
     * @param element Element to check
     * @return True or false
     */
    private static boolean isBlankElement(Element element) {
        if (element == null) {
            return true;
        }
        if (element.hasChildNodes() || element.getAttributes().getLength() > 1) {
            return false;
        }
        return DialogConstants.PN_PRIMARY_TYPE.equals(element.getAttributes().item(0).getNodeName());
    }


    /*
        Data attributes
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void appendDataAttributes(Element element, Data[] data) {
        if (ArrayUtils.isEmpty(data)) {
            return;
        }
        appendDataAttributes(element, Arrays.stream(data).collect(Collectors.toMap(Data::name, Data::value)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void appendDataAttributes(Element element, Map<String, String> data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        Element graniteDataNode = getOrAddChildElement(element,
            CoreConstants.NN_GRANITE_DATA);
        data.entrySet().stream()
            .filter(entry -> StringUtils.isNotBlank(entry.getKey()))
            .forEach(entry -> graniteDataNode.setAttribute(entry.getKey(), entry.getValue()));
    }
}
