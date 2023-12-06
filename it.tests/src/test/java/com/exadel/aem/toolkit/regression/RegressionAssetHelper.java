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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;

class RegressionAssetHelper {

    private static final String PROPERTY_TMPDIR = "java.io.tmpdir";
    private static final String DIRECTORY_PREFIX = "eak-regression-";

    private Path tempDirectory;

    @SuppressWarnings("SameParameterValue")
    Path createTempRoot(String name) {
        Path newPath = Paths.get(System.getProperty(PROPERTY_TMPDIR), DIRECTORY_PREFIX + name);
        if (newPath.toFile().exists()) {
            return newPath.toAbsolutePath();
        }
        try {
            Files.createDirectory(newPath);
            return newPath.toAbsolutePath();
        } catch (IOException e) {
            throw new AssertionError("Could not create directory " + newPath.toAbsolutePath(), e);
        }
    }

    Path getTempRoot() {
        if (tempDirectory != null) {
            return tempDirectory;
        }
        try {
            tempDirectory = Files.createTempDirectory(DIRECTORY_PREFIX);
        } catch (IOException e) {
            throw new AssertionError("Could not create temp directory", e);
        }
        return tempDirectory;
    }

    @SuppressWarnings("SameParameterValue")
    Path retainPackages(File projectDirectory, String selector) {
        return retainPackages(projectDirectory, getTempRoot().resolve(selector));
    }

    Path retainPackages(File projectDirectory, Path storePath) {
        List<File> packageFiles = getPackages(projectDirectory);
        Assert.assertFalse("Could not find any packages at " + projectDirectory, packageFiles.isEmpty());
        try {
            if (!storePath.toFile().exists()) {
                Files.createDirectory(storePath);
            }
            for (File file : packageFiles) {
                FileUtils.copyFileToDirectory(file, storePath.toFile());
            }
            return storePath;
        } catch (IOException e) {
            throw new AssertionError("Could not retain package file(-s)", e);
        }
    }

    void cleanUp() {
        if (tempDirectory == null) {
            return;
        }
        try {
            FileUtils.deleteDirectory(tempDirectory.toFile());
        } catch (IOException e) {
            RegressionTest.LOG.warn("Could not clean up {}", tempDirectory, e);
        }
    }

    private static List<File> getPackages(File directory) {
        try (Stream<Path> paths = Files.walk(Paths.get(directory.getAbsolutePath()))){
            return paths
                .filter(path -> StringUtils.contains(path.toString(), File.separator + "target" + File.separator))
                .filter(path -> StringUtils.endsWithIgnoreCase(path.getFileName().toString(), ".zip"))
                .map(Path::toFile)
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new AssertionError("Could not enumerate package files at "+ directory.getAbsolutePath(), e);
        }
    }
}
