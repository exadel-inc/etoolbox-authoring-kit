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
package com.exadel.aem.toolkit.plugin.writers;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;

import com.exadel.aem.toolkit.api.annotations.meta.Scopes;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.adapters.DomAdapter;
import com.exadel.aem.toolkit.plugin.handlers.HandlerChains;
import com.exadel.aem.toolkit.plugin.handlers.common.DomHandler;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.sources.Sources;
import com.exadel.aem.toolkit.plugin.targets.Targets;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;

/**
 * Base class for routines that render XML files inside a component folder within an AEM package
 */
abstract class PackageEntryWriter {
    private static final DomHandler DOM_HANDLER = new DomHandler();

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
     * Gets the scope associated with this {@code PackageEntryWriter} instance
     * @return String value representing a valid scope
     * @see com.exadel.aem.toolkit.api.annotations.meta.Scopes
     */
    abstract String getScope();

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
            Path existingFilePath = componentPath.resolve(getScope());
            Files.deleteIfExists(existingFilePath);
            if (!Scopes.COMPONENT.equals(getScope())) {
                // We take into account that the markup could be stored by hand in e.g. _cq_dialog/.content.xml structure
                // instead of _cq_dialog.xml file. Therefore, both the "file" and "folder" must be deleted,
                // or we might end up with two versions of component markup within same package
                Path nestedFolderPath = componentPath.resolve(StringUtils.substringBeforeLast(
                    getScope(),
                    DialogConstants.SEPARATOR_DOT));
                Path nestedFilePath = nestedFolderPath.resolve(Scopes.COMPONENT);
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
     * @param componentPath  {@link Path} representing a file within a file system the data is written to
     */
    final void writeXml(Class<?> componentClass, Path componentPath) {
        try (Writer writer = Files.newBufferedWriter(componentPath.resolve(getScope()), StandardOpenOption.CREATE)) {
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
        Source source = Sources.fromClass(componentClass);
        Target target = Targets.newInstance(DialogConstants.NN_ROOT, getScope());
        HandlerChains.forScope(getScope()).accept(source, target);

        Document result = target
            .adaptTo(DomAdapter.class)
            .composeDocument(PluginRuntime.context().newXmlUtility().getDocument());
        DOM_HANDLER.accept(source, result, getScope());
        return result;
    }
}
