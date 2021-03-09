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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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
import com.exadel.aem.toolkit.api.annotations.meta.DialogAnnotation;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.Scope;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.adapters.DomAdapter;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.runtime.PluginXmlUtility;
import com.exadel.aem.toolkit.plugin.source.Sources;
import com.exadel.aem.toolkit.plugin.target.Targets;
import com.exadel.aem.toolkit.plugin.util.DialogConstants;


/**
 * Base class for routines that render XML files inside a component folder within an AEM package
 */
abstract class PackageEntryWriter {


    /* -----------------------------
       Class fields and constructors
       ----------------------------- */

    private final Transformer transformer;

    /**
     * Basic constructor
     * @param transformer {@code Transformer} instance used to serialize XML DOM document to an output stream
     */
    PackageEntryWriter(Transformer transformer) {
        this.transformer = transformer;
    }


    /* ----------------
       Instance members
       ---------------- */

    /**
     * Gets {@link Scope} associated with this {@code PackageEntryWriter} instance
     * @return One of {@code XmlScope} values
     */
    abstract Scope getScope();

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
     * Called by {@link PackageWriter#write(Class)} before storing new XML entities into the component's folder
     * to remove redundant and obsolete XML entries
     * @param componentPath {@link Path} representing a file within a file system the data is written to
     */
    final void cleanUp(Path componentPath) {
        try {
            Path existingFilePath = componentPath.resolve(getScope().toString());
            Files.deleteIfExists(existingFilePath);
            if (!getScope().equals(Scope.COMPONENT)) {
                // We take into account that the markup could be stored by hand in e.g. _cq_dialog/.content.xml structure
                // instead of _cq_dialog.xml file. Therefore, both the "file" and the "folder" must be deleted,
                // or we might end up with two versions of component markup within same package
                Path nestedFolderPath = componentPath.resolve(StringUtils.substringBeforeLast(
                    getScope().toString(),
                    DialogConstants.EXTENSION_SEPARATOR));
                Path nestedFilePath = nestedFolderPath.resolve(Scope.COMPONENT.toString()); // we just use the '.content.xml' value here as a constant
                Files.deleteIfExists(nestedFilePath);
                Files.deleteIfExists(nestedFolderPath);
            }
        } catch (IOException e) {
            PluginRuntime.context().getExceptionHandler().handle(e);
        }
    }

    /**
     * Used to store XML markup filled with annotation data taken from current {@code Class} instance
     * @param componentClass {@link Class} to analyze
     * @param componentPath {@link Path} representing a file within a file system the data is written to
     */
    final void writeXml(Class<?> componentClass, Path componentPath) {
        try (Writer writer = Files.newBufferedWriter(componentPath.resolve(getScope().toString()), StandardOpenOption.CREATE)) {
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
     * Wraps DOM document creating with use of a {@link DocumentBuilder} and populating it with data
     * @param componentClass The {@code Class} being processed
     * @return {@link Document} created
     */
    private Document createDocument(Class<?> componentClass) {
        Target rootTarget = Targets.newInstance(DialogConstants.NN_ROOT, getScope());
        populateTarget(componentClass, rootTarget);

        Document result = rootTarget
            .adaptTo(DomAdapter.class)
            .composeDocument(PluginRuntime.context().getXmlUtility().resetDocument());
        writeCommonProperties(componentClass, getScope(), result);
        if (Scope.CQ_DIALOG.equals(getScope())) {
            processLegacyDialogHandlers(result.getDocumentElement(), componentClass);
        }

        return result;
    }


    /* ------------------------
       Static (utility) methods
       ------------------------ */

    /**
     * Gets whether the provided {@code Member} matches the provided scope. This is used to decide if a particular
     * property should be rendered by the current {@code PackageEntryWriter}
     * @param member Non-null {@code Member} instance
     * @param scope {@code Scope} for the current {@code PackageEntryWriter}
     * @return True or false
     */
    static boolean fitsInScope(Member member, Scope scope) {
        List<Scope> activeScopes = Sources.fromMember(member)
            .tryAdaptTo(PropertyRendering.class)
            .map(PropertyRendering::scope)
            .map(Arrays::asList)
            .orElse(Collections.singletonList(Scope.ALL));

        return activeScopes.contains(scope) || activeScopes.contains(Scope.ALL);
    }

    /**
     * Maps the values set in {@link CommonProperties} annotation to nodes of a pre-built XML document. The nodes are
     * picked by an {@link javax.xml.xpath.XPath}
     * @param componentClass Current {@code Class} instance
     * @param scope Current {@code XmlScope}
     */
    private static void writeCommonProperties(Class<?> componentClass, Scope scope, Document document) {
        Arrays.stream(componentClass.getAnnotationsByType(CommonProperty.class))
                .filter(p -> scope.equals(p.scope()))
                .forEach(p -> writeCommonProperty(p, PluginXmlUtility.getElementNodes(p.path(), document)));
    }

    /**
     * Called by {@link PackageEntryWriter#writeCommonProperties(Class, Scope, Document)} for each {@link CommonProperty}
     * instance
     * @param property {@code CommonProperty} instance
     * @param targets Target {@code Node}s selected via an XPath
     */
    private static void writeCommonProperty(CommonProperty property, List<Element> targets) {
        targets.forEach(target -> target.setAttribute(property.name(), property.value()));
    }

    /**
     * Called by {@link PackageEntryWriter#createDocument(Class)} to find and activate legacy handlers (those consuming
     * the pair of {@code Element} and {@code Class<?>} references) that operate class-wide
     * @param element DOM {@code Element} object
     * @param annotatedClass The {@code Class<?>} that a legacy handler processes
     */
    private static void processLegacyDialogHandlers(Element element, Class<?> annotatedClass) {
        List<DialogAnnotation> customAnnotations = getCustomDialogAnnotations(annotatedClass);
        PluginRuntime.context().getReflection().getCustomDialogHandlers().stream()
            .filter(handler -> customAnnotations.stream()
                .anyMatch(annotation -> StringUtils.equals(annotation.source(), handler.getName())))
            .forEach(handler -> handler.accept(element, annotatedClass));
    }

    /**
     * Retrieves list of {@link DialogAnnotation} instances defined for the current {@code Class}
     *
     * @param componentClass The {@code Class} being processed
     * @return List of values, empty or non-empty
     */
    private static List<DialogAnnotation> getCustomDialogAnnotations(Class<?> componentClass) {
        return Arrays.stream(componentClass.getDeclaredAnnotations())
            .filter(annotation -> annotation.annotationType().getDeclaredAnnotation(DialogAnnotation.class) != null)
            .map(annotation -> annotation.annotationType().getDeclaredAnnotation(DialogAnnotation.class))
            .collect(Collectors.toList());
    }
}
