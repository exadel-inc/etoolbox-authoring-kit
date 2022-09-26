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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.ConfigurationContainer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.plugin.exceptions.PluginException;
import com.exadel.aem.toolkit.plugin.sources.ComponentSource;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;
import com.exadel.aem.toolkit.plugin.writers.PackageWriter;

/**
 * Represents the entry-point of the EToolbox Authoring Kit (the ToolKit) Maven plugin execution
 */
@Mojo(name = "aem-authoring", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyCollection = ResolutionScope.COMPILE)
@SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection"})
public class PluginMojo extends AbstractMojo {
    private static final Logger LOG = LoggerFactory.getLogger(DialogConstants.ARTIFACT_NAME);

    private static final String PLUGIN_ARTIFACT_ID = "etoolbox-authoring-kit-plugin";
    private static final String PROJECT_TYPE_PACKAGE = "content-package";
    private static final String CONFIG_KEY_PATH_BASE = "componentsPathBase";
    private static final String CONFIG_KEY_REFERENCE_BASE = "componentsReferenceBase";

    private static final String DEPENDENCY_RESOLUTION_EXCEPTION_MESSAGE = "Could not resolve dependencies of project %s: %s";
    private static final String PLUGIN_EXECUTION_EXCEPTION_MESSAGE = "%s in module %s: %s";
    private static final String PLUGIN_COMPLETION_MESSAGE = "Execution completed.";
    private static final String PLUGIN_COMPLETION_STATISTICS_MESSAGE = PLUGIN_COMPLETION_MESSAGE + " {} component(-s) processed.";

    @Parameter(readonly = true, defaultValue = "${project}")
    private MavenProject project;

    @Parameter(defaultValue = "${session}", required = true, readonly = true)
    private MavenSession session;

    @Parameter(readonly = true, defaultValue = "${plugin.artifacts}")
    private List<Artifact> pluginDependencies;

    @Parameter(readonly = true)
    private String componentsPathBase;

    @Parameter(readonly = true)
    private String componentsReferenceBase;

    @Parameter(readonly = true, defaultValue = "java.io.IOException")
    private String terminateOn;

    /**
     * Executes the ToolKit Maven plugin. This is done by initializing {@link PluginRuntime} and then enumerating
     * classpath entries present in the Maven reactor. Relevant AEM component classes (POJOs or Sling models) are
     * extracted and processed with {@link PackageWriter} instance created for a particular Maven project; the result is
     * written down to the AEM package zip file. The method is run once for each package module that has the ToolKit
     * plugin included in the POM file
     * @throws MojoExecutionException if work on a package cannot proceed (due to e.g. file system failure or improper
     *                                initialization) or in case an internal exception is thrown that corresponds to the
     *                                {@code terminateOn} setting
     */
    public void execute() throws MojoExecutionException {
        List<String> classpathElements;
        try {
            classpathElements = project.getCompileClasspathElements();
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException(String.format(DEPENDENCY_RESOLUTION_EXCEPTION_MESSAGE,
                project.getBuild().getFinalName(),
                e.getMessage()), e);
        }
        pluginDependencies.stream().findFirst().ifPresent(d -> classpathElements.add(d.getFile().getPath()));

        PluginSettings.Builder settingsBuilder = PluginSettings.builder()
            .terminateOn(terminateOn)
            .defaultPathBase(componentsPathBase);
        populateReferenceEntries(settingsBuilder);

        PluginRuntime.contextBuilder()
            .classPathElements(classpathElements)
            .settings(settingsBuilder.build())
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
     * Scans the module structure of the current Maven installation to retrieve the ToolKit's plugin configurations and
     * store the matches between AEM component Java packages and repository paths. The references are passed to the
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
}
