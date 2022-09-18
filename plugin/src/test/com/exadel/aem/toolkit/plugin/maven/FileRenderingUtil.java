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
package com.exadel.aem.toolkit.plugin.maven;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.plugin.sources.ComponentSource;
import com.exadel.aem.toolkit.plugin.sources.Sources;
import com.exadel.aem.toolkit.plugin.writers.PackageWriter;

class FileRenderingUtil {
    private static final Logger LOG = LoggerFactory.getLogger(FileRenderingUtil.class);

    private static final String PROJECT_NAME = "test-package";

    private FileRenderingUtil() {
    }

    public static boolean doRenderingTest(
        FileSystem fileSystem,
        String className,
        Path sampleFilesPath)
        throws ClassNotFoundException, IOException {

        return doRenderingTest(fileSystem, className, null, sampleFilesPath);
    }

    public static boolean doRenderingTest(
        FileSystem fileSystem,
        String className,
        Path createdFilesPath,
        Path sampleFilesPath)
        throws ClassNotFoundException, IOException {

        Class<?> testableClass = Class.forName(className);
        ComponentSource testable =  Sources.fromComponentClass(testableClass);
        Path effectiveCreatedFilesPath = createdFilesPath == null
            ? fileSystem.getPath(TestConstants.PACKAGE_ROOT_PATH + TestConstants.DEFAULT_COMPONENT_NAME)
            : createdFilesPath;

        PackageWriter.forFileSystem(fileSystem, PROJECT_NAME).write(testable);

        Map<String, String> actualFiles = getFiles(effectiveCreatedFilesPath);
        Map<String, String> expectedFiles = getFiles(sampleFilesPath);

        for (String fileName : actualFiles.keySet()) {
            Path filePath = effectiveCreatedFilesPath.resolve(fileName);
            Files.delete(filePath);
        }

        return FileComparingUtil.compare(actualFiles, expectedFiles, sampleFilesPath.toString());
    }

    private static Map<String, String> getFiles(Path componentPath) {
        Map<String, String> files = new HashMap<>();
        try {
            for (Path filePath : Files.list(componentPath).collect(Collectors.toList())) {
                files.put(filePath.getFileName().toString(), String.join("", Files.readAllLines(filePath)));
            }
        } catch (NullPointerException | IOException ex) {
            LOG.error("Could not read the package " + componentPath, ex);
        }
        return files;
    }
}
