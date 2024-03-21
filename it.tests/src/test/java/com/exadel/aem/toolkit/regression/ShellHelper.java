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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.plexus.util.cli.Commandline;

import com.exadel.aem.toolkit.core.CoreConstants;

class ShellHelper {

    static final String MAVEN_PROP_FORMAT = "-D%s=%s";
    private static final String MAVEN_OPTION_BATCH = "--batch-mode";

    private ShellHelper() {
    }

    static Commandline getBuildCommand(RegressionSettings settings, ProjectInfo project, String version) {
        Commandline commandLine = new Commandline(settings.getMavenExecutable());
        commandLine.addArguments(new String[]{
            "package",
            String.format(MAVEN_PROP_FORMAT, "maven.test.skip", true),
            MAVEN_OPTION_BATCH});
        if (CollectionUtils.isNotEmpty(project.getOptions())) {
            Map<String, String> interpolatableValues = new HashMap<>();
            interpolatableValues.put(RegressionSettings.PROPERTY_VERSION, version);
            interpolatableValues.put("version", version);
            commandLine.addArguments(project.getOptions(interpolatableValues).toArray(new String[0]));
        }
        commandLine.setWorkingDirectory(project.getDirectory().toFile());
        return commandLine;
    }

    static Commandline getDownloadCommand(RegressionSettings settings, String artifact, Path repoDirectory) {
        Commandline commandLine = new Commandline(settings.getMavenExecutable());
        commandLine.addArguments(new String[]{
            "dependency:get",
            String.format(MAVEN_PROP_FORMAT, "artifact", artifact),
            String.format(MAVEN_PROP_FORMAT, "maven.repo.local", repoDirectory),
            String.format(MAVEN_PROP_FORMAT, "transitive", false),
            String.format(MAVEN_PROP_FORMAT, "remoteRepositories", settings.getSnapshotRepository()),
            MAVEN_OPTION_BATCH});
        commandLine.setWorkingDirectory(repoDirectory.toFile());
        return commandLine;
    }

    static Commandline getInstallCommand(RegressionSettings settings, ArtifactInfo artifact, Path repoDirectory) {
        Commandline commandLine = new Commandline(settings.getMavenExecutable());
        String packaging = artifact.getPackaging();
        if (ArtifactInfo.PACKAGING_CONTENT.equals(packaging)) {
            packaging = ArtifactInfo.EXTENSION_ZIP;
        }
        commandLine.addArguments(new String[]{
            "install:install-file",
            String.format(MAVEN_PROP_FORMAT, "file", artifact.getPath()),
            String.format(MAVEN_PROP_FORMAT, "groupId", artifact.getGroupId()),
            String.format(MAVEN_PROP_FORMAT, "artifactId", artifact.getId()),
            String.format(MAVEN_PROP_FORMAT, "version", artifact.getVersion()),
            String.format(MAVEN_PROP_FORMAT, "packaging", packaging),
            MAVEN_OPTION_BATCH});
        if (!ArtifactInfo.PACKAGING_POM.equals(artifact.getPackaging())) {
            commandLine.addArguments(new String[]{
                String.format(MAVEN_PROP_FORMAT, "pomFile", artifact.getPomPath())
            });
        }
        commandLine.setWorkingDirectory(repoDirectory.toFile());
        return commandLine;
    }

    static Commandline getPurgeCommand(RegressionSettings settings, List<ArtifactInfo> artifacts) {
        Commandline commandLine = new Commandline(settings.getMavenExecutable());
        String artifactsLine = artifacts
            .stream()
            .map(ArtifactInfo::getFullName)
            .collect(Collectors.joining(CoreConstants.SEPARATOR_COMMA));
        commandLine.addArguments(new String[]{
            "dependency:purge-local-repository",
            String.format(MAVEN_PROP_FORMAT, "manualInclude", artifactsLine),
            MAVEN_OPTION_BATCH
        });
        commandLine.setWorkingDirectory(Paths.get(StringUtils.EMPTY).toAbsolutePath().toString());
        return commandLine;
    }
}
