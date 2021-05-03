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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.meta.Scopes;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.exceptions.InvalidSettingException;
import com.exadel.aem.toolkit.plugin.exceptions.PluginException;
import com.exadel.aem.toolkit.plugin.exceptions.UnknownComponentException;
import com.exadel.aem.toolkit.plugin.exceptions.ValidationException;
import com.exadel.aem.toolkit.plugin.maven.PluginInfo;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.utils.XmlFactory;

/**
 * Implements actions needed to store collected/processed data into AEM package, optimal for use in "try-with-resources" block
 */
public class PackageWriter implements AutoCloseable {

    private static final String PACKAGE_EXTENSION = ".zip";
    private static final String FILESYSTEM_PREFIX = "jar:";
    private static final Map<String, String> FILESYSTEM_OPTIONS = ImmutableMap.of("create", "true");

    private static final String PACKAGE_ROOT_DIRECTORY = "jcr_root";
    private static final String PACKAGE_INFO_DIRECTORY = "META-INF/etoolbox-authoring-kit";
    private static final String PACKAGE_INFO_FILE_NAME = "version.info";

    private static final String CANNOT_WRITE_TO_PACKAGE_EXCEPTION_MESSAGE = "Cannot write to package ";
    private static final String COMPONENT_DATA_MISSING_EXCEPTION_MESSAGE = "No data to build .content.xml file wile processing component ";
    private static final String COMPONENT_NAME_MISSING_EXCEPTION_MESSAGE = "Component name missing in class ";
    private static final String COMPONENT_PATH_MISSING_EXCEPTION_MESSAGE = "Component path missing for project ";
    private static final String INVALID_PROJECT_EXCEPTION_MESSAGE = "Invalid project";
    private static final String MULTIPLE_MODULES_EXCEPTION_MESSAGE = "Multiple modules available for %s while processing component %s";
    private static final String UNRECOGNIZED_MODULE_EXCEPTION_MESSAGE = "Unrecognized component module %s while processing component %s";


    /* -----------------------------
       Class fields and constructors
       ----------------------------- */

    private final String componentsPathBase;
    private final FileSystem fileSystem;
    private final List<PackageEntryWriter> writers;

    private PackageWriter(FileSystem fileSystem, String componentsPathBase, List<PackageEntryWriter> writers) {
        this.fileSystem = fileSystem;
        this.componentsPathBase = componentsPathBase;
        this.writers = writers;
    }


    /* ------------------------
       Public interface members
       ------------------------ */

    @Override
    public void close() {
        try {
            fileSystem.close();
        } catch (IOException e) {
            throw new PluginException(CANNOT_WRITE_TO_PACKAGE_EXCEPTION_MESSAGE, e);
        }
    }


    /* ----------------
       Instance members
       ---------------- */

    /**
     * Stores information about the current binary into the package for the purposes of versioning
     * @param info {@link PluginInfo} object
     */
    public void writeInfo(PluginInfo info) {
        Path rootPath = fileSystem.getRootDirectories().iterator().next();
        if (!Files.isWritable(rootPath)) {
            return;
        }
        Path infoDirPath = rootPath.resolve(PACKAGE_INFO_DIRECTORY);
        Path infoFilePath = infoDirPath.resolve(PACKAGE_INFO_FILE_NAME);
        try {
            Files.createDirectories(infoDirPath);
            Files.createFile(infoFilePath);
        } catch (IOException e) {
            PluginRuntime.context().getExceptionHandler().handle(e);
        }

        try (
            BufferedWriter fileWriter = Files.newBufferedWriter(infoFilePath, StandardOpenOption.CREATE);
            PrintWriter printWriter = new PrintWriter(fileWriter)
        ) {
            printWriter.println(info.getName());
            printWriter.println(info.getVersion());
            printWriter.println(info.getTimestamp());
        } catch (IOException e) {
            PluginRuntime.context().getExceptionHandler().handle(e);
        }
    }

    /**
     * Stores AEM component's authoring markup into the package. To do this, several package entry writers,
     * e.g. for populating {@code .content.xml}, {@code _cq_dialog.xml}, {@code _cq_editConfig.xml}, etc.
     * are called in sequence. If the component is split into several "modules" (views), each is processed separately
     * @param componentClass Current {@code Class} instance
     */
    public void write(Class<?> componentClass) {
        String providedComponentPath = getComponentPath(componentClass);
        if (StringUtils.isBlank(providedComponentPath)) {
            ValidationException validationException = new ValidationException(COMPONENT_NAME_MISSING_EXCEPTION_MESSAGE + componentClass.getSimpleName());
            PluginRuntime.context().getExceptionHandler().handle(validationException);
            return;
        }

        Path fullComponentPath;
        if (providedComponentPath.startsWith(PACKAGE_ROOT_DIRECTORY)) {
            fullComponentPath = fileSystem.getPath(providedComponentPath);
        } else if (providedComponentPath.startsWith(CoreConstants.SEPARATOR_SLASH + PACKAGE_ROOT_DIRECTORY)) {
            fullComponentPath = fileSystem.getPath(providedComponentPath.substring(1));
        } else if (providedComponentPath.startsWith(CoreConstants.SEPARATOR_SLASH)) {
            fullComponentPath = fileSystem.getPath(PACKAGE_ROOT_DIRECTORY + providedComponentPath);
        } else {
            fullComponentPath = fileSystem.getPath(componentsPathBase, providedComponentPath);
        }
        if (!Files.isWritable(fullComponentPath)) {
            PluginRuntime.context().getExceptionHandler().handle(new UnknownComponentException(fullComponentPath));
            return;
        }

        Map<PackageEntryWriter, Class<?>> viewsByWriter = getComponentViews(componentClass);

        if (viewsByWriter.keySet().stream().noneMatch(writer -> Scopes.COMPONENT.equals(writer.getScope()))) {
            InvalidSettingException e = new InvalidSettingException(
                COMPONENT_DATA_MISSING_EXCEPTION_MESSAGE + componentClass.getName());
            PluginRuntime.context().getExceptionHandler().handle(e);
        }

        viewsByWriter.forEach((writer, view) -> {
            writer.cleanUp(fullComponentPath);
            writer.writeXml(view, fullComponentPath);
        });
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
            .ofNullable(componentClass.getAnnotation(AemComponent.class))
            .map(AemComponent::views)
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
                if (result.containsKey(matchedWriter) && !Scopes.COMPONENT.equals(matchedWriter.getScope())) {
                    InvalidSettingException ex = new InvalidSettingException(String.format(
                        MULTIPLE_MODULES_EXCEPTION_MESSAGE,
                        matchedWriter.getScope(),
                        componentClass.getName()
                    ));
                    PluginRuntime.context().getExceptionHandler().handle(ex);
                } else if (!result.containsKey(matchedWriter)) {
                    result.put(matchedWriter, view);
                }
            }
        }
        return result;
    }


    /* ---------------
       Utility methods
       --------------- */

    /**
     * Retrieves the path specified for the current component class in either {@link AemComponent} or {@link Dialog} annotation
     * @param componentClass The {@code Class<?>} to get the path for
     * @return String value
     */
    private static String getComponentPath(Class<?> componentClass) {
        String pathByComponent = Optional.ofNullable(componentClass.getAnnotation(AemComponent.class))
            .map(AemComponent::path)
            .orElse(null);
        @SuppressWarnings("deprecation") // "name" processing is for compatibility; will be removed in a version after 2.0.2
        String pathByDialog = Optional.ofNullable(componentClass.getAnnotation(Dialog.class))
            .map(Dialog::name)
            .orElse(null);
        return StringUtils.firstNonBlank(pathByComponent, pathByDialog);
    }


    /* ---------------
       Factory methods
       --------------- */

    /**
     * Initializes an instance of {@link PackageWriter} profiled for the current {@link MavenProject} and the tree of
     * folders storing AEM components' data
     * @param project            {@code MavenProject instance}
     * @param componentsPathBase Path to the sub-folder within package under which AEM component folders are situated
     * @return {@code PackageWriter} instance
     */
    public static PackageWriter forMavenProject(MavenProject project, String componentsPathBase) {
        if (StringUtils.isBlank(componentsPathBase)) {
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
            return forFileSystem(fs, project.getBuild().getFinalName(), componentsPathBase);
        } catch (IOException e) {
            // Exceptions caught here are critical for the execution, so no further handling
            throw new PluginException(CANNOT_WRITE_TO_PACKAGE_EXCEPTION_MESSAGE + project.getBuild().getFinalName(), e);
        }
    }

    /**
     * Initializes an instance of {@link PackageWriter} profiled for the particular {@link FileSystem} representing
     * the structure of the package
     * @param fileSystem         Current {@link FileSystem} instance
     * @param projectName        Name of the project this file system contains information for
     * @param componentsPathBase Path to the sub-folder within package under which AEM component folders are situated
     * @return {@code PackageWriter} instance
     */
    static PackageWriter forFileSystem(FileSystem fileSystem, String projectName, String componentsPathBase) {
        List<PackageEntryWriter> writers;
        try {
            Transformer transformer = XmlFactory.newDocumentTransformer();
            writers = Arrays.asList(
                    new ContentXmlWriter(transformer),
                    new CqDialogWriter(transformer, Scopes.CQ_DIALOG),
                    new CqDialogWriter(transformer, Scopes.CQ_DESIGN_DIALOG),
                    new CqEditConfigWriter(transformer),
                    new CqChildEditConfigWriter(transformer),
                    new CqHtmlTagWriter(transformer)
            );
        } catch (TransformerConfigurationException e) {
            // Exceptions caught here are due to possible XXE security vulnerabilities, so no further handling
            throw new PluginException(CANNOT_WRITE_TO_PACKAGE_EXCEPTION_MESSAGE + projectName, e);
        }
        return new PackageWriter(fileSystem, componentsPathBase, writers);
    }
}
