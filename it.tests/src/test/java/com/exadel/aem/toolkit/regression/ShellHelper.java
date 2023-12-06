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
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.plexus.util.cli.Commandline;

import com.exadel.aem.toolkit.core.CoreConstants;

class ShellHelper {

    static final String MAVEN_PROP_FORMAT = "-D%s=%s";
    static final String MAVEN_SNAPSHOT_VERSION = "0.0.1-SNAPSHOT";
    private static final String MAVEN_OPTION_BATCH = "--batch-mode";

    private ShellHelper() {
    }

    static Commandline getBuildCommand(ProjectSettings settings) {
        Commandline commandLine = new Commandline(settings.getMavenExecutable());
        commandLine.addArguments(new String[]{
            "package",
            String.format(MAVEN_PROP_FORMAT, "maven.test.skip", true),
            MAVEN_OPTION_BATCH});
        if (StringUtils.isNotBlank(settings.getModules())) {
            commandLine.addArguments(new String[]{"-pl", settings.getModules()});
        }
        commandLine.setWorkingDirectory(settings.getProjectDirectory());
        return commandLine;
    }

    static Commandline getDownloadCommand(ProjectSettings settings, String artifact, Path repoDirectory) {
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

    static Commandline getInstallCommand(ProjectSettings settings, ArtifactInfo artifact, Path repoDirectory) {
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
        commandLine.setWorkingDirectory(repoDirectory.toFile());
        return commandLine;
    }

    static Commandline getPurgeCommand(ProjectSettings settings, List<ArtifactInfo> artifacts) {
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
