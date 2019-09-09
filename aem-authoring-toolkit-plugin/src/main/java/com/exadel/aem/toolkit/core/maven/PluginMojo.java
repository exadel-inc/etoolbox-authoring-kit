/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package com.exadel.aem.toolkit.core.maven;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import com.exadel.aem.toolkit.core.exceptions.PluginException;
import com.exadel.aem.toolkit.core.util.PackageWriter;

@Mojo(name = "aem-authoring", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyCollection = ResolutionScope.COMPILE)
@SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection"})
public class PluginMojo extends AbstractMojo {
    private static final String DEPENDENCY_RESOLUTION_EXCEPTION_MESSAGE = "AEM Authoring Toolkit could not resolve dependencies of project ";
    private static final String PLUGIN_EXECUTION_EXCEPTION_MESSAGE = "AEM Authoring Toolkit terminated due to an exception in project ";

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

    public void execute() throws MojoExecutionException {
        List<String> classpathElements;
        try {
            classpathElements = project.getCompileClasspathElements().stream()
                    .filter(element -> StringUtils.isBlank(componentsReferenceBase) || element.startsWith(componentsReferenceBase))
                    .collect(Collectors.toList());
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException(DEPENDENCY_RESOLUTION_EXCEPTION_MESSAGE + project.getBuild().getFinalName(), e);
        }
        pluginDependencies.stream().findFirst().ifPresent(d -> classpathElements.add(d.getFile().getPath()));

        PluginRuntime.initialize(classpathElements, terminateOn);

        try (PackageWriter packageWriter = PackageWriter.forMavenProject(project, componentsPathBase)) {
            PluginRuntime.context().getReflectionUtility().getComponentClasses().forEach(packageWriter::write);
        } catch (PluginException e) {
            throw new MojoExecutionException(PLUGIN_EXECUTION_EXCEPTION_MESSAGE + project.getBuild().getFinalName(), e);
        }

        PluginRuntime.close();
    }
}
