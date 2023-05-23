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
package com.exadel.aem.toolkit.it.base;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.jimfs.Jimfs;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.sources.ComponentSource;
import com.exadel.aem.toolkit.plugin.sources.Sources;
import com.exadel.aem.toolkit.plugin.writers.PackageWriter;

/**
 * Represents a synthetic content package containing test cases and adjacent content that is deployed to an AEM instance
 * before running the tests
 */
class Package implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(Package.class);

    static final String FILE_NAME = "etoolbox-authoring-kit-tests.zip";
    private static final String ROOT_FOLDER = "package";

    private static final Pattern PATTERN_SYSTEM_PROPERTY = Pattern.compile("\\$\\$\\{([\\w.-]+)}");

    private static final String EXCEPTION_COULD_NOT_ASSEMBLE = "Could not assemble static content for the package";

    private final Map<String, byte[]> entries = new HashMap<>();

    private FileSystem fileSystem;
    private String path;

    /* -----------------------
       Public contract methods
       ----------------------- */

    /**
     * Default constructor
     */
    public Package() {
        URI rootUri;
        try {
            rootUri = Objects.requireNonNull(getClass().getClassLoader().getResource(ROOT_FOLDER)).toURI();
        } catch (URISyntaxException e) {
            LOG.error(EXCEPTION_COULD_NOT_ASSEMBLE, e);
            return;
        }
        try (Stream<Path> pathStream = Files.walk(Paths.get(Objects.requireNonNull(rootUri)))) {
            pathStream.forEach(this::includeStaticContent);
        } catch (IOException | NullPointerException e) {
            LOG.error(EXCEPTION_COULD_NOT_ASSEMBLE, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        if (fileSystem != null) {
            fileSystem.close();
        }
    }

    /* ----------------
       Public accessors
       ---------------- */

    /**
     * Retrieves the package path
     * @return String value
     */
    public String getPath() {
        return path;
    }

    /**
     * Assigns the package path
     * @param path String value
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Produces the binary entity that contains the package data. This entity is transferred to the AEM's
     * {@code PackageManager} network endpoint
     * @return Array of bytes
     */
    public byte[] toByteArray() {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            try (ZipOutputStream zos = new ZipOutputStream(output)) {
                for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
                    zos.putNextEntry(new ZipEntry(entry.getKey()));
                    zos.write(entry.getValue());
                    zos.closeEntry();
                }
            } catch (IOException e) {
                LOG.error("Error compiling content package", e);
            }
            return output.toByteArray();
        } catch (IOException e) {
            LOG.warn("Could not close the in-memory output stream", e);
        }
        return null;
    }

    /* --------------------
       Content manipulation
       -------------------- */

    /**
     * Renders the Granite dialog / config files for the provided AEM component and adds them to the content package
     * @param component {@code Class} reference that represents the AEM component
     */
    public void includeRender(Class<?> component) {
        ComponentSource componentSource = Sources.fromComponentClass(component);
        String path = componentSource.getPath();
        if (StringUtils.isEmpty(path)) {
            return;
        }

        Path renderedPath = getFileSystem().getPath(path);
        try {
            Files.createDirectories(renderedPath);
        } catch (IOException e) {
            LOG.error("Could not provide the component path {}", renderedPath, e);
            return;
        }

        PackageWriter.forFileSystem(getFileSystem(), StringUtils.EMPTY).write(componentSource);
        try (Stream<Path> pathStream = Files.list(renderedPath)) {
            List<Path> renderedFilePaths = pathStream.filter(p -> p.toString().endsWith(".xml")).collect(Collectors.toList());
            for (Path renderedFilePath : renderedFilePaths) {
                entries.put(
                    renderedFilePath.toString().replace(File.separator, CoreConstants.SEPARATOR_SLASH),
                    IOUtils.toByteArray(renderedFilePath.toUri()));
            }
        } catch (IOException e) {
            LOG.error("Could not read rendered files", e);
        }
    }

    /**
     * Adds the file resource specified by {@code path} to the content package
     * @param path {@link Path} object
     */
    private void includeStaticContent(Path path) {
        if (path.toFile().isDirectory()) {
            return;
        }
        String packagePath = StringUtils
            .substringAfter(
                path.toString(),
                File.separator + ROOT_FOLDER + File.separator)
            .replace(File.separator, CoreConstants.SEPARATOR_SLASH);
        try {
            String content = IOUtils.toString(path.toUri(), StandardCharsets.UTF_8);
            if (StringUtils.isNotEmpty(content)) {
                entries.put(packagePath, populateProperties(content).getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            LOG.error("Could not read a file at {}", path, e);
        }
    }

    /**
     * Processes the file resource before adding it to the package. Looks for property placeholders in the resource
     * content and replaces them with the system property values
     * @param value The content of a resource
     * @return Processed content string
     */
    private String populateProperties(String value) {
        StringBuilder result = new StringBuilder(value);
        Matcher matcher = PATTERN_SYSTEM_PROPERTY.matcher(result.toString());
        while (matcher.find()) {
            result.replace(
                matcher.start(),
                matcher.end(),
                AemConnection.getProperty(matcher.group(1)));
            matcher.reset(result.toString());
        }
        return result.toString();
    }

    /* -----------------
       File system logic
       ----------------- */

    /**
     * Initializes (if needed) and retrieves the file system object used to build the content package
     * @return {@link FileSystem} object
     */
    private FileSystem getFileSystem() {
        if (fileSystem != null) {
            return fileSystem;
        }
        fileSystem = Jimfs.newFileSystem();
        return fileSystem;
    }
}
