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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
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
import com.exadel.aem.toolkit.api.annotations.meta.MapProperties;
import com.exadel.aem.toolkit.api.annotations.meta.Scope;
import com.exadel.aem.toolkit.api.handlers.DialogHandler;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.adapters.DomAdapter;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.runtime.XmlRuntime;
import com.exadel.aem.toolkit.plugin.source.Sources;
import com.exadel.aem.toolkit.plugin.target.Targets;
import com.exadel.aem.toolkit.plugin.util.AnnotationUtil;
import com.exadel.aem.toolkit.plugin.util.DialogConstants;
import com.exadel.aem.toolkit.plugin.util.ScopeUtil;

/**
 * Base class for routines that render XML files inside a component folder within an AEM package
 */
abstract class PackageEntryWriter {
    private static final String API_PACKAGE_NAME = "com.exadel.aem.toolkit.api";


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


    /* -----------------------
       Common instance members
       ----------------------- */

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


    /* ----------------------------
       File system writing routines
       ---------------------------- */

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
     * Creates a DOM document that reflects the data that is provided by the component class and is relevant to the
     * scope of the current writer
     * @param componentClass The {@code Class} being processed
     * @return {@link Document} created
     */
    private Document createDocument(Class<?> componentClass) {
        Target rootTarget = Targets.newInstance(DialogConstants.NN_ROOT, getScope());
        applyAutoMappedProperties(componentClass, rootTarget);
        applySpecificProperties(componentClass, rootTarget);
        applyHandlers(componentClass, rootTarget);

        Document result = rootTarget
            .adaptTo(DomAdapter.class)
            .composeDocument(PluginRuntime.context().newXmlUtility().getDocument());
        writeCommonProperties(componentClass, result);

        if (Scope.CQ_DIALOG.equals(getScope())) {
            applyLegacyDialogHandlers(result.getDocumentElement(), componentClass);
        }

        return result;
    }


    /* -----------------
       Populating target
       ----------------- */

    /**
     * Triggers routines for storing component-related data in the provided {@link Target}
     * @param componentClass The {@code Class} being processed
     * @param target The {@code Target} to feed data to
     */
    abstract void applySpecificProperties(Class<?> componentClass, Target target);

    /**
     * Processes Toolkit annotations appended to the given {@code Class} (other than those present in out-of-box API module)
     * that have a {@link MapProperties} meta-annotation. Their mappable properties are stored in the given {@link Target}
     * @param componentClass The {@code Class} being processed
     * @param target The {@code Target} to feed data to
     */
    private void applyAutoMappedProperties(Class<?> componentClass, Target target) {
        Arrays.stream(componentClass.getDeclaredAnnotations())
            .filter(annotation -> !annotation.annotationType().getPackage().getName().startsWith(API_PACKAGE_NAME))
            .filter(annotation -> ScopeUtil.fits(getScope(), annotation, componentClass.getDeclaredAnnotations()))
            .forEach(annotation -> target.attributes(annotation, AnnotationUtil.getPropertyMappingFilter(annotation)));
    }

    /**
     * Processes Toolkit handlers associated with out-of-box or custom annotations appended to the given {@code Class}
     * @param componentClass The {@code Class} being processed
     * @param target The {@code Target} to feed data to
     */
    private void applyHandlers(Class<?> componentClass, Target target) {
        PluginRuntime
            .context()
            .getReflection()
            .getHandlers(getScope(), componentClass.getDeclaredAnnotations())
            .forEach(handler -> handler.accept(Sources.fromClass(componentClass), target));
    }

    /**
     * Maps the values set in {@link CommonProperties} annotation to nodes of a DOM document being built. The nodes are
     * picked by an {@link javax.xml.xpath.XPath}
     * @param componentClass Current {@code Class} instance
     */
    private void writeCommonProperties(Class<?> componentClass, Document document) {
        Arrays.stream(componentClass.getAnnotationsByType(CommonProperty.class))
            .filter(p -> getScope().equals(p.scope()))
            .forEach(p -> writeCommonProperty(p, XmlRuntime.getElementNodes(p.path(), document)));
    }

    /**
     * Called by {@link PackageEntryWriter#writeCommonProperties(Class, Document)} for each {@link CommonProperty}
     * instance
     * @param property {@code CommonProperty} instance
     * @param targets Target {@code Node}s selected via an XPath
     */
    private static void writeCommonProperty(CommonProperty property, List<Element> targets) {
        targets.forEach(target -> target.setAttribute(property.name(), property.value()));
    }


    /* --------------------------
       Legacy populating routines
       -------------------------- */

    /**
     * Called by {@link PackageEntryWriter#createDocument(Class)} to find and activate legacy handlers (those consuming
     * the pair of {@code Element} and {@code Class<?>} references) that operate class-wide
     * @param element DOM {@code Element} object
     * @param annotatedClass The {@code Class<?>} that a legacy handler processes
     */
    @SuppressWarnings("deprecation") // DialogHandler reference and Handler#accept(Element, Class) method are retained
                                     // for compatibility and will be removed in a version after 2.0.1
    private static void applyLegacyDialogHandlers(Element element, Class<?> annotatedClass) {
        List<DialogAnnotation> customAnnotations = getLegacyDialogAnnotations(annotatedClass);
        PluginRuntime.context().getReflection().getHandlers().stream()
            .filter(handler -> handler instanceof DialogHandler)
            .filter(handler -> customAnnotations.stream()
                .anyMatch(annotation -> StringUtils.equals(annotation.source(), ((DialogHandler) handler).getName())))
            .forEach(handler -> ((DialogHandler) handler).accept(element, annotatedClass));
    }

    /**
     * Retrieves list of {@link DialogAnnotation} instances defined for the current {@code Class}
     * @param componentClass The {@code Class} being processed
     * @return List of values, empty or non-empty
     */
    private static List<DialogAnnotation> getLegacyDialogAnnotations(Class<?> componentClass) {
        return Arrays.stream(componentClass.getDeclaredAnnotations())
            .filter(annotation -> annotation.annotationType().getDeclaredAnnotation(DialogAnnotation.class) != null)
            .map(annotation -> annotation.annotationType().getDeclaredAnnotation(DialogAnnotation.class))
            .collect(Collectors.toList());
    }
}
