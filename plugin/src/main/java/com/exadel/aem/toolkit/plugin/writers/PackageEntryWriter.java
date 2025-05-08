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
import java.util.function.BiConsumer;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.exadel.aem.toolkit.api.annotations.meta.Scopes;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.adapters.DomAdapter;
import com.exadel.aem.toolkit.plugin.handlers.Handlers;
import com.exadel.aem.toolkit.plugin.handlers.common.DomHandler;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.sources.ComponentSource;
import com.exadel.aem.toolkit.plugin.targets.Targets;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;
import com.exadel.aem.toolkit.plugin.utils.XmlFactory;
import com.exadel.aem.toolkit.plugin.utils.XmlMergeHelper;

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
     * Gets whether this {@link Source} representing a Java class is processable by this particular {@code
     * PackageEntryWriter} implementation
     * @param source The {@code Source} that refers to a class under consideration
     * @return True or false
     */
    abstract boolean canProcess(Source source);

    /**
     * Retrieves the {@link com.exadel.aem.toolkit.api.handlers.Handler} or handler chain associated with the current
     * instance. The returned value is used for populating the {@code Target} artifact created by this package entry
     * writer with data
     * @return {@code BiConsumer} object representing the handler or handler chain
     */
    BiConsumer<Source, Target> getHandlers() {
        return Handlers.forScope(getScope());
    }

    /**
     * Retrieves the {@link Transformer} associated with this instance
     * @return {@code Transformer} object
     */
    Transformer getTransformer() {
        return transformer;
    }

    /* ----------------------------
       File system writing routines
       ---------------------------- */

    /**
     * Called by {@link PackageWriter#write(ComponentSource)} before storing new XML entities into the component's
     * folder to remove redundant and obsolete XML entries
     * @param componentPath {@link Path} representing a folder within a file system the data is written to
     */
    final void cleanUp(Path componentPath) {
        Path existingFilePath = componentPath.resolve(getScope());
        try {
            Files.deleteIfExists(existingFilePath);
            if (!Scopes.COMPONENT.equals(getScope())) {
                // Since the markup could be stored by hand in e.g. _cq_dialog/.content.xml instead of _cq_dialog.xml
                // file, both the "file" and "folder" must be deleted, or we might end up with two versions of component
                // markup within the same package
                String nestedFolder = StringUtils.substringBeforeLast(getScope(), DialogConstants.SEPARATOR_DOT);
                Path nestedFolderPath = componentPath.resolve(nestedFolder);
                Path nestedFilePath = nestedFolderPath.resolve(Scopes.COMPONENT);
                Files.deleteIfExists(nestedFilePath);
                Files.deleteIfExists(nestedFolderPath);
            }
        } catch (IOException e) {
            PluginRuntime.context().getExceptionHandler().handle(e);
        }
    }

    /**
     * Called by {@link PackageWriter#write(ComponentSource)} to open an existing XML file within the component's folder
     * for merging with the new XML markup
     * @param componentPath {@link Path} representing a folder within a file system the data is written to
     * @return {@link Document} representing the existing XML file, or null if the file does not exist or cannot be
     * parsed
     */
    final Document openXml(Path componentPath) {
        Path existingFilePath = componentPath.resolve(getScope());
        try {
            if (Files.exists(existingFilePath)) {
                return XmlFactory.newDocument(Files.readAllBytes(existingFilePath));
            }
            // Since the markup could be stored by hand in e.g. _cq_dialog/.content.xml instead of _cq_dialog.xml,
            // we need to check for the "folder" version of the file as well
            if (Scopes.COMPONENT.equals(getScope())) {
                return null;
            }
            String nestedFolder = StringUtils.substringBeforeLast(getScope(), DialogConstants.SEPARATOR_DOT);
            Path nestedFolderPath = componentPath.resolve(nestedFolder);
            Path nestedFilePath = nestedFolderPath.resolve(Scopes.COMPONENT);
            if (Files.exists(nestedFilePath)) {
                return XmlFactory.newDocument(Files.readAllBytes(existingFilePath));
            }
        } catch (IOException | ParserConfigurationException | SAXException e) {
            PluginRuntime.context().getExceptionHandler().handle(e);
        }
        return null;
    }

    /**
     * Used to store XML markup filled with annotation data taken from the current {@code Source} instance
     * @param source {@link Source} instance that delivers the rendering data
     * @param path   {@link Path} representing a file system entry the data is written to
     * @param patch Optional {@link Document} to be merged into the resulting XML markup
     */
    final void writeXml(Source source, Path path, Document patch) {
        try (Writer writer = Files.newBufferedWriter(path.resolve(getScope()), StandardOpenOption.CREATE)) {
            writeXml(source, writer, patch);
        } catch (IOException e) {
            PluginRuntime.context().getExceptionHandler().handle(e);
        }
    }

    /**
     * Used to store XML markup filled with annotation data taken from current {@code Source} instance
     * @param source {@link Source} instance that delivers the rendering data
     * @param writer {@link Writer} managing the data storage procedure
     * @param patch Optional {@link Document} to be merged into the resulting XML markup
     */
    private void writeXml(Source source, Writer writer, Document patch) {
        Document document = createDocument(source);
        XmlMergeHelper.merge(document, patch);
        try {
            transformer.transform(new DOMSource(document), new StreamResult(writer));
        } catch (TransformerException e) {
            PluginRuntime.context().getExceptionHandler().handle(e);
        }
    }

    /**
     * Creates a DOM document that reflects the data that is provided by the component class and is relevant to the
     * scope of the current writer
     * @param source {@link Source} instance that delivers the rendering data
     * @return {@link Document} created
     */
    private Document createDocument(Source source) {
        Target target = Targets.newRoot(getScope());
        getHandlers().accept(source, target);

        Document result = target
            .adaptTo(DomAdapter.class)
            .composeDocument(PluginRuntime.context().newXmlUtility().getDocument());
        DOM_HANDLER.accept(source, result, getScope());
        return result;
    }
}
