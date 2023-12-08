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

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegressionTest {

    static final Logger LOG = LoggerFactory.getLogger(RegressionTest.class);

    private static final String SHELL_COMMAND_FORMAT = "{}> {}";
    private static final String OPERATION_FAILED = "%s failed (wrong exit code)";

    private static final Pattern PATTERN_LOG_LEVEL = Pattern.compile("^\\s*\\[([A-Z]+)]\\s+");

    private RegressionSettings settings;
    private RegressionAssetHelper assetHelper;

    @Before
    public void setUp() {
        settings = new RegressionSettings();
        assetHelper = new RegressionAssetHelper();
    }

    @Test
    public void doRegression() {
        settings.validate();
        for (ProjectInfo project : settings.getProjects()) {
            project.validate();
            doRegression(project);
        }
    }

    private void doRegression(ProjectInfo project) {
        LOG.info("Using project {}", project.getDirectory());
        LOG.info("Comparing version {} to {}", project.getDeclaredEakVersion(), settings.getCurrentEakVersion());
        LOG.info("Using Maven executable {}", settings.getMavenExecutable());

        try {
            boolean versionsAreEqual = StringUtils.equals(project.getDeclaredEakVersion(), settings.getCurrentEakVersion())
                && StringUtils.contains(settings.getCurrentEakVersion(), "-SNAPSHOT");
            boolean result = !versionsAreEqual
                ? doRegressionWithDifferingVersions(project)
                : doRegressionWithEqualVersions(project);
            Assert.assertTrue("There were differences in regression content. See log above", result);
        } finally {
            if (settings.cleanUp()) {
                LOG.info("Cleaning up regression content");
                assetHelper.cleanUp();
            } else {
                LOG.info("Regression content is stored at {}", assetHelper.getTempRoot());
            }
        }
    }

    private boolean doRegressionWithDifferingVersions(ProjectInfo project) {
        LOG.info("Running regression build with EAK version {}", project.getDeclaredEakVersion());

        Commandline buildCommand = ShellHelper.getBuildCommand(settings, project, project.getDeclaredEakVersion());
        LOG.info(SHELL_COMMAND_FORMAT, buildCommand.getWorkingDirectory(), buildCommand);
        int exitCode = runCommand(buildCommand);
        Assert.assertEquals(
            String.format(OPERATION_FAILED, "Regression build with EAK version " + project.getDeclaredEakVersion()),
            0,
            exitCode);

        Path oldPackagesDirectory = assetHelper.retainPackages(project.getDirectory(), "old");
        LOG.info("EAK version {} files stored at {}", project.getDeclaredEakVersion(), oldPackagesDirectory);

        LOG.info("Running regression build with EAK version {}", settings.getCurrentEakVersion());

        buildCommand = ShellHelper.getBuildCommand(settings, project, settings.getCurrentEakVersion());
        LOG.info(SHELL_COMMAND_FORMAT, buildCommand.getWorkingDirectory(), buildCommand);
        exitCode = runCommand(buildCommand);
        Assert.assertEquals(
            String.format(OPERATION_FAILED, "Regression build with EAK version " + settings.getCurrentEakVersion()),
            0,
            exitCode);

        Path newPackagesDirectory = assetHelper.retainPackages(
            project.getDirectory(),
            oldPackagesDirectory.getParent().resolve("new"));
        LOG.info("EAK version {} files stored at {}", settings.getCurrentEakVersion(), newPackagesDirectory);

        return ComparisonUtil.isMatch(oldPackagesDirectory, newPackagesDirectory, settings.getFilters(project));
    }

    private boolean doRegressionWithEqualVersions(ProjectInfo project) {
        Path repoDirectory = assetHelper.createTempRoot("repo");
        String fakeVersion = settings.getCurrentEakVersion().replace("-SNAPSHOT", ".old-SNAPSHOT");

        LOG.info("Retrieving former EAK assembly of version {}", settings.getCurrentEakVersion());
        List<ArtifactInfo> activeArtifacts = getInstallableArtifacts(repoDirectory);
        Assert.assertFalse("Could not download any artifacts", activeArtifacts.isEmpty());

        LOG.info("Installing former EAK assembly files to local repository");
        installArtifacts(activeArtifacts, repoDirectory, fakeVersion);

        try {
            LOG.info("Running regression build with the former EAK assembly");

            Commandline buildCommand = ShellHelper.getBuildCommand(settings, project, fakeVersion);
            LOG.info(SHELL_COMMAND_FORMAT, buildCommand.getWorkingDirectory(), buildCommand);
            int exitCode = runCommand(buildCommand);
            Assert.assertEquals(
                String.format(OPERATION_FAILED, "Regression build with the former EAK assembly"),
                0,
                exitCode);

            Path oldPackagesDirectory = assetHelper.retainPackages(project.getDirectory(), "old");
            LOG.info("Former EAK assembly package files stored at {}", oldPackagesDirectory);

            LOG.info("Running regression build with the current EAK assembly");

            buildCommand = ShellHelper.getBuildCommand(settings, project, settings.getCurrentEakVersion());
            LOG.info(SHELL_COMMAND_FORMAT, buildCommand.getWorkingDirectory(), buildCommand);
            exitCode = runCommand(buildCommand);
            Assert.assertEquals(
                String.format(OPERATION_FAILED, "Regression build with the current EAK assembly"),
                0,
                exitCode);

            Path newPackagesDirectory = assetHelper.retainPackages(
                project.getDirectory(),
                oldPackagesDirectory.getParent().resolve("new"));
            LOG.info("Current EAK assembly package files stored at {}", newPackagesDirectory);

            return ComparisonUtil.isMatch(oldPackagesDirectory, newPackagesDirectory, settings.getFilters(project));

        } finally {
            LOG.info("Purging former EAK assembly from the local repository");
            uninstallArtifacts(activeArtifacts);
        }
    }

    private List<ArtifactInfo> getInstallableArtifacts(Path repoDirectory) {
        List<ArtifactInfo> result = new ArrayList<>();
        for (ArtifactInfo artifact : settings.getEakArtifacts()) {
            if (!artifact.isDownloadable()) {
                continue;
            }
            Commandline downloadCommand = ShellHelper.getDownloadCommand(
                settings,
                artifact.getFullName(artifact.getFileExtension()),
                repoDirectory);
            LOG.info(SHELL_COMMAND_FORMAT, downloadCommand.getWorkingDirectory(), downloadCommand);
            int exitCode = runCommand(downloadCommand);
            if (!ArtifactInfo.PACKAGING_POM.equals(artifact.getPackaging())) {
                Commandline downloadPomCommand = ShellHelper.getDownloadCommand(
                    settings,
                    artifact.getFullName(ArtifactInfo.PACKAGING_POM),
                    repoDirectory);
                LOG.info(SHELL_COMMAND_FORMAT, downloadPomCommand.getWorkingDirectory(), downloadCommand);
                exitCode += runCommand(downloadPomCommand);
            }
            if (exitCode == 0) {
                artifact.resolveFrom(repoDirectory);
            }
            if (artifact.getPath() != null && artifact.getPomPath() != null) {
                result.add(artifact);
            }
        }
        return result;
    }

    private void installArtifacts(List<ArtifactInfo> artifacts, Path repoDirectory, String version) {
        int exitCode;
        for (ArtifactInfo artifact : artifacts) {
            try {
                VersionUpdateHelper.update(artifact, version);
            } catch (IOException e) {
                throw new AssertionError("Could not update " + artifact.getPath(), e);
            }
            Commandline installCommand = ShellHelper.getInstallCommand(settings, artifact, repoDirectory);
            LOG.info(SHELL_COMMAND_FORMAT, installCommand.getWorkingDirectory(), installCommand);
            exitCode = runCommand(installCommand);
            Assert.assertEquals(String.format(OPERATION_FAILED, "Installation of " + artifact), 0, exitCode);
        }
    }

    private void uninstallArtifacts(List<ArtifactInfo> artifacts) {
        Commandline purgeCommand = ShellHelper.getPurgeCommand(settings, artifacts);
        LOG.info(SHELL_COMMAND_FORMAT, purgeCommand.getWorkingDirectory(), purgeCommand);
        int exitCode = runCommand(purgeCommand);
        Assert.assertEquals(String.format(OPERATION_FAILED,"Purge of the former EAK assembly"),0, exitCode);
    }

    private static int runCommand(Commandline command) {
        try {
            return CommandLineUtils.executeCommandLine(
                command,
                RegressionTest::relayLogLine,
                RegressionTest::relayLogLine);
        } catch (CommandLineException e) {
            throw new AssertionError(e.getMessage());
        }
    }

    private static void relayLogLine(String line) {
        Matcher matcher = PATTERN_LOG_LEVEL.matcher(StringUtils.defaultString(line));
        if (!matcher.find() && StringUtils.isNotBlank(line)) {
            LOG.info(line.trim());
            return;
        } else if (StringUtils.isBlank(line)) {
            return;
        }
        String effectiveLine = StringUtils.substring(line, matcher.end());
        switch (matcher.group(1)) {
            case "ERROR":
                LOG.error(effectiveLine);
                break;
            case "WARN":
                LOG.warn(effectiveLine);
                break;
            default:
                LOG.info(effectiveLine);
        }
    }
}
