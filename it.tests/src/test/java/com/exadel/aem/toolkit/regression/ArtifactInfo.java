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

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.core.CoreConstants;

class ArtifactInfo {

    static final String PACKAGING_CONTENT = "content-package";
    static final String PACKAGING_PLUGIN = "maven-plugin";
    static final String PACKAGING_POM = "pom";

    static final String EXTENSION_ZIP = "zip";
    private static final String EXTENSION_JAR = "jar";
    private static final String EXTENSION_POM = PACKAGING_POM;

    private final String groupId;
    private final String id;
    private final String packaging;

    private boolean downloadable;
    private String path;
    private String pomPath;
    private String version;

    ArtifactInfo(String groupId, String id, String version, String packaging) {
        this.groupId = groupId;
        this.id = id;
        this.version = version;
        this.packaging = packaging;
    }

    String getGroupId() {
        return groupId;
    }

    String getId() {
        return id;
    }

    String getPackaging() {
        return packaging;
    }

    boolean isDownloadable() {
        return downloadable;
    }

    void setDownloadable(boolean downloadable) {
        this.downloadable = downloadable;
    }

    String getPath() {
        return path;
    }

    void setPath(String path) {
        this.path = path;
    }

    String getPomPath() {
        return pomPath;
    }

    String getVersion() {
        return version;
    }

    void setVersion(String value) {
        version = value;
    }

    String getFileExtension() {
        if (EXTENSION_POM.equals(packaging)) {
            return EXTENSION_POM;
        }
        if (PACKAGING_CONTENT.equals(packaging)) {
            return EXTENSION_ZIP;
        }
        return EXTENSION_JAR;
    }

    String getFullName() {
        return getFullName(null);
    }

    String getFullName(String packaging) {
        String result = StringUtils.joinWith(
            CoreConstants.SEPARATOR_COLON,
            groupId,
            id,
            version);
        if (StringUtils.isEmpty(packaging)) {
            return result;
        }
        return result + CoreConstants.SEPARATOR_COLON + packaging;
    }

    void resolveFrom(Path root) {
        Path target = resolve(root, getFileExtension());
        path = target.toFile().exists() ? target.toAbsolutePath().toString() : null;
        if (PACKAGING_POM.equals(packaging)) {
            pomPath = path;
        } else {
            target = resolve(root, EXTENSION_POM);
            pomPath = target.toFile().exists() ? target.toAbsolutePath().toString() : null;
        }
    }

    private Path resolve(Path root, String extension) {
        return root
            .resolve(groupId.replace(CoreConstants.SEPARATOR_DOT, CoreConstants.SEPARATOR_SLASH))
            .resolve(id)
            .resolve(version)
            .resolve(id + CoreConstants.SEPARATOR_HYPHEN + version + CoreConstants.SEPARATOR_DOT + extension);
    }
}
