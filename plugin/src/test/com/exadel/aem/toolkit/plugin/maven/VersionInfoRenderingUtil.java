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

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.ImmutableMap;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.writers.PackageWriter;

class VersionInfoRenderingUtil {
    private static final Logger LOG = LoggerFactory.getLogger(VersionInfoRenderingUtil.class);

    private static final String PACKAGE_INFO_PATH = "META-INF/etoolbox-authoring-kit/version.info";
    private static final String PROPERTY_VERSION = "version";
    private static final String ARTIFACT_NAME = "EAK-Test";
    private static final String ARTIFACT_VERSION = "1.0";

    private VersionInfoRenderingUtil() {
    }

    public static Map<String, String> getVersionInfo(FileSystem fileSystem) {
        PackageWriter
            .forFileSystem(fileSystem, TestConstants.DEFAULT_PROJECT_NAME)
            .writeInfo(PluginInfo.getInstance(ARTIFACT_NAME, ARTIFACT_VERSION));
        Path infoFilePath = fileSystem.getRootDirectories().iterator().next().resolve(PACKAGE_INFO_PATH);
        try {
            List<String> lines = Files.readAllLines(infoFilePath);
            return lines.size() >= 3
                ? ImmutableMap.of(
                CoreConstants.PN_NAME, lines.get(0),
                PROPERTY_VERSION, lines.get(1),
                TestConstants.PROPERTY_TIMESTAMP, lines.get(2))
                : Collections.emptyMap();
        } catch (IOException ex) {
            LOG.error("Could not read the package info file {}", infoFilePath, ex);
        }
        return Collections.emptyMap();
    }
}
