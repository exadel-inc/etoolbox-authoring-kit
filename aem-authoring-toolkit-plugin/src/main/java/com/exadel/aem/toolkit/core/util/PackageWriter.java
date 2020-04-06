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
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.project.MavenProject;
import com.google.common.collect.ImmutableMap;

import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.core.exceptions.PluginException;
import com.exadel.aem.toolkit.core.exceptions.UnknownComponentException;
import com.exadel.aem.toolkit.core.exceptions.ValidationException;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;

/**
 * Implements actions needed to store collected/processed data into AEM package, optimal for use in "try-with-resources" block
 */
public class PackageWriter implements AutoCloseable {
    private static final String PACKAGE_EXTENSION = ".zip";
    private static final String FILESYSTEM_PREFIX = "jar:";
    private static final Map<String, String> FILESYSTEM_OPTIONS = Collections.singletonMap("create", "true");

    /**
     * Security features as per XML External entity protection cheat sheet
     * @see <a href="https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html">here</a>
     */
    private static final Map<String, Boolean> DOCUMENT_BUILDER_FACTORY_SECURITY_FEATURES = ImmutableMap.of(
            "http://apache.org/xml/features/disallow-doctype-decl", true,
            "http://xml.org/sax/features/external-general-entities", false,
            "http://xml.org/sax/features/external-parameter-entities", false,
            "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

    private static final String INVALID_PROJECT_EXCEPTION_MESSAGE = "Invalid project";
    private static final String COMPONENT_PATH_MISSING_EXCEPTION_MESSAGE = "Component path missing for project ";
    private static final String COMPONENT_NAME_MISSING_EXCEPTION_MESSAGE = "Component name missing in @Dialog annotation for class ";
    private static final String CANNOT_WRITE_TO_PACKAGE_EXCEPTION_MESSAGE = "Cannot write to package ";

    private String componentsBasePath;
    private FileSystem fileSystem;
    private List<PackageEntryWriter> writers;

    private PackageWriter(FileSystem fileSystem, String componentsBasePath, List<PackageEntryWriter> writers) {
        this.fileSystem = fileSystem;
        this.componentsBasePath = componentsBasePath;
        this.writers = writers;
    }

    @Override
    public void close() {
        try {
            fileSystem.close();
        } catch (IOException e) {
            throw new PluginException(CANNOT_WRITE_TO_PACKAGE_EXCEPTION_MESSAGE, e);
        }
    }

    /**
     * Encapsulates steps taken to store authoring features on an AEM component into package. For this, several particular
     * package entry writers, vid. for populating {@code .content.xml}, {@code _cq_dialog.xml}, and {@code _cq_editConfig.xml},
     * are invoked in sequence
     * @param componentClass Current {@code Class} instance
     */
    public void write(Class<?> componentClass) {
        Dialog dialog = componentClass.getDeclaredAnnotation(Dialog.class);
        if (StringUtils.isBlank(dialog.name())) {
            ValidationException validationException = new ValidationException(COMPONENT_NAME_MISSING_EXCEPTION_MESSAGE + componentClass.getSimpleName());
            PluginRuntime.context().getExceptionHandler().handle(validationException);
            return;
        }
        Path componentPath = fileSystem.getPath(componentsBasePath, dialog.name());
        if (!Files.isWritable(componentPath)) {
            PluginRuntime.context().getExceptionHandler().handle(new UnknownComponentException(componentPath));
            return;
        }
        writers.forEach(writer -> writer.writeXml(componentClass, componentPath));
    }

    /**
     * Initializes an instance of {@link PackageWriter} profiled for the current {@link MavenProject} and the tree of
     * folders storing AEM components' data
     * @param project {@code MavenProject instance}
     * @param componentsBasePath Path to the sub-folder within package under which AEM component folders are situated
     * @return {@code PackageWriter} instance
     */
    public static PackageWriter forMavenProject(MavenProject project, String componentsBasePath) {
        if (StringUtils.isBlank(componentsBasePath)) {
            throw new PluginException(COMPONENT_PATH_MISSING_EXCEPTION_MESSAGE + project.getBuild().getFinalName());
        }
        if (project == null) {
            throw new PluginException(INVALID_PROJECT_EXCEPTION_MESSAGE);
        }

        String packageFileName = project.getBuild().getFinalName() + PACKAGE_EXTENSION;
        Path path = Paths.get(project.getBuild().getDirectory()).resolve(packageFileName);
        URI uri = URI.create(FILESYSTEM_PREFIX + path.toUri());
        try {
            FileSystem fs = FileSystems.newFileSystem(uri, FILESYSTEM_OPTIONS);
            return forFileSystem(project.getBuild().getFinalName(), fs, componentsBasePath);
        } catch (IOException e) {
            // exception caught here are critical for the execution, so no further handling
            throw new PluginException(CANNOT_WRITE_TO_PACKAGE_EXCEPTION_MESSAGE + project.getBuild().getFinalName(), e);
        }
    }

    /**
     * Initializes an instance of {@link PackageWriter} profiled for the particular {@link FileSystem} representing
     * the structure of the package
     * @param projectName Name of the project this file system contains information for
     * @param fileSystem Current {@link FileSystem} instance
     * @param componentsBasePath Path to the sub-folder within package under which AEM component folders are situated
     * @return {@code PackageWriter} instance
     */
    private static PackageWriter forFileSystem(String projectName, FileSystem fileSystem, String componentsBasePath) {
        List<PackageEntryWriter> writers;
        try {
            DocumentBuilder documentBuilder = createDocumentBuilder();
            Transformer transformer = createTransformer();
            writers = Arrays.asList(
                    new ContentXmlWriter(documentBuilder, transformer),
                    new CqEditConfigWriter(documentBuilder, transformer),
                    new CqDialogWriter(documentBuilder, transformer),
                    new CqHtmlTagWriter(documentBuilder, transformer)
                    new CqChildEditConfigWriter(documentBuilder, transformer)
            );
        } catch (ParserConfigurationException | TransformerConfigurationException e) {
            // exceptions caught here are due to possible XXE security vulnerabilities, so no further handling
            throw new PluginException(CANNOT_WRITE_TO_PACKAGE_EXCEPTION_MESSAGE + projectName, e);
        }
        return new PackageWriter(fileSystem, componentsBasePath, writers);
    }

    /**
     * Creates an XML {@code DocumentBuilder} with specific XML security features set for this writer to generate DOM  trees for data storage
     * @return {@link DocumentBuilder} instance
     * @throws ParserConfigurationException in case security feature cannot be set
     */
    static DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        for(Map.Entry<String, Boolean> feature : DOCUMENT_BUILDER_FACTORY_SECURITY_FEATURES.entrySet()) {
            dbf.setFeature(feature.getKey(), feature.getValue());
        }
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);
        return dbf.newDocumentBuilder();
    }

    /**
     * Creates an XML {@code DocumentBuilder} with specific XML security attributes set for this writer to output ready XML
     * structures
     * @return {@link Transformer} instance
     * @throws TransformerConfigurationException in case security attributes cannot be set
     */
    static Transformer createTransformer() throws TransformerConfigurationException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        return transformerFactory.newTransformer();
    }
}
