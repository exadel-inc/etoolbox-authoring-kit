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
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Assert;

import com.exadel.aem.toolkit.core.CoreConstants;

class ProjectSettings {

    private static final String PROPERTY_GROUP = "eak.group";
    private static final String PROPERTY_HOME = "user.home";
    private static final String PROPERTY_MAVEN_CMD = "maven.cmd";
    private static final String PROPERTY_MAVEN_DIR = "maven.dir";
    private static final String PROPERTY_MODULES = "modules";
    private static final String PROPERTY_NO_CLEANUP = "nocleanup";
    private static final String PROPERTY_PROJECT = "project";
    private static final String PROPERTY_VERSION = "eak.version";
    private static final String PROPERTY_VERSION_PROP = "eak.version.prop";

    private static final String DEFAULT_GROUP = "com.exadel.etoolbox";
    private static final String DEFAULT_MAVEN_CMD = "mvn";
    private static final String DEFAULT_VERSION_PROP = PROPERTY_VERSION;

    private final String currentVersion;

    private final File directory;

    private final String mavenExecutable;

    private final String mavenDirectory;

    private File pomFile;

    private final String project;

    private String projectVersion;

    private final String modules;

    private final boolean noCleanUp;

    private final String versionProperty;

    ProjectSettings() {
        currentVersion = System.getProperty(PROPERTY_VERSION);
        project = System.getProperty(PROPERTY_PROJECT);
        directory = Paths.get(StringUtils.EMPTY).resolve(StringUtils.defaultString(project)).toFile();
        mavenExecutable = System.getProperty(PROPERTY_MAVEN_CMD, DEFAULT_MAVEN_CMD);
        mavenDirectory = System.getProperty(PROPERTY_MAVEN_DIR, System.getProperty(PROPERTY_HOME, StringUtils.EMPTY));
        modules = System.getProperty(PROPERTY_MODULES);
        noCleanUp = Boolean.parseBoolean(System.getProperty(PROPERTY_NO_CLEANUP, StringUtils.EMPTY));
        versionProperty = System.getProperty(PROPERTY_VERSION_PROP, DEFAULT_VERSION_PROP);
    }

    boolean cleanUp() {
        return !noCleanUp;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    Path getMavenDirectory() {
        return Paths.get(
            mavenDirectory,
            ".m2/repository",
            StringUtils.replace(
                System.getProperty(PROPERTY_GROUP, DEFAULT_GROUP),
                CoreConstants.SEPARATOR_DOT, CoreConstants.SEPARATOR_SLASH));
    }

    String getMavenExecutable() {
        return mavenExecutable;
    }

    String getModules() {
        return modules;
    }

    private File getPomFile() {
        if (pomFile == null) {
            pomFile = Paths.get(directory.getAbsolutePath()).resolve("pom.xml").toFile();
        }
        return pomFile;
    }

    File getProjectDirectory() {
        return directory;
    }

    String getProjectVersion() {
        if (projectVersion != null) {
            return projectVersion;
        }
        try (InputStream input = getPomFile().toURI().toURL().openStream()) {
            Model model = new MavenXpp3Reader().read(input);
            model.setPomFile(getPomFile());
            MavenProject mavenProject = new MavenProject(model);
            projectVersion = mavenProject.getProperties().getProperty(versionProperty, StringUtils.EMPTY);
        } catch (IOException | XmlPullParserException e) {
            RegressionTest.LOG.error("Could not read project model at {}", getPomFile().getAbsolutePath(), e);
            projectVersion = StringUtils.EMPTY;
        }
        return projectVersion;
    }

    String getVersionProperty() {
        return versionProperty;
    }

    void validate() {
        Assert.assertNotNull("Could not retrieve current version", currentVersion);
        Assert.assertTrue("Maven executable is invalid", StringUtils.isNotBlank(mavenExecutable));
        Assert.assertTrue("Target project is not specified", StringUtils.isNotEmpty(project));
        Assert.assertTrue("POM file is missing or invalid", getPomFile().exists() && getPomFile().isFile());
        Assert.assertTrue("Version property is not specified", StringUtils.isNotBlank(versionProperty));
    }
}
