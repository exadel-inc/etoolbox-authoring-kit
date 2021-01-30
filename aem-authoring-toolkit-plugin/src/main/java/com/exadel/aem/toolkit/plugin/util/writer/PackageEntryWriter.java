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

package com.exadel.aem.toolkit.plugin.util.writer;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Member;
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

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.main.CommonProperties;
import com.exadel.aem.toolkit.api.annotations.main.CommonProperty;
import com.exadel.aem.toolkit.api.annotations.meta.DialogAnnotation;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyScope;
import com.exadel.aem.toolkit.api.annotations.widgets.common.XmlScope;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.adapters.DocumentAdapter;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.source.Sources;
import com.exadel.aem.toolkit.plugin.target.Targets;
import com.exadel.aem.toolkit.plugin.util.DialogConstants;
import com.exadel.aem.toolkit.plugin.util.PluginXmlUtility;

import static com.exadel.aem.toolkit.plugin.util.writer.CqDialogWriter.getCustomDialogAnnotations;

/**
 * Base class for routines that render XML files inside a component folder within an AEM package
 */
abstract class PackageEntryWriter {
    private final Transformer transformer;

    /**
     * Basic constructor
     * @param transformer {@code Transformer} instance used to serialize XML DOM document to an output stream
     */
    PackageEntryWriter(Transformer transformer) {
        this.transformer = transformer;
    }

    /**
     * Used to store XML markup filled with annotation data taken from current {@code Class} instance
     * @param componentClass {@link Class} to analyze
     * @param componentPath {@link Path} representing a file within a file system to write data to
     */
    void writeXml(Class<?> componentClass, Path componentPath) {
        try (Writer writer = Files.newBufferedWriter(componentPath.resolve(getScope().toString()), StandardOpenOption.CREATE)) {
            if (getScope() != XmlScope.COMPONENT) {
                // markup can be stored by hand in a _cq_dialog/.content.xml structure instead of _cq_dialog.xml file
                // at first, folder-like storage must be deleted, or we might end up with two versions of component markup within same package
                Path nestedFolderPath = componentPath.resolve(StringUtils.substringBeforeLast(getScope().toString(), DialogConstants.EXTENSION_SEPARATOR));
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
    private void writeXml(Class<?> componentClass, Writer writer) {
        Document document = createDocument(componentClass);
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
    abstract XmlScope getScope();

    /**
     * Gets whether this component {@code Class} is processable by this particular {@code PackageEntryWriter} implementation
     * @param componentClass The {@code Class} under consideration
     * @return True or false
     */
    abstract boolean canProcess(Class<?> componentClass);

    /**
     * Triggers the particular routines for storing component-related data in the XML markup
     * @param componentClass The {@code Class} being processed
     * @param target The targetFacade element of DOM {@link Document} to feed data to
     */
    abstract void populateTarget(Class<?> componentClass, Target target);

    /**
     * Wraps DOM document creating with use of a {@link DocumentBuilder} and populating it with data
     * @param componentClass The {@code Class} being processed
     * @return {@link Document} created
     */
    private Document createDocument(Class<?> componentClass) {
        Target rootTarget = Targets.newInstance(DialogConstants.NN_ROOT, getScope());
        populateTarget(componentClass, rootTarget);

        Document document = rootTarget.adaptTo(DocumentAdapter.class).getDocument();
        writeCommonProperties(componentClass, getScope(), document);
        if (XmlScope.CQ_DIALOG.equals(getScope())) {
            // This assignment is for legacy dialog handlers, will not interfere with modern handlers
            PluginRuntime.context().getXmlUtility().setDocument(document);
            processLegacyDialogHandlers(document.getDocumentElement(), componentClass);
        }

        return document;
    }

    /**
     * Maps values set in {@link CommonProperties} annotation to nodes of a pre-build XML document. The nodes are picked
     * by an {@link javax.xml.xpath.XPath}
     * @param componentClass Current {@code Class} instance
     * @param scope Current {@code XmlScope}
     */
    static void writeCommonProperties(Class<?> componentClass, XmlScope scope, Document document) {
        Arrays.stream(componentClass.getAnnotationsByType(CommonProperty.class))
                .filter(p -> p.scope().equals(scope))
                .forEach(p -> writeCommonProperty(p, PluginXmlUtility.getElementNodes(p.path(), document)));
    }

    /**
     * Called by {@link PackageEntryWriter#writeCommonProperties(Class, XmlScope, Document)} for each {@link CommonProperty}
     * instance
     * @param property {@code CommonProperty} instance
     * @param targets Target {@code Node}s selected via an XPath
     */
    private static void writeCommonProperty(CommonProperty property, List<Element> targets) {
        targets.forEach(target -> target.setAttribute(property.name(), property.value()));
    }

    private static void processLegacyDialogHandlers(Element element, Class<?> cls) {
        List<DialogAnnotation> customAnnotations = getCustomDialogAnnotations(cls);
        PluginRuntime.context().getReflectionUtility().getCustomDialogHandlers().stream()
            .filter(handler -> customAnnotations.stream()
                .anyMatch(annotation -> StringUtils.equals(annotation.source(), handler.getName())))
            .forEach(handler -> handler.accept(element, cls));
    }

    static boolean fitsInScope(Member member, XmlScope scope) {
        List<XmlScope> activeScopes = Sources.fromMember(member)
            .tryAdaptTo(PropertyScope.class)
            .map(PropertyScope::value)
            .map(Arrays::asList)
            .orElse(EnumUtils.getEnumList(XmlScope.class));

        return activeScopes.contains(scope);
    }
}
