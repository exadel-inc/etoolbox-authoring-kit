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
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.project.MavenProject;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;

import com.exadel.aem.toolkit.api.annotations.main.Component;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.common.Scope;
import com.exadel.aem.toolkit.plugin.exceptions.InvalidSettingException;
import com.exadel.aem.toolkit.plugin.exceptions.PluginException;
import com.exadel.aem.toolkit.plugin.exceptions.UnknownComponentException;
import com.exadel.aem.toolkit.plugin.exceptions.ValidationException;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.util.XmlFactory;

/**
 * Implements actions needed to store collected/processed data into AEM package, optimal for use in "try-with-resources" block
 */
public class PackageWriter implements AutoCloseable {

    private static final String PACKAGE_EXTENSION = ".zip";
    private static final String FILESYSTEM_PREFIX = "jar:";
    private static final Map<String, String> FILESYSTEM_OPTIONS = ImmutableMap.of("create", "true");

    private static final String CANNOT_WRITE_TO_PACKAGE_EXCEPTION_MESSAGE = "Cannot write to package ";
    private static final String COMPONENT_DATA_MISSING_EXCEPTION_MESSAGE = "No data to build .content.xml file wile processing component ";
    private static final String COMPONENT_NAME_MISSING_EXCEPTION_MESSAGE = "Component name missing in class ";
    private static final String COMPONENT_PATH_MISSING_EXCEPTION_MESSAGE = "Component path missing for project ";
    private static final String INVALID_PROJECT_EXCEPTION_MESSAGE = "Invalid project";
    private static final String MULTIPLE_MODULES_EXCEPTION_MESSAGE = "Multiple modules available for %s while processing component %s";
    private static final String UNRECOGNIZED_MODULE_EXCEPTION_MESSAGE = "Unrecognized component module %s while processing component %s";

    private final String componentsBasePath;
    private final FileSystem fileSystem;
    private final List<PackageEntryWriter> writers;

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
     * Stores AEM component's authoring markup into a package. For this, several package entry writers,
     * e.g. for populating {@code .content.xml}, {@code _cq_dialog.xml}, {@code _cq_editConfig.xml}, etc.
     * are called in sequence. If the component is split into several "modules" (views), each is processed separately
     * @param componentClass Current {@code Class} instance
     */
    public void write(Class<?> componentClass) {
        String relativeComponentPath = getComponentPath(componentClass);
        if (StringUtils.isBlank(relativeComponentPath)) {
            ValidationException validationException = new ValidationException(COMPONENT_NAME_MISSING_EXCEPTION_MESSAGE + componentClass.getSimpleName());
            PluginRuntime.context().getExceptionHandler().handle(validationException);
            return;
        }
        Path componentPath = fileSystem.getPath(componentsBasePath, relativeComponentPath);
        if (!Files.isWritable(componentPath)) {
            PluginRuntime.context().getExceptionHandler().handle(new UnknownComponentException(componentPath));
            return;
        }

        Map<PackageEntryWriter, Class<?>> viewsByWriter = getComponentViews(componentClass);

        if (viewsByWriter.keySet().stream().noneMatch(writer -> writer.getScope() == Scope.COMPONENT)) {
            InvalidSettingException ex = new InvalidSettingException(
                COMPONENT_DATA_MISSING_EXCEPTION_MESSAGE + componentClass.getName());
            PluginRuntime.context().getExceptionHandler().handle(ex);
        }

        viewsByWriter.forEach((writer, view) -> writer.writeXml(view, componentPath));
    }

    /**
     * Collects a registry of views available for the given AEM component and matches each view to an appropriate
     * {@link PackageEntryWriter}
     * @param componentClass Current {@code Class} instance
     * @return {@code Map} that exposes {@code PackageEntryWriter} instances as keys and the matched component views
     * as values
     */
    private Map<PackageEntryWriter, Class<?>> getComponentViews(Class<?> componentClass) {
        Class<?>[] referencedViews = Optional
            .ofNullable(componentClass.getAnnotation(Component.class))
            .map(Component::views)
            .orElse(ArrayUtils.EMPTY_CLASS_ARRAY);

        List<Class<?>> allViews = Streams
            .concat(Stream.of(componentClass), Arrays.stream(referencedViews))
            .distinct()
            .collect(Collectors.toList());

        Map<PackageEntryWriter, Class<?>> result = new HashMap<>();
        for (Class<?> view: allViews) {
            List<PackageEntryWriter> matchedWriters = writers
                .stream()
                .filter(w -> w.canProcess(view))
                .collect(Collectors.toList());

            if (matchedWriters.isEmpty()) {
                InvalidSettingException ex = new InvalidSettingException(String.format(
                    UNRECOGNIZED_MODULE_EXCEPTION_MESSAGE,
                    view.getSimpleName(),
                    componentClass.getName()
                ));
                PluginRuntime.context().getExceptionHandler().handle(ex);
            }

            for (PackageEntryWriter matchedWriter : matchedWriters) {
                if (result.containsKey(matchedWriter) && matchedWriter.getScope() != Scope.COMPONENT) {
                    InvalidSettingException ex = new InvalidSettingException(String.format(
                        MULTIPLE_MODULES_EXCEPTION_MESSAGE,
                        matchedWriter.getScope(),
                        componentClass.getName()
                    ));
                    PluginRuntime.context().getExceptionHandler().handle(ex);
                }
                result.put(matchedWriter, view);
            }
        }
        return result;
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
            return forFileSystem(fs, project.getBuild().getFinalName(), componentsBasePath);
        } catch (IOException e) {
            // Exceptions caught here are critical for the execution, so no further handling
            throw new PluginException(CANNOT_WRITE_TO_PACKAGE_EXCEPTION_MESSAGE + project.getBuild().getFinalName(), e);
        }
    }

    /**
     * Initializes an instance of {@link PackageWriter} profiled for the particular {@link FileSystem} representing
     * the structure of the package
     * @param fileSystem Current {@link FileSystem} instance
     * @param projectName Name of the project this file system contains information for
     * @param componentsBasePath Path to the sub-folder within package under which AEM component folders are situated
     * @return {@code PackageWriter} instance
     */
    static PackageWriter forFileSystem(FileSystem fileSystem, String projectName, String componentsBasePath) {
        List<PackageEntryWriter> writers;
        try {
            Transformer transformer = XmlFactory.newDocumentTransformer();
            writers = Arrays.asList(
                    new ContentXmlWriter(transformer),
                    new CqDialogWriter(transformer, Scope.CQ_DIALOG),
                    new CqDialogWriter(transformer, Scope.CQ_DESIGN_DIALOG),
                    new CqEditConfigWriter(transformer),
                    new CqChildEditConfigWriter(transformer),
                    new CqHtmlTagWriter(transformer)
            );
        } catch (TransformerConfigurationException e) {
            // Exceptions caught here are due to possible XXE security vulnerabilities, so no further handling
            throw new PluginException(CANNOT_WRITE_TO_PACKAGE_EXCEPTION_MESSAGE + projectName, e);
        }
        return new PackageWriter(fileSystem, componentsBasePath, writers);
    }

    private static String getComponentPath(Class<?> componentClass) {
        String pathByComponent = Optional.ofNullable(componentClass.getAnnotation(Component.class))
            .map(Component::path)
            .orElse(null);
        String pathByDialog = Optional.ofNullable(componentClass.getAnnotation(Dialog.class))
            .map(Dialog::name)
            .orElse(null);
        return StringUtils.firstNonBlank(pathByComponent, pathByDialog);
    }
}
