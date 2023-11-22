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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.ConfigurationContainer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.toolchain.ToolchainManager;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.exceptions.PluginException;
import com.exadel.aem.toolkit.plugin.sources.ComponentSource;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;
import com.exadel.aem.toolkit.plugin.utils.ToolchainUtil;
import com.exadel.aem.toolkit.plugin.writers.PackageWriter;

/**
 * Represents the entry point of the EToolbox Authoring Kit (the ToolKit) Maven plugin execution
 */
@Mojo(
    name = PluginMojo.PLUGIN_GOAL,
    defaultPhase = LifecyclePhase.PACKAGE,
    requiresDependencyCollection = ResolutionScope.COMPILE
)
@SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection"})
public class PluginMojo extends AbstractMojo {
    private static final Logger LOG = LoggerFactory.getLogger(DialogConstants.ARTIFACT_NAME);

    static final String PLUGIN_GOAL = "aem-authoring";
    private static final String PLUGIN_ARTIFACT_ID = "etoolbox-authoring-kit-plugin";
    private static final String PLUGIN_GROUP = "com.exadel.etoolbox";

    private static final String MAVEN_EXECUTABLE = "mvn";

    private static final String PROJECT_TYPE_PACKAGE = "content-package";

    private static final String CONFIG_KEY_CLASSPATH_ELEMENTS = "classpathElements";
    private static final String CONFIG_KEY_PATH_BASE = "componentsPathBase";
    private static final String CONFIG_KEY_REFERENCE_BASE = "componentsReferenceBase";
    private static final String CONFIG_KEY_TERMINATE_ON = "terminateOn";

    private static final String DEPENDENCY_RESOLUTION_EXCEPTION_MESSAGE = "Could not resolve dependencies of project %s: %s";
    private static final String PLUGIN_EXECUTION_EXCEPTION_MESSAGE = "%s in module %s: %s";
    private static final String PLUGIN_COMPLETION_MESSAGE = "Execution completed.";
    private static final String PLUGIN_COMPLETION_STATISTICS_MESSAGE = PLUGIN_COMPLETION_MESSAGE + " {} component(-s) processed.";

    private static final String ARGUMENT_FORMAT = "-D%s=%s";

    private static final String PATTERN_COLOR_CODE = "[^A-Za-z0-0]\\[[0-9;]*m";
    private static final String PATTERN_LOG_LEVEL = "^\\s*\\[[A-Z]+]\\s+";
    private static final Pattern PATTERN_SPLITTER = Pattern.compile(CoreConstants.SEPARATOR_COMMA);

    @Component
    private ToolchainManager toolchainManager;

    // Automatic parameters

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    @Parameter(defaultValue = "${plugin.artifacts}", readonly = true)
    private List<Artifact> pluginDependencies;

    // Customizable parameters

    @Parameter(property = "classpathElements", readonly = true)
    private String classpathElements;

    @Parameter(property = "componentsPathBase", readonly = true)
    private String componentsPathBase;

    @Parameter(property = "componentsReferenceBase", readonly = true)
    private String componentsReferenceBase;

    @Parameter(defaultValue = "java.io.IOException", property = "terminateOn", readonly = true)
    private String terminateOn;

    /**
     * Executes the ToolKit Maven plugin. This is done by initializing {@link PluginRuntime} and then enumerating
     * classpath entries present in the Maven reactor. Relevant AEM component classes (POJOs or Sling models) are
     * extracted and processed with {@link PackageWriter} instance created for a particular Maven project; the result is
     * written down to the AEM package zip file. The method is run once for each package module that has the ToolKit
     * plugin included in the POM file
     * @throws MojoExecutionException if work on a package cannot proceed (due to, e.g., file system failure or improper
     *                                initialization) or in case an internal exception is thrown that corresponds to the
     *                                {@code terminateOn} setting
     */
    public void execute() throws MojoExecutionException {
        if (ToolchainUtil.shouldReload(toolchainManager, session)) {
            String javaHome = ToolchainUtil.getJavaHome(toolchainManager, session);
            LOG.info("Another JVM version is required. Will use {}", javaHome);
            fork(javaHome);
            return;
        }

        PluginSettings.Builder settingsBuilder = PluginSettings.builder()
            .terminateOn(terminateOn)
            .defaultPathBase(componentsPathBase);
        populateReferenceEntries(settingsBuilder);
        PluginSettings pluginSettings = settingsBuilder.build();

        PluginRuntime.contextBuilder()
            .classPathElements(getClasspathElements())
            .settings(pluginSettings)
            .build();

        int processedCount = 0;
        try (PackageWriter packageWriter = PackageWriter.forMavenProject(project)) {
            packageWriter.writeInfo(PluginInfo.getInstance());
            for (ComponentSource component : PluginRuntime.context().getReflection().getComponents(componentsReferenceBase)) {
                processedCount += packageWriter.write(component) ? 1 : 0;
            }
        } catch (PluginException e) {
            throw new MojoExecutionException(String.format(PLUGIN_EXECUTION_EXCEPTION_MESSAGE,
                e.getCause() != null ? e.getCause().getClass().getSimpleName() : e.getClass().getSimpleName(),
                project.getBuild().getFinalName(),
                e.getMessage()), e);
        }

        PluginRuntime.close();

        if (processedCount > 0) {
            LOG.info(PLUGIN_COMPLETION_STATISTICS_MESSAGE, processedCount);
        } else {
            LOG.info(PLUGIN_COMPLETION_MESSAGE);
        }
    }

    /**
     * Restarts the ToolKit's plugin execution with the JDK specified in the current Maven toolchain
     * @param jvmPath String value representing the path to the JDK
     * @throws MojoExecutionException if the plugin process cannot be restarted due to, e.g., missing dependencies
     */
    private void fork(String jvmPath) throws MojoExecutionException {
        Commandline commandline = new Commandline();
        commandline.setExecutable(MAVEN_EXECUTABLE);
        commandline.addEnvironment(DialogConstants.PN_JAVA_HOME, jvmPath);
        commandline.addArguments(new String[] {
            "--batch-mode",
            String.join(CoreConstants.SEPARATOR_COLON, PLUGIN_GROUP, PLUGIN_ARTIFACT_ID, PLUGIN_GOAL),
            String.format(ARGUMENT_FORMAT, CONFIG_KEY_CLASSPATH_ELEMENTS, String.join(CoreConstants.SEPARATOR_COMMA, getClasspathElements())),
            String.format(ARGUMENT_FORMAT, CONFIG_KEY_PATH_BASE, componentsPathBase),
            String.format(ARGUMENT_FORMAT, CONFIG_KEY_REFERENCE_BASE, componentsReferenceBase),
            String.format(ARGUMENT_FORMAT, CONFIG_KEY_TERMINATE_ON, terminateOn)
        });
        commandline.setWorkingDirectory(project.getFile().getParentFile());
        LOG.info("Restarting with {}", commandline);
        try {
            CommandLineUtils.executeCommandLine(
                commandline,
                line -> relayLogLine(LOG::info, line),
                line -> relayLogLine(LOG::error, line));
        } catch (CommandLineException e) {
            throw new MojoExecutionException("Could not restart plugin process", e);
        }
    }

    /**
     * Retrieves the list of classpath elements for the current Maven project
     * @return {@code List} of {@code String} values
     * @throws MojoExecutionException if required dependencies cannot be resolved
     */
    private List<String> getClasspathElements() throws MojoExecutionException {
        Set<String> result;
        try {
            result = new HashSet<>(project.getCompileClasspathElements());
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException(String.format(DEPENDENCY_RESOLUTION_EXCEPTION_MESSAGE,
                project.getBuild().getFinalName(),
                e.getMessage()), e);
        }
        if (StringUtils.isNotBlank(this.classpathElements)) {
            PATTERN_SPLITTER
                .splitAsStream(this.classpathElements)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .forEach(result::add);
        }
        pluginDependencies.stream().findFirst().ifPresent(d -> result.add(d.getFile().getPath()));
        return new ArrayList<>(result);
    }

    /**
     * Scans the module structure of the current Maven installation to retrieve the ToolKit's plugin configurations and
     * stores the matches between AEM component Java packages and repository paths. The references are passed to the
     * {@link PluginSettings} builder
     * @param builder {@link PluginSettings.Builder} instance
     */
    private void populateReferenceEntries(PluginSettings.Builder builder) {
        List<MavenProject> contentPackages = session
            .getProjectDependencyGraph()
            .getAllProjects()
            .stream()
            .filter(p -> StringUtils.equals(p.getPackaging(), PROJECT_TYPE_PACKAGE))
            .collect(Collectors.toList());

        for (MavenProject contentPackage : contentPackages) {
            Xpp3Dom pluginConfig = contentPackage
                .getBuildPlugins()
                .stream()
                .filter(plugin -> PLUGIN_ARTIFACT_ID.equals(plugin.getArtifactId()))
                .map(ConfigurationContainer::getConfiguration)
                .map(Xpp3Dom.class::cast)
                .findFirst()
                .orElse(null);
            if (pluginConfig == null) {
                continue;
            }
            String pathBase = Optional.ofNullable(pluginConfig.getChild(CONFIG_KEY_PATH_BASE))
                .map(Xpp3Dom::getValue)
                .orElse(null);
            String referenceBase = Optional.ofNullable(pluginConfig.getChild(CONFIG_KEY_REFERENCE_BASE))
                .map(Xpp3Dom::getValue)
                .orElse(null);
            builder.referenceEntry(pathBase, referenceBase);
        }
    }

    /**
     * Transfers to the main logger a line retrieved from the secondary Maven process
     * @param logger A logging method such as {@code .info()} or {@code .error()}
     * @param line   A string value representing the line to log
     */
    private static void relayLogLine(Consumer<String> logger, String line) {
        String effectiveLine = RegExUtils.removePattern(line, PATTERN_LOG_LEVEL);
        effectiveLine = RegExUtils.removePattern(effectiveLine, PATTERN_COLOR_CODE);
        if (StringUtils.isEmpty(effectiveLine)) {
            return;
        }
        logger.accept(effectiveLine.trim());
    }
}

