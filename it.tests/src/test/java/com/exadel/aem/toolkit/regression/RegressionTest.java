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

import org.apache.commons.lang3.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.WriterStreamConsumer;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegressionTest {

    static final Logger LOG = LoggerFactory.getLogger(RegressionTest.class);

    private static final String CUSTOM_PROP_FORMAT = "-D%s=%s";

    @Test
    public void doRegression() {

        ProjectSettings settings = new ProjectSettings();
        settings.validate();

        RegressionAssetHelper assetHelper = new RegressionAssetHelper();
        boolean shouldReplaceSnapshot = StringUtils.equals(settings.getCurrentVersion(), settings.getProjectVersion())
            && StringUtils.contains(settings.getCurrentVersion(), "-SNAPSHOT");

        if (shouldReplaceSnapshot) {
            LOG.info("Backing up current snapshot");
            assetHelper.retainArtifacts(settings);
            assetHelper.restoreArtifacts(settings);
        }

        LOG.info("Running pre-change regression build");
        Commandline commandLine = getCommandLine(settings);
        int exitCode = runBuild(commandLine);
        Assert.assertEquals("Pre-change regression build exited with code " + exitCode, 0, exitCode);

        Path oldPackagesDirectory = assetHelper.retainPackages(settings.getProjectDirectory(), "old");
        LOG.info("Pre-change package files stored at {}", oldPackagesDirectory);

        if (shouldReplaceSnapshot) {
            LOG.info("Restoring current snapshot");
            assetHelper.restoreArtifacts(settings);
        }

        LOG.info("Running post-change build");
        commandLine.addArguments(new String[] {String.format(
            CUSTOM_PROP_FORMAT,
            settings.getVersionProperty(),
            settings.getCurrentVersion())});
        exitCode = runBuild(commandLine);
        Assert.assertEquals("Post-change regression build exited with code " + exitCode, 0, exitCode);

        Path newPackagesDirectory = assetHelper.retainPackages(
            settings.getProjectDirectory(),
            oldPackagesDirectory.getParent().resolve("new"));
        LOG.info("Post-change package files stored at {}", newPackagesDirectory);

        boolean result = ComparisonUtil.isMatch(oldPackagesDirectory, newPackagesDirectory);

        if (settings.cleanUp()) {
            assetHelper.cleanUp();
        }
        Assert.assertTrue("There were differences in regression content. See log above", result);
    }

    private static Commandline getCommandLine(ProjectSettings settings) {
        Commandline commandLine = new Commandline(settings.getMavenExecutable());
        commandLine.addArguments(new String[] {"package", "-Dmaven.test.skip=true"});
        if (StringUtils.isNotBlank(settings.getModules())) {
            commandLine.addArguments(new String[] {"-pl", settings.getModules()});
        }
        commandLine.setWorkingDirectory(settings.getProjectDirectory());
        return commandLine;
    }

    private static int runBuild(Commandline commandLine) {
        try (LogWriter sysOutWriter = new LogWriter(false);
             LogWriter sysErrWriter = new LogWriter(true)) {
            WriterStreamConsumer systemOut = new WriterStreamConsumer(sysOutWriter);
            WriterStreamConsumer systemErr = new WriterStreamConsumer(sysErrWriter);
            return CommandLineUtils.executeCommandLine(commandLine, systemOut, systemErr);
        } catch (IOException | CommandLineException e) {
            throw new AssertionError(e.getMessage());
        }
    }
}
