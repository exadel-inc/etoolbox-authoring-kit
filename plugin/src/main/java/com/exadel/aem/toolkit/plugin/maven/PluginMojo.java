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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import com.exadel.aem.toolkit.plugin.exceptions.PluginException;
import com.exadel.aem.toolkit.plugin.writers.PackageWriter;

/**
 * Represents the entry-point of the EToolbox Authoring Kit (the ToolKit) Maven plugin execution
 */
@Mojo(name = "aem-authoring", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyCollection = ResolutionScope.COMPILE)
@SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection"})
public class PluginMojo extends AbstractMojo {
    private static final String DEPENDENCY_RESOLUTION_EXCEPTION_MESSAGE = "Could not resolve dependencies of project %s: %s";
    private static final String PLUGIN_EXECUTION_EXCEPTION_MESSAGE = "%s in module %s: %s";

    @Parameter(readonly = true, defaultValue = "${project}")
    private MavenProject project;

    @Parameter(readonly = true, defaultValue = "${plugin.artifacts}")
    private List<Artifact> pluginDependencies;

    @Parameter(readonly = true)
    private String componentsPathBase;

    @Parameter(readonly = true)
    private String componentsReferenceBase;

    @Parameter(readonly = true, defaultValue = "java.io.IOException")
    private String terminateOn;

    /**
     * Executes the ToolKit Maven plugin. This is done by initializing {@link PluginRuntime} and then
     * enumerating classpath entries present in the Maven reactor. Relevant AEM component classes (POJOs or Sling models)
     * are extracted and processed with {@link PackageWriter} instance created for a particular Maven project; the result
     * is written down to the AEM package zip file. The method is run once for each package module that has the ToolKit
     * plugin included in the POM file
     * @throws MojoExecutionException if work on a package cannot proceed (due to e.g. file system failure or improper
     * initialization) or in case an internal exception is thrown that corresponds to the {@code terminateOn} setting
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

        PluginRuntime.contextBuilder()
            .classPathElements(classpathElements)
            .packageBase(componentsReferenceBase)
            .terminateOn(terminateOn)
            .build();

        try (PackageWriter packageWriter = PackageWriter.forMavenProject(project, componentsPathBase)) {
            packageWriter.writeInfo(PluginInfo.getInstance());
            PluginRuntime.context().getReflection().getComponentClasses().forEach(packageWriter::write);
        } catch (PluginException e) {
            throw new MojoExecutionException(String.format(PLUGIN_EXECUTION_EXCEPTION_MESSAGE,
                    e.getCause() != null ? e.getCause().getClass().getSimpleName() : e.getClass().getSimpleName(),
                    project.getBuild().getFinalName(),
                    e.getMessage()), e);
        }

        PluginRuntime.close();
    }
}
