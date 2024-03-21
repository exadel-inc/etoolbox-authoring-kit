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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.project.MavenProject;
import org.junit.Assert;

import com.exadel.aem.toolkit.core.CoreConstants;

class ProjectInfo {

    private static final String CLI_PROPERTY_PREFIX = "-D";

    private static final String PLACEHOLDER = "${%s}";

    private static final Pattern CLI_OPTION = Pattern.compile("\\s-[A-Za-z]+");
    private static final Pattern SPACE_DELIMITER = Pattern.compile("\\s+");

    private Path directory;
    private List<String> options;
    private Path pomFile;
    private String version;

    ProjectInfo(String source) {
        String directoryString = extractDirectory(source);
        if (StringUtils.isEmpty(directoryString)) {
            return;
        }
        directory = Paths.get(StringUtils.EMPTY).resolve(directoryString.trim()).toAbsolutePath();
        pomFile = directory.resolve(RegressionSettings.POM_FILE).toAbsolutePath();
        options = extractOptions(source.substring(directoryString.length()));
    }

    String getDeclaredEakVersion() {
        if (version != null) {
            return version;
        }
        String versionProperty = getVersionProperty();
        if (StringUtils.isEmpty(versionProperty)) {
            version = StringUtils.EMPTY;
        } else {
            MavenProject mavenProject = MavenProjectHelper.getProject(pomFile);
            version = mavenProject.getProperties().getProperty(versionProperty, StringUtils.EMPTY);
        }
        return version;
    }

    Path getDirectory() {
        return directory;
    }

    List<String> getOptions() {
        return options;
    }

    List<String> getOptions(Map<String, String> interpolatable) {
        return options
            .stream()
            .map(option -> {
                String result = option;
                for (Map.Entry<String, String> entry : interpolatable.entrySet()) {
                    result = StringUtils.replace(result, String.format(PLACEHOLDER, entry.getKey()), entry.getValue());
                }
                return result;
            })
            .collect(Collectors.toList());
    }

    private String getVersionProperty() {
        if (CollectionUtils.isEmpty(options)) {
            return StringUtils.EMPTY;
        }
        return options
            .stream()
            .filter(entry -> entry.startsWith(CLI_PROPERTY_PREFIX) && entry.contains("version="))
            .map(entry -> StringUtils.substringBefore(entry.substring(CLI_PROPERTY_PREFIX.length()), CoreConstants.EQUALITY_SIGN))
            .findFirst()
            .orElse(StringUtils.EMPTY);
    }

    void validate() {
        Assert.assertTrue(
            "Invalid project directory " + directory,
            directory != null && directory.toFile().exists());
        Assert.assertTrue(
            "Invalid POM file " + pomFile, pomFile != null && pomFile.toFile().exists());
        Assert.assertTrue(
            "EAK version is missing or invalid in " + pomFile, StringUtils.isNotEmpty(getDeclaredEakVersion()));
    }

    private static String extractDirectory(String source) {
        if (StringUtils.isBlank(source)) {
            return StringUtils.EMPTY;
        }
        Matcher matcher = CLI_OPTION.matcher(source);
        if (matcher.find()) {
            return source.substring(0, matcher.start()).trim();
        }
        return source.trim();
    }

    private static List<String> extractOptions(String source) {
        if (StringUtils.isBlank(source)) {
            return Collections.emptyList();
        }
        return SPACE_DELIMITER
            .splitAsStream(source)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());
    }
}
