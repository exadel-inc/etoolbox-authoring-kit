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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;

class RegressionAssetHelper {

    private static final String ARTIFACT_ID =  System.getProperty("eak.id", "etoolbox-authoring-kit");
    private static final String[] ARTIFACT_SELECTORS = new String[] {"core", "plugin", "ui.apps", "ui.content"};

    private static final String FOLDER_REPO = "repo";

    private Path tempDirectory;

    /* ---------------
       Artifacts logic
       --------------- */

    void retainArtifacts(ProjectSettings settings) {
        Path tempRepoPath = getTempDirectory().resolve(FOLDER_REPO);
        try {
            if (!tempRepoPath.toFile().exists()) {
                Files.createDirectory(tempRepoPath);
            }
            Path mavenDirectory = settings.getMavenDirectory();
            if (!mavenDirectory.toFile().exists()) {
                throw new AssertionError("Could not find local Maven repository");
            }
            List<File> artifacts = getArtifactDirectories(mavenDirectory, settings.getCurrentVersion());
            for (File artifact : artifacts) {
                String parentFolderName = artifact.toPath().getParent().getFileName().toString();
                FileUtils.moveDirectory(artifact, tempRepoPath.resolve(parentFolderName).toFile());
            }
        } catch (IOException e) {
            RegressionTest.LOG.warn("Could not retain artifact(-s): {}", e.getMessage());
        }
    }

    void restoreArtifacts(ProjectSettings settings) {
        File tempRepoDirectory = getTempDirectory().resolve(FOLDER_REPO).toFile();
        if (!tempRepoDirectory.exists()) {
            return;
        }
        Path mavenDirectory = settings.getMavenDirectory();
        if (!mavenDirectory.toFile().exists()) {
            throw new AssertionError("Could not find local Maven repository");
        }
        File[] artifacts = tempRepoDirectory.listFiles();
        if (ArrayUtils.isEmpty(artifacts)) {
            return;
        }
        try {
            assert artifacts != null;
            for (File artifact : artifacts) {
                Path existingMavenArtifact = mavenDirectory.resolve(artifact.getName()).resolve(settings.getCurrentVersion());
                Files.deleteIfExists(existingMavenArtifact);
                Files.createDirectories(existingMavenArtifact.getParent());
                FileUtils.moveDirectory(artifact, existingMavenArtifact.toFile());
            }
        } catch (IOException e) {
            throw new AssertionError("Could not restore artifact(-s): " + e.getMessage());
        }
    }

    private static List<File> getArtifactDirectories(Path mavenDirectory, String version) {
        File[] folders = mavenDirectory
            .toFile()
            .listFiles(RegressionAssetHelper::isMatchingArtifactDirectory);
        if (ArrayUtils.isEmpty(folders)) {
            return Collections.emptyList();
        }
        assert folders != null;
        return Arrays
            .stream(folders)
            .map(File::toPath)
            .map(path -> path.resolve(version))
            .map(Path::toFile)
            .filter(file -> file.exists() && file.isDirectory())
            .collect(Collectors.toList());
    }

    private static boolean isMatchingArtifactDirectory(File value) {
        if (!value.isDirectory() || !value.getName().startsWith(ARTIFACT_ID)) {
            return false;
        }
        if (value.getName().equals(ARTIFACT_ID)) {
            return true;
        }
        String selector = value.getName().substring(ARTIFACT_ID.length() + 1);
        return ArrayUtils.contains(ARTIFACT_SELECTORS, selector);
    }

    /* --------------
       Packages logic
       -------------- */

    @SuppressWarnings("SameParameterValue")
    Path retainPackages(File projectDirectory, String selector) {
        return retainPackages(projectDirectory, getTempDirectory().resolve(selector));
    }

    Path retainPackages(File projectDirectory, Path storePath) {
        List<File> packageFiles = getPackages(projectDirectory);
        Assert.assertFalse("Could not find any packages", packageFiles.isEmpty());
        try {
            if (!storePath.toFile().exists()) {
                Files.createDirectory(storePath);
            }
            for (File file : packageFiles) {
                FileUtils.copyFileToDirectory(file, storePath.toFile());
            }
            return storePath;
        } catch (IOException e) {
            throw new AssertionError("Could not retain package file(-s): " + e.getMessage());
        }
    }

    private Path getTempDirectory() {
        if (tempDirectory != null) {
            return tempDirectory;
        }
        try {
            tempDirectory = Files.createTempDirectory("eak-regression-");
        } catch (IOException e) {
            throw new AssertionError("Could not create temp directory: " + e.getMessage());
        }
        return tempDirectory;
    }

    private static List<File> getPackages(File directory) {
        try (Stream<Path> paths = Files.walk(Paths.get(directory.getAbsolutePath()))){
            return paths
                .filter(path -> StringUtils.contains(path.toString(), File.separator + "target" + File.separator))
                .filter(path -> StringUtils.endsWithIgnoreCase(path.getFileName().toString(), ".zip"))
                .map(Path::toFile)
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new AssertionError("Could not enumerate package files at "
                + directory.getAbsolutePath()
                + ": " + e.getMessage());
        }
    }

    /* -------
       Cleanup
       ------- */

    void cleanUp() {
        if (tempDirectory == null) {
            return;
        }
        try {
            Files.deleteIfExists(tempDirectory);
        } catch (IOException e) {
            RegressionTest.LOG.warn("Could not clean up {}", tempDirectory, e);
        }
    }
}
