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
package com.exadel.aem.toolkit.regression;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

class VersionUpdateHelper {

    private static final String PLUGIN_DESCRIPTOR_PATH = "META-INF/maven/plugin.xml";

    private static final String PROPERTY_BUNDLE_VERSION = "Bundle-Version";

    private static final String VERSION_FORMAT = "<version>%s</version>";

    private VersionUpdateHelper() {
    }

    @SuppressWarnings("SameParameterValue")
    static void update(ArtifactInfo artifact, String newVersion) throws IOException {
        if (StringUtils.isAnyEmpty(artifact.getPath(), artifact.getVersion())) {
            return;
        }
        String modifiedName = artifact.getPath().replaceAll(artifact.getVersion() + "(\\.\\w+)$", newVersion + "$1");
        Path oldPath = Paths.get(artifact.getPath());
        Path newPath = Paths.get(modifiedName);
        if (ArtifactInfo.PACKAGING_POM.equals(artifact.getPackaging())) {
            ensureFile(newPath);
            createPomCopy(oldPath, artifact.getVersion(), newPath, newVersion);
        } else if (ArtifactInfo.PACKAGING_PLUGIN.equals(artifact.getPackaging())) {
            ensureFile(newPath);
            createJarCopy(oldPath, artifact.getVersion(), newPath, newVersion, PLUGIN_DESCRIPTOR_PATH::equals);
        } else if (ArtifactInfo.EXTENSION_JAR.equals(artifact.getFileExtension())) {
            ensureFile(newPath);
            createJarCopy(oldPath, artifact.getVersion(), newPath, newVersion, null);
        } else {
            Files.copy(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
        }
        artifact.setPath(newPath.toAbsolutePath().toString());
        artifact.setVersion(newVersion);
    }

    private static void ensureFile(Path value) throws IOException {
        if (!value.toFile().exists()) {
            Files.createFile(value);
        }
    }

    private static void createPomCopy(
        Path oldPath,
        String oldVersion,
        Path newPath,
        String newVersion) throws IOException {

        try (
            InputStream input = Files.newInputStream(oldPath);
            OutputStream output = new FileOutputStream(newPath.toFile(), false)
        ) {
            String content = IOUtils.toString(input, StandardCharsets.UTF_8);
            content = StringUtils.replaceOnce(
                content,
                String.format(VERSION_FORMAT, oldVersion),
                String.format(VERSION_FORMAT, newVersion));
            IOUtils.write(content, output, StandardCharsets.UTF_8);
        }
    }

    private static void createJarCopy(
        Path oldPath,
        String oldVersion,
        Path newPath,
        String newVersion,
        Predicate<String> versionEntrySelector) throws IOException {

        Manifest manifest = getManifest(oldPath);
        manifest.getMainAttributes().putValue(PROPERTY_BUNDLE_VERSION, newVersion);

        try (
            InputStream jarInput = Files.newInputStream(oldPath);
            JarInputStream jarInputStream = new JarInputStream(jarInput);
            FileOutputStream jarOutput = new FileOutputStream(newPath.toFile(), false);
            JarOutputStream jarOutputStream = new JarOutputStream(jarOutput, manifest)
        ) {
            JarEntry inputEntry;
            while ((inputEntry = jarInputStream.getNextJarEntry()) != null) {
                jarOutputStream.putNextEntry(new JarEntry(inputEntry.getName()));
                if (versionEntrySelector != null && versionEntrySelector.test(inputEntry.getName())) {
                    String content = IOUtils.toString(jarInputStream, StandardCharsets.UTF_8);
                    content = StringUtils.replaceOnce(
                        content,
                        String.format(VERSION_FORMAT, oldVersion),
                        String.format(VERSION_FORMAT, newVersion));
                    IOUtils.write(content, jarOutputStream, StandardCharsets.UTF_8);
                } else {
                    byte[] content = IOUtils.toByteArray(jarInputStream);
                    IOUtils.write(content, jarOutputStream);
                }
                jarOutputStream.closeEntry();
            }
            jarOutputStream.finish();
        }
    }

    private static Manifest getManifest(Path path) throws IOException {
        try (JarFile jarFile = new JarFile(path.toFile())) {
            return jarFile.getManifest();
        }
    }
}
