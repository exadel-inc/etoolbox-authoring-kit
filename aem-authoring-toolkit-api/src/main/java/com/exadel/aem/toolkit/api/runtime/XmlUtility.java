package com.exadel.aem.toolkit.api.runtime;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;

import com.exadel.aem.toolkit.api.annotations.widgets.DataSource;
import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Data;
import com.exadel.aem.toolkit.api.annotations.widgets.common.XmlScope;

/**
 * An abstraction of class encapsulating routines for XML generation and handling
 */
public interface XmlUtility {
    /**
     * Creates named XML {@code Element} node with default JCR type
     * @param name Tag name of the XML node
     * @return {@code Element} instance
     */
    Element createNodeElement(String name);

    /**
     * Creates named XML {@code Element} node with default JCR type and specific {@code sling:resourceType}
     * @param name Tag name of the XML node
     * @param resourceType Value of {@code sling:resourceType} attribute
     * @return {@code Element} instance
     */
    Element createNodeElement(String name, String resourceType);

    /**
     * Creates named XML {@code Element} node with default JCR type and additional properties
     * @param name Tag name of the XML node
     * @param properties {@code Map} of String values to be rendered as additional XML node attributes
     * @return {@code Element} instance
     */
    Element createNodeElement(String name, Map<String,String> properties);

    /**
     * Creates named XML {@code Element} node with specific JCR type and optional properties
     * @param name Tag name of the XML node
     * @param nodeType Value of {@code jcr:primaryType} attribute
     * @param properties {@code Map} of String values to be rendered as additional XML node attributes
     * @return {@code Element} instance
     */
    Element createNodeElement(String name, String nodeType, Map<String,String> properties);

    /**
     * Creates named XML {@code Element} node with default JCR type,
     * additional properties, and specific {@code sling:resourceType}
     * @param name Tag name of the XML node
     * @param properties {@code Map} of String values to be rendered as additional XML node attributes
     * @param resourceType Value of {@code sling:resourceType} attribute
     * @return {@code Element} instance
     */
    Element createNodeElement(String name, Map<String,String> properties, String resourceType);

    /**
     * Creates named XML {@link Element} node with specific JCR type and optional properties
     * @param name Tag name of the XML node
     * @param nodeType Value of {@code jcr:primaryType} attribute
     * @param properties {@link Map} of String values to be rendered as additional XML node attributes
     * @param resourceType Value of {@code sling:resourceType} attribute
     * @return {@code Element} instance
     */
    Element createNodeElement(String name, String nodeType, Map<String, String> properties, String resourceType);

    /**
     * Creates named XML {@code Element} node from existing {@code Annotation} instance
     * @param name Tag name of the XML node
     * @param source Annotation to be rendered to XML
     * @return {@code Element} instance
     */
    Element createNodeElement(String name, Annotation source);

    /**
     * Stores Double-typed value as an XML attribute
     * @param element {@code Element} node
     * @param name Attribute name
     * @param value Value to set
     */
    void setAttribute(Element element, String name, Double value);

    /**
     * Stores Long-typed value as an XML attribute
     * @param element {@code Element} node
     * @param name Attribute name
     * @param value Value to set
     */
    void setAttribute(Element element, String name, Long value);

    /**
     * Stores Boolean-typed value as an XML attribute
     * @param element {@code Element} node
     * @param name Attribute name
     * @param value Value to set
     */
    void setAttribute(Element element, String name, Boolean value);

    /**
     * Stores String value as an XML attribute
     * @param element {@code Element} node
     * @param name Attribute name
     * @param value Value to set
     */
    void setAttribute(Element element, String name, String value);

    /**
     * Stores list of String values as an XML attribute
     * @param element {@code Element} node
     * @param name Attribute name
     * @param values Values to set
     */
    void setAttribute(Element element, String name, List<String> values);

    /**
     * Stores list of String values as an XML attribute
     * @param element {@code Element} node
     * @param name Attribute name
     * @param values Values to set
     * @param attributeMerger Function that manages an existing attribute value and a new one
     *                        in case when a new value is set to an existing {@code Element}
     */
    void setAttribute(Element element, String name, List<String> values, BinaryOperator<String> attributeMerger);

    /**
     * Stores property value of a specific {@code Annotation} as an XML attribute
     * @param element {@code Element} node
     * @param name Attribute name, same as annotation property name
     * @param source Annotation to look for a value in
     */
    void setAttribute(Element element, String name, Annotation source);

    /**
     * Stores property value of a specific {@code Annotation} as an XML attribute
     * @param element {@code Element} node
     * @param name Attribute name, same as annotation property name
     * @param source Annotation to look for a value in
     * @param attributeMerger Function that manages an existing attribute value and a new one
     *                        in case when a new value is set to an existing {@code Element}
     */
    void setAttribute(Element element, String name, Annotation source, BinaryOperator<String> attributeMerger);

    /**
     * Populates {@code Element} node with all eligible property values of an {@code Annotation} instance
     * @param element Element node
     * @param annotation Annotation to take property values from
     */
    void mapProperties(Element element, Annotation annotation);

    /**
     * Populates {@code Element} node with all eligible property values of an {@code Annotation} instance,
     * honoring {@link XmlScope} specified for an annotation or a particular annotation method
     * @param element Element node
     * @param annotation Annotation to take property values from
     * @param scope Current {@code XmlScope}
     */
    void mapProperties(Element element, Annotation annotation, XmlScope scope);

    /**
     * Populates {@code Element} node with property values of an {@code Annotation} instance,
     * skipping specified annotation fields
     * @param element Element node
     * @param annotation Annotation to take property values from
     * @param skippedFields List of field names to skip
     */
    void mapProperties(Element element, Annotation annotation, List<String> skippedFields);

    /**
     * Appends {@link DataSource} values to an {@code Element} node
     * @param element Element to store data in
     * @param acsListPath Path to ACS Commons List in JCR repository
     * @param acsListResourceType Use this to set {@code sling:resourceType} of data source, other than standard
     * @param dataSource Provided values as a {@code DataSource} annotation
     */
    void appendDataSource(Element element, DataSource dataSource, String acsListPath, String acsListResourceType);

    /**
     * Appends to the current {@code Element} node and returns a child {@code datasource} node
     * @param element Element to store data in
     * @param path Path to element
     * @param resourceType Use this to set {@code sling:resourceType} of data source
     * @return Appended {@code datasource} node
     */
    Element appendDataSource(Element element,  String path, String resourceType, Map<String, String> properties);

    /**
     * Appends to the current {@code Element} node and returns a child {@code datasource} node bearing link to an ACS Commons list
     * @param element Element to store data in
     * @param path Path to ACS Commons List in JCR repository
     * @param resourceType Use this to set {@code sling:resourceType} of data source, other than standard
     * @return Appended {@code datasource} node
     */
    Element appendAcsCommonsList(Element element, String path, String resourceType);

    /**
     * Appends {@link Data} values to an {@code Element} node, storing them within {@code granite:data} predefined subnode
     * @param element Element to store data in
     * @param data Provided values as an array of {@code Data} annotations
     */
    void appendDataAttributes(Element element, Data[] data);

    /**
     * Appends values to an {@code Element} node, storing them within {@code granite:data} predefined subnode
     * @param element Element to store data in
     * @param data Provided values as a {@code Map<String, String>} instance
     */
    void appendDataAttributes(Element element, Map<String, String> data);

    /**
     * Tries to append provided {@code Element} node as a child to a parent {@code Element} node.
     * Appended node must be non-empty, i.e. containing at least one attribute that is not a {@code jcr:primaryType},
     * or a child node
     * If child node with same name already exists, it is updated with attribute values of the newcoming node
     * @param parent Routine than provides Element to serve as parent
     * @param child Element to serve as child
     * @return Appended child
     */
    Element appendNonemptyChildElement(Element parent, Element child);

    /**
     * Retrieves child {@code Element} node of the specified node by its name / relative path. Same as {@link XmlUtility#getOrAddChildElement(Element, String)},
     * but if the parent's child (or any of the specified grandchildren) do not exist, null value is returned
     * @param parent Element to analyze
     * @param child  Name of child to look for, can be a simple name or a relative path e.g. {@code child/otherChild/yetAnotherChild}
     * @return Element instance if path traversing was successfull, null otherwise
     */
    Element getChildElement(Element parent, String child);

    /**
     * Retrieves child {@code Element} node of the specified node by its name / relative path.Same as {@link XmlUtility#getChildElement(Element, String)},
     * but as soon as the parent's child (or any of the specified grandchildren) not found, an empty node of {@code jcr:primaryType="nt:unstructured"}
     * is created
     * @param parent Element to analyze
     * @param child Name of child to look for, can be a simple name or a relative path e.g. {@code child/otherChild/yetAnotherChild}
     * @return Element instance
     */
    Element getOrAddChildElement(Element parent, String child);

    /**
     * Generates compliant XML tag or attribute name (optionally prepended by an XML namespace) from an arbitrary string
     * @param name Raw (unchecked) string for a tag / attribute name
     * @return Valid tag / attribute name
     */
    String getValidName(String name);

    /**
     * Generates compliant XML tag or attribute name without a namespace part from an arbitrary string
     * @param name Raw (unchecked) string for a tag / attribute name
     * @return Valid tag / attribute name
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
     * @param name Raw (unchecked) string for a tag name
     * @param defaultValue Source string for unique (indexed) name generation
     * @param context Parent node
     * @return Valid and locally unique tag name
     */
    String getUniqueName(String name, String defaultValue, Element context);
}
