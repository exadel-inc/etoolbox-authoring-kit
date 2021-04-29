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
package com.exadel.aem.toolkit.api.runtime;

import java.lang.annotation.Annotation;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;

import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Data;
import com.exadel.aem.toolkit.api.handlers.Source;

/**
 * An abstraction of class encapsulating routines for XML generation and handling
 * @deprecated Since v. 2.0.2 users are encouraged to use the new custom handlers API that is based
 * on {@link Source} and {@link Target} objects handling. Legacy API will be removed in the versions to come
 */
@Deprecated
@SuppressWarnings({"unused", "squid:S1133"})
public interface XmlUtility {

    /**
     * Creates named XML {@code Element} node with default JCR type
     * @param name Tag name of the XML node
     * @return {@code Element} instance
     */
    Element createNodeElement(String name);

    /**
     * Creates named XML {@code Element} node with default JCR type and specific {@code sling:resourceType}
     * @param name         Tag name of the XML node
     * @param resourceType Value of {@code sling:resourceType} attribute
     * @return {@code Element} instance
     */
    Element createNodeElement(String name, String resourceType);

    /**
     * Creates named XML {@code Element} node with default JCR type and additional properties
     * @param name       Tag name of the XML node
     * @param properties {@code Map} of String values to be rendered as additional XML node attributes
     * @return {@code Element} instance
     */
    Element createNodeElement(String name, Map<String, String> properties);

    /**
     * Creates named XML {@code Element} node with specific JCR type and optional properties
     * @param name       Tag name of the XML node
     * @param nodeType   Value of {@code jcr:primaryType} attribute
     * @param properties {@code Map} of String values to be rendered as additional XML node attributes
     * @return {@code Element} instance
     */
    Element createNodeElement(String name, String nodeType, Map<String, String> properties);

    /**
     * Creates named XML {@code Element} node with default JCR type,
     * additional properties, and specific {@code sling:resourceType}
     * @param name         Tag name of the XML node
     * @param properties   {@code Map} of String values to be rendered as additional XML node attributes
     * @param resourceType Value of {@code sling:resourceType} attribute
     * @return {@code Element} instance
     */
    Element createNodeElement(String name, Map<String, String> properties, String resourceType);

    /**
     * Creates named XML {@link Element} node with specific JCR type and optional properties
     * @param name         Tag name of the XML node
     * @param nodeType     Value of {@code jcr:primaryType} attribute
     * @param properties   {@link Map} of String values to be rendered as additional XML node attributes
     * @param resourceType Value of {@code sling:resourceType} attribute
     * @return {@code Element} instance
     */
    Element createNodeElement(String name, String nodeType, Map<String, String> properties, String resourceType);

    /**
     * Creates named XML {@code Element} node from existing {@code Annotation} instance
     * @param name   Tag name of the XML node
     * @param source Annotation to be rendered to XML
     * @return {@code Element} instance
     */
    Element createNodeElement(String name, Annotation source);

    /**
     * Stores Double-typed value as an XML attribute
     * @param element {@code Element} node
     * @param name    Attribute name
     * @param value   Value to set
     */
    void setAttribute(Element element, String name, Double value);

    /**
     * Stores Long-typed value as an XML attribute
     * @param element {@code Element} node
     * @param name    Attribute name
     * @param value   Value to set
     */
    void setAttribute(Element element, String name, Long value);

    /**
     * Stores Boolean-typed value as an XML attribute
     * @param element {@code Element} node
     * @param name    Attribute name
     * @param value   Value to set
     */
    void setAttribute(Element element, String name, Boolean value);

    /**
     * Stores String value as an XML attribute
     * @param element {@code Element} node
     * @param name    Attribute name
     * @param value   Value to set
     */
    void setAttribute(Element element, String name, String value);

    /**
     * Stores list of String values as an XML attribute
     * @param element {@code Element} node
     * @param name    Attribute name
     * @param values  Values to set
     */
    void setAttribute(Element element, String name, List<String> values);

    /**
     * Stores list of String values as an XML attribute
     * @param element         {@code Element} node
     * @param name            Attribute name
     * @param values          Values to set
     * @param attributeMerger Function that manages an existing attribute value and a new one
     *                        in case when a new value is set to an existing {@code Element}
     */
    void setAttribute(Element element, String name, List<String> values, BinaryOperator<String> attributeMerger);

    /**
     * Stores property value of a specific {@code Annotation} as an XML attribute
     * @param element {@code Element} node
     * @param name    Attribute name, same as annotation property name
     * @param source  Annotation to look for a value in
     */
    void setAttribute(Element element, String name, Annotation source);

    /**
     * Stores property value of a specific {@code Annotation} as an XML attribute
     * @param element         {@code Element} node
     * @param name            Attribute name, same as annotation property name
     * @param source          Annotation to look for a value in
     * @param attributeMerger Function that manages an existing attribute value and a new one
     *                        in case when a new value is set to an existing {@code Element}
     */
    void setAttribute(Element element, String name, Annotation source, BinaryOperator<String> attributeMerger);

    /**
     * Populates {@code Element} node with all eligible property values of an {@code Annotation} instance
     * @param element    Element node
     * @param annotation Annotation to take property values from
     */
    void mapProperties(Element element, Annotation annotation);

    /**
     * Populates {@code Element} node with all eligible property values of an {@code Annotation} instance,
     * honoring the scope specified for an annotation or a particular annotation method
     * @param element    Element node
     * @param annotation Annotation to take property values from
     * @param scope      Non-null string representing the current scope
     */
    void mapProperties(Element element, Annotation annotation, String scope);

    /**
     * Populates {@code Element} node with property values of an {@code Annotation} instance,
     * skipping specified annotation fields
     * @param element       Element node
     * @param annotation    Annotation to take property values from
     * @param skippedFields List of field names to skip
     */
    void mapProperties(Element element, Annotation annotation, List<String> skippedFields);

    /**
     * Appends {@link Data} values to an {@code Element} node, storing them within {@code granite:data} predefined subnode
     * @param element Element to store data in
     * @param data    Provided values as an array of {@code Data} annotations
     */
    void appendDataAttributes(Element element, Data[] data);

    /**
     * Appends the values to an {@code Element} node, storing them within {@code granite:data} predefined subnode
     * @param element Element to store data in
     * @param data    Provided values as a {@code Map<String, String>} instance
     */
    void appendDataAttributes(Element element, Map<String, String> data);

    /**
     * Tries to append provided {@code Element} node as a child to a parent {@code Element} node.
     * The appended node must be non-empty, i.e. containing at least one attribute that is not a {@code jcr:primaryType},
     * or a child node
     * If a child node with the same name already exists, it is updated with attribute values of the newly arrived node
     * @param parent Routine than provides Element to serve as parent
     * @param child  Element to serve as child
     * @return Appended child
     */
    Element appendNonemptyChildElement(Element parent, Element child);

    /**
     * Retrieves child {@code Element} node of the specified node by its name / relative path. Same as {@link XmlUtility#getOrAddChildElement(Element, String)},
     * but if the parent's child (or any of the specified grandchildren) do not exist, a null value is returned
     * @param parent Element to analyze
     * @param child  Name of a child to look for, can be a simple name or a relative path e.g. {@code child/otherChild/yetAnotherChild}
     * @return Element instance if path traversing was successful, null otherwise
     */
    Element getChildElement(Element parent, String child);

    /**
     * Retrieves child {@code Element} node of the specified node by its name / relative path.Same as {@link XmlUtility#getChildElement(Element, String)},
     * but as soon as the parent's child (or any of the specified grandchildren) not found, an empty node of {@code jcr:primaryType="nt:unstructured"}
     * is created
     * @param parent Element to analyze
     * @param child  Name of a child to look for, can be a simple name or a relative path e.g. {@code child/otherChild/yetAnotherChild}
     * @return Element instance
     */
    Element getOrAddChildElement(Element parent, String child);

    /**
     * Generates compliant XML tag or attribute name (optionally prepended by an XML namespace) from an arbitrary string
     * @param name Raw (unchecked) string for a tag/attribute name
     * @return Valid tag/attribute name
     */
    String getValidName(String name);

    /**
     * Generates compliant XML tag or attribute name without a namespace part from an arbitrary string
     * @param name Raw (unchecked) string for a tag/attribute name
     * @return Valid tag/attribute name
     */
    String getValidSimpleName(String name);

    /**
     * Generates compliant XML field name from an arbitrary string
     * @param name Raw (unchecked) string for a field name
     * @return Valid field name
     */
    String getValidFieldName(String name);

    /**
     * Generates compliant XML tag name thai is unique within the scope of specified parent node
     * @param name         Raw (unchecked) string for a tag name
     * @param defaultValue Source string for unique (indexed) name generation
     * @param context      Parent node
     * @return Valid and locally unique tag name
     */
    String getUniqueName(String name, String defaultValue, Element context);
}
