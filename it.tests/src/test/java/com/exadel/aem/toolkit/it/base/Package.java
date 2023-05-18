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
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.jimfs.Jimfs;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.sources.ComponentSource;
import com.exadel.aem.toolkit.plugin.sources.Sources;
import com.exadel.aem.toolkit.plugin.writers.PackageWriter;

class Package implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(Package.class);

    static final String FILE_NAME = "etoolbox-authoring-kit-tests.zip";
    private static final String ROOT_FOLDER = "package";

    private final Map<String, byte[]> entries = new HashMap<>();

    private FileSystem fileSystem;
    private String fileName;

    public Package() {
        try {
            URI rootUri = Objects.requireNonNull(getClass().getClassLoader().getResource(ROOT_FOLDER)).toURI();
            Files.walk(Paths.get(Objects.requireNonNull(rootUri))).forEach(this::includeContent);
        } catch (URISyntaxException | IOException | NullPointerException e) {
            LOG.error("Could not assemble static content for the package", e);
        }
    }

    @Override
    public void close() throws IOException {
        if (fileSystem != null) {
            fileSystem.close();
        }
    }

    public String getPath() {
        return fileName;
    }

    public void setPath(String fileName) {
        this.fileName = fileName;
    }

    public byte[] toByteArray() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
                    zos.putNextEntry(new ZipEntry(entry.getKey()));
                    zos.write(entry.getValue());
                    zos.closeEntry();
                }
            } catch (IOException e) {
                LOG.error("Error compiling content package", e);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            LOG.warn("Could not close the in-memory output stream", e);
        }
        return null;
    }

    public void includeRender(Class<?> component) {
        ComponentSource componentSource = Sources.fromComponentClass(component);
        String path = componentSource.getPath();
        if (StringUtils.isEmpty(path)) {
            return;
        }

        Path renderedPath = getFileSystem().getPath(path);
        createDirectories(renderedPath);

        PackageWriter.forFileSystem(getFileSystem(), StringUtils.EMPTY).write(componentSource);
        try {
            List<Path> renderedFilePaths = Files.list(renderedPath).filter(p -> p.toString().endsWith(".xml")).collect(Collectors.toList());
            for (Path renderedFilePath : renderedFilePaths) {
                entries.put(
                    renderedFilePath.toString().replace(File.separator, CoreConstants.SEPARATOR_SLASH),
                    IOUtils.toByteArray(renderedFilePath.toUri()));
            }
        } catch (IOException e) {
            LOG.error("Could not read rendered files", e);
        }
    }

    private void createDirectories(Path value) {
        try {
            Files.createDirectories(value);
        } catch (IOException e) {
            LOG.error("Could not provide the component path {}", value, e);
            e.printStackTrace();
        }
    }

    private void includeContent(Path path) {
        if (path.toFile().isDirectory()) {
            return;
        }
        String packagePath = StringUtils
            .substringAfter(
                path.toString(),
                File.separator + ROOT_FOLDER + File.separator)
            .replace(File.separator, CoreConstants.SEPARATOR_SLASH);
        try {
            byte[] content = IOUtils.toByteArray(path.toUri());
            if (ArrayUtils.isNotEmpty(content)) {
                entries.put(packagePath, content);
            }
        } catch (IOException e) {
            LOG.error("Could not read file at {}", path, e);
        }
    }

    private FileSystem getFileSystem() {
        if (fileSystem != null) {
            return fileSystem;
        }
        fileSystem = Jimfs.newFileSystem();
        return fileSystem;
    }
}
