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

    private static final String SHELL_COMMAND_LOG_FORMAT = "{}> {}";

    private static final String[] ALLOWED_PACKAGING_TYPES = {"pom", "jar", "maven-plugin"};

    private static final Pattern PATTERN_LOG_LEVEL = Pattern.compile("^\\s*\\[([A-Z]+)]\\s+");

    private ProjectSettings settings;
    private RegressionAssetHelper assetHelper;

    @Before
    public void setUp() {
        settings = new ProjectSettings();
        assetHelper = new RegressionAssetHelper();
    }

    @Test
    public void doRegression() {
        settings.validate();

        boolean shouldReplaceSnapshot = StringUtils.equals(settings.getEakVersion(), settings.getProjectVersion())
            && StringUtils.contains(settings.getEakVersion(), "-SNAPSHOT");

        boolean result = shouldReplaceSnapshot ? doRegressionWithEqualVersions() : doRegressionWithDifferingVersions();
        if (settings.cleanUp()) {
            LOG.info("Cleaning up regression content");
            assetHelper.cleanUp();
        } else {
            LOG.info("Regression content is stored at {}", assetHelper.getTempRoot());
        }
        Assert.assertTrue("There were differences in regression content. See log above", result);
    }

    private boolean doRegressionWithDifferingVersions() {
        Commandline commandLine = ShellHelper.getBuildCommand(settings);

        LOG.info("Running regression build with EAK version {}", settings.getProjectVersion());
        LOG.info(SHELL_COMMAND_LOG_FORMAT, settings.getProjectDirectory(), commandLine);
        int exitCode = runCommand(commandLine);
        Assert.assertEquals("Regression build with EAK version " + settings.getProjectVersion() + " failed with code " + exitCode,
            0,
            exitCode);

        Path oldPackagesDirectory = assetHelper.retainPackages(settings.getProjectDirectory(), "old");
        LOG.info("EAK version {} files stored at {}", settings.getProjectVersion(), oldPackagesDirectory);

        LOG.info("Running regression build with EAK version {}", settings.getEakVersion());
        commandLine = ShellHelper.getBuildCommand(settings);
        commandLine.addArguments(new String[] {
            String.format(ShellHelper.MAVEN_PROP_FORMAT, settings.getVersionProperty(), settings.getEakVersion())
        });
        LOG.info(SHELL_COMMAND_LOG_FORMAT, settings.getProjectDirectory(), commandLine);
        exitCode = runCommand(commandLine);
        Assert.assertEquals("Regression build with EAK version " + settings.getEakVersion() + " failed with code " + exitCode,
            0,
            exitCode);

        Path newPackagesDirectory = assetHelper.retainPackages(
            settings.getProjectDirectory(),
            oldPackagesDirectory.getParent().resolve("new"));
        LOG.info("EAK version {} files stored at {}", settings.getEakVersion(), newPackagesDirectory);

        return ComparisonUtil.isMatch(oldPackagesDirectory, newPackagesDirectory, settings.getFilters());
    }

    private boolean doRegressionWithEqualVersions() {
        int exitCode;

        Path repoDirectory = assetHelper.createTempRoot("repo");

        LOG.info("Retrieving former EAK assembly of version {}", settings.getEakVersion());
        List<ArtifactInfo> activeArtifacts = getInstallableArtifacts(repoDirectory);
        Assert.assertFalse("Could not download any artifacts", activeArtifacts.isEmpty());

        LOG.info("Installing former EAK assembly files to local repository");
        installArtifacts(activeArtifacts, repoDirectory);

        LOG.info("Running regression build with the former EAK assembly");
        Commandline buildCommandLine = ShellHelper.getBuildCommand(settings);
        buildCommandLine.addArguments(new String[] {
            String.format(ShellHelper.MAVEN_PROP_FORMAT, settings.getVersionProperty(), ShellHelper.MAVEN_SNAPSHOT_VERSION)
        });
        LOG.info(SHELL_COMMAND_LOG_FORMAT, settings.getProjectDirectory(), buildCommandLine);
        exitCode = runCommand(buildCommandLine);
        Assert.assertEquals(
            "Regression build with the former EAK assembly failed with code " + exitCode,
            0,
            exitCode);
        Path oldPackagesDirectory = assetHelper.retainPackages(settings.getProjectDirectory(), "old");
        LOG.info("Former EAK assembly package files stored at {}", oldPackagesDirectory);

        LOG.info("Running regression build with the current EAK assembly");
        buildCommandLine = ShellHelper.getBuildCommand(settings);
        buildCommandLine.addArguments(new String[] {
            String.format(ShellHelper.MAVEN_PROP_FORMAT, settings.getVersionProperty(), settings.getEakVersion())
        });
        LOG.info(SHELL_COMMAND_LOG_FORMAT, settings.getProjectDirectory(), buildCommandLine);
        exitCode = runCommand(buildCommandLine);
        Assert.assertEquals(
            "Regression build with the current EAK assembly exited with code " + exitCode,
            0,
            exitCode);
        Path newPackagesDirectory = assetHelper.retainPackages(
            settings.getProjectDirectory(),
            oldPackagesDirectory.getParent().resolve("new"));
        LOG.info("Current EAK assembly package files stored at {}", newPackagesDirectory);

        boolean result = ComparisonUtil.isMatch(oldPackagesDirectory, newPackagesDirectory, settings.getFilters());
        if (settings.cleanUp()) {
            LOG.info("Purging former EAK assembly from the local repository");
            uninstallArtifacts(activeArtifacts, repoDirectory);
        }
        return result;
    }

    private List<ArtifactInfo> getInstallableArtifacts(Path repoDirectory) {
        List<ArtifactInfo> result = new ArrayList<>();
        for (ArtifactInfo artifact : settings.getEakArtifacts()) {
            if (!StringUtils.equalsAny(artifact.getPackaging(), ALLOWED_PACKAGING_TYPES)
                || artifact.getId().contains("tests")) {
                continue;
            }
            String artifactId = artifact.toString();
            if ("pom".equals(artifact.getPackaging())) {
                artifactId += ":pom";
            }
            Commandline downloadCommandLine = ShellHelper.getDownloadCommand(settings, artifactId, repoDirectory);
            LOG.info(SHELL_COMMAND_LOG_FORMAT, repoDirectory, downloadCommandLine);
            int exitCode = runCommand(downloadCommandLine);
            if (exitCode == 0) {
                artifact.resolveFrom(repoDirectory);
            }
            if (artifact.getPath() != null) {
                result.add(artifact);
            }
        }
        return result;
    }

    private void installArtifacts(List<ArtifactInfo> activeArtifacts, Path repoDirectory) {
        int exitCode;
        for (ArtifactInfo artifact : activeArtifacts) {
            Commandline installCommandLine = ShellHelper.getInstallCommand(settings, artifact, repoDirectory);
            LOG.info(SHELL_COMMAND_LOG_FORMAT, repoDirectory, installCommandLine);
            exitCode = runCommand(installCommandLine);
            Assert.assertEquals(
                "Installation of " + artifact + " failed with code " + exitCode,
                0,
                exitCode);
        }
    }

    private void uninstallArtifacts(List<ArtifactInfo> artifacts, Path repoDirectory) {
        Commandline purgeCommandLine = ShellHelper.getPurgeCommand(settings, artifacts);
        LOG.info(SHELL_COMMAND_LOG_FORMAT, repoDirectory, purgeCommandLine);
        int exitCode = runCommand(purgeCommandLine);
        Assert.assertEquals(
            "Purging of the former EAK assembly failed with code " + exitCode,
            0,
            exitCode);
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
