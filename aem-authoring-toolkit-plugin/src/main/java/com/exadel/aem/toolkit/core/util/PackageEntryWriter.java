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

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.main.CommonProperties;
import com.exadel.aem.toolkit.api.annotations.main.CommonProperty;
import com.exadel.aem.toolkit.api.annotations.widgets.common.XmlScope;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;

/**
 * Base class for creating XML representation of AEM component's stored attributes and authoring features
 */
abstract class PackageEntryWriter {
    private DocumentBuilder documentBuilder;
    private Transformer transformer;

    PackageEntryWriter(DocumentBuilder documentBuilder, Transformer transformer) {
        this.documentBuilder = documentBuilder;
        this.transformer = transformer;
    }

    /**
     * Used to store XML markup filled with annotation data taken from current {@code Class} instance
     * @param componentClass {@link Class} to analyze
     * @param componentPath {@link Path} representing a file within a file system to write data to
     */
    void writeXml(Class<?> componentClass, Path componentPath) {
        if (!isProcessed(componentClass)) {
            return;
        }
        try (Writer writer = Files.newBufferedWriter(componentPath.resolve(getXmlScope().toString()), StandardOpenOption.CREATE)) {
            if (getXmlScope() != XmlScope.COMPONENT) {
                // markup can be stored by hand in a _cq_dialog/.content.xml structure instead of _cq_dialog.xml file
                // at first, folder-like storage must be deleted, or we might end up with two versions of component markup within same package
                Path nestedFolderPath = componentPath.resolve(StringUtils.substringBeforeLast(getXmlScope().toString(), DialogConstants.EXTENSION_SEPARATOR));
                Path nestedFilePath = nestedFolderPath.resolve(XmlScope.COMPONENT.toString());
                Files.deleteIfExists(nestedFilePath);
                Files.deleteIfExists(nestedFolderPath);
            }
            // then second we store the newly generated class
            writeXml(componentClass, writer);
        } catch (IOException e) {
            PluginRuntime.context().getExceptionHandler().handle(e);
        }
    }

    /**
     * Used to store XML markup filled with annotation data taken from current {@code Class} instance
     * @param componentClass {@link Class} to analyze
     * @param writer {@link Writer} managing the data storage procedure
     */
    void writeXml(Class<?> componentClass, Writer writer) {
        Document document = createDomDocument(componentClass);
        try {
             transformer.transform(new DOMSource(document), new StreamResult(writer));
        } catch (TransformerException e) {
            PluginRuntime.context().getExceptionHandler().handle(e);
        }
    }

    /**
     * Gets {@link XmlScope} associated with this {@code PackageEntryWriter} instance
     * @return One of {@code XmlScope} values
     */
    abstract XmlScope getXmlScope();

    /**
     * Gets whether this component {@code Class} is processable by this particular {@code PackageEntryWriter} implementation
     * @param componentClass The {@code Class} under consideration
     * @return True or false
     */
    abstract boolean isProcessed(Class<?> componentClass);

    /**
     * Triggers the particular routines for storing component-related data in the XML markup
     * @param componentClass The {@code Class} being processed
     * @param root The root element of DOM {@link Document} to feed data to
     */
    abstract void populateDomDocument(Class<?> componentClass, Element root);

    /**
     * Wraps DOM document creating with use of a {@link DocumentBuilder} and populating it with data
     * @param componentClass The {@code Class} being processed
     * @return {@link Document} created
     */
    private Document createDomDocument(Class<?> componentClass) {
        Element rootElement = PluginRuntime.context().getXmlUtility().newDocumentRoot(this.documentBuilder, componentClass);
        populateDomDocument(componentClass, rootElement);
        return PluginRuntime.context().getXmlUtility().getCurrentDocument();
    }

    /**
     * Maps values set in {@link CommonProperties} annotation to nodes of a pre-build XML document. The nodes are picked
     * by an {@link javax.xml.xpath.XPath}
     * @param componentClass Current {@code Class} instance
     * @param scope Current {@code XmlScope}
     */
    static void writeCommonProperties(Class<?> componentClass, XmlScope scope) {
        Arrays.stream(componentClass.getAnnotationsByType(CommonProperty.class))
                .filter(p -> p.scope().equals(scope))
                .forEach(p -> writeCommonProperty(p, PluginRuntime.context().getXmlUtility().getElementNodes(p.path())));
    }

    /**
     * Called by {@link PackageEntryWriter#writeCommonProperties(Class, XmlScope)} for each {@link CommonProperty}
     * instance
     * @param property {@code CommonProperty} instance
     * @param targets Target {@code Node}s selected via an XPath
     */
    private static void writeCommonProperty(CommonProperty property, List<Element> targets) {
        targets.forEach(target -> PluginRuntime.context().getXmlUtility().setAttribute(target, property.name(), property.value()));
    }
}
