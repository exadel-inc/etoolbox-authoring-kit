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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.RepositoryBase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.core.CoreConstants;

class RegressionSettings {

    private static final Logger LOG = LoggerFactory.getLogger(RegressionSettings.class);

    static final String POM_FILE = "pom.xml";

    private static final String PROPERTY_FILTERS = "filters";
    private static final String PROPERTY_MAVEN_CMD = "maven.cmd";
    private static final String PROPERTY_NO_CLEANUP = "nocleanup";
    private static final String PROPERTY_PROJECT = "project";
    private static final String PROPERTY_PROJECTS = "projects";
    static final String PROPERTY_VERSION = "eak.version";

    private static final String DEFAULT_FILTERS = "eak.regression/filters";
    private static final String DEFAULT_MAVEN_CMD = "mvn";

    private List<ArtifactInfo> eakArtifacts;

    private String eakVersion;

    private final String filtersPath;

    private List<String> filters;

    private final String mavenExecutable;

    private Model mavenModel;

    private final boolean noCleanUp;

    private final List<ProjectInfo> projects;

    RegressionSettings() {
        eakVersion = System.getProperty(PROPERTY_VERSION);
        filtersPath = System.getProperty(PROPERTY_FILTERS);
        mavenExecutable = System.getProperty(PROPERTY_MAVEN_CMD, DEFAULT_MAVEN_CMD);
        noCleanUp = Boolean.parseBoolean(System.getProperty(PROPERTY_NO_CLEANUP, StringUtils.EMPTY));
        String projectString = StringUtils.firstNonEmpty(
            System.getProperty(PROPERTY_PROJECT),
            System.getProperty(PROPERTY_PROJECTS));
        projects = StringUtils.isNotEmpty(projectString)
            ? Pattern.compile(";").splitAsStream(projectString).map(ProjectInfo::new).collect(Collectors.toList())
            : Collections.emptyList();
    }

    boolean cleanUp() {
        return !noCleanUp;
    }

    List<ArtifactInfo> getEakArtifacts() {
        if (eakArtifacts != null) {
            return eakArtifacts;
        }
        eakArtifacts = new ArrayList<>();
        Path root = Paths.get(StringUtils.EMPTY).toAbsolutePath().getParent();
        List<Path> childPomFiles;
        try (Stream<Path> modules = Stream.concat(Stream.of(root), Files.list(root))) {
            childPomFiles = modules
                .filter(Files::isDirectory)
                .map(child -> child.resolve(POM_FILE))
                .filter(Files::exists)
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new AssertionError("Could not retrieve project modules at " + root);
        }
        for (Path childPomFile : childPomFiles) {
            Model model = MavenProjectHelper.getModel(childPomFile);
            Parent parent = model.getParent();
            String groupId = parent != null ? parent.getGroupId() : model.getGroupId();
            String version = parent != null ? parent.getVersion() : model.getVersion();
            ArtifactInfo artifactInfo = new ArtifactInfo(
                groupId,
                model.getArtifactId(),
                version,
                model.getPackaging());
            boolean isDownloadable = CollectionUtils.isNotEmpty(model.getProfiles())
                && model.getProfiles().stream().anyMatch(p -> "deploy".equals(p.getId()));
            artifactInfo.setDownloadable(isDownloadable);
            eakArtifacts.add(artifactInfo);
        }
        return eakArtifacts;
    }

    public String getCurrentEakVersion() {
        if (eakVersion != null) {
            return eakVersion;
        }
        eakVersion = getMavenModel().getVersion();
        return eakVersion;
    }

    public List<String> getFilters(ProjectInfo project) {
        if (filters != null) {
            return filters;
        }
        File filtersDirectory = Stream.of(
            filtersPath,
            DEFAULT_FILTERS + CoreConstants.SEPARATOR_SLASH + project.getDeclaredEakVersion() + "-to-" + getCurrentEakVersion(),
            DEFAULT_FILTERS)
            .filter(StringUtils::isNotBlank)
            .map(path -> project.getDirectory().resolve(path).toFile())
            .filter(File::exists)
            .findFirst()
            .orElse(null);

        if (filtersDirectory == null) {
            LOG.info("Custom diff filters are not specified");
            filters = Collections.emptyList();
            return filters;
        }

        LOG.info("Loading diff filters from directory {}", filtersDirectory);
        File[] files = filtersDirectory.listFiles((dir, name) -> StringUtils.endsWithIgnoreCase(name, ".js"));
        if (ArrayUtils.isEmpty(files)) {
            filters = Collections.emptyList();
        } else {
            assert files != null;
            filters = new ArrayList<>();
            for (File file : files) {
                try {
                    String content = IOUtils.toString(file.toURI(), StandardCharsets.UTF_8);
                    filters.add(content);
                } catch (IOException e) {
                    throw new AssertionError("Could not read filter file " + file.getAbsolutePath());
                }
            }
        }
        LOG.info("{} filer(-s) loaded", filters.size());
        return filters;
    }

    String getMavenExecutable() {
        return mavenExecutable;
    }

    private Model getMavenModel() {
        if (mavenModel != null) {
            return mavenModel;
        }
        mavenModel = MavenProjectHelper.getModel(Paths.get(StringUtils.EMPTY).toAbsolutePath().getParent().resolve(POM_FILE));
        return mavenModel;
    }

    public List<ProjectInfo> getProjects() {
        return projects;
    }

    String getSnapshotRepository() {
        return Optional.ofNullable(getMavenModel().getDistributionManagement())
            .map(DistributionManagement::getSnapshotRepository)
            .map(RepositoryBase::getUrl)
            .orElse(StringUtils.EMPTY);
    }

    void validate() {
        Assert.assertFalse("Target project(-s) not specified", projects.isEmpty());
        Assert.assertTrue("Maven executable is invalid", StringUtils.isNotBlank(mavenExecutable));
        Assert.assertTrue(
            "Could not retrieve current EAK version",
            StringUtils.isNotEmpty(getCurrentEakVersion()));
    }
}
