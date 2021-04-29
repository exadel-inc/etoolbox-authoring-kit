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
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.commons.lang3.StringUtils;

/**
 * Contains information about the current binary, such as build version and timestamp, to be used for package versioning
 */
public class PluginInfo {
    private static final String MANIFEST_FILE_ADDRESS = "META-INF/MANIFEST.MF";

    private static final String ATTRIBUTE_NAME = "Bundle-Name";
    private static final String ATTRIBUTE_VERSION = "Bundle-Version";
    private static final String ATTRIBUTE_TIMESTAMP = "Build-Timestamp";

    private final String name;
    private final String version;
    private final String timestamp;

    /**
     * Fallback constructor creating an empty {@code PluginInfo} instance
     */
    private PluginInfo() {
        this(StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY);
    }

    /**
     * Default constructor. Creates a {@code PluginInfo} instance with name, version, and build timestamp specified
     * @param name      Name of the artifact
     * @param version   Version of the artifact
     * @param timestamp Build timestamp as assigned within the Maven build workflow
     */
    private PluginInfo(String name, String version, String timestamp) {
        this.name = name;
        this.version = version;
        this.timestamp = timestamp;
    }

    /**
     * Gets the name of the artifact
     * @return String value (non-null)
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the version of the artifact
     * @return String value (non-null)
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the build timestamp of the artifact
     * @return String value (non-null)
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Creates a new {@link PluginInfo} instance and fills it with information stored in the {@code Manifest} file
     * @return {@code PluginIngo} object
     */
    static PluginInfo getInstance() {
        Manifest manifest = new Manifest();
        try {
            manifest.read(Thread.currentThread().getContextClassLoader().getResourceAsStream(MANIFEST_FILE_ADDRESS));
            Attributes manifestAttributes = manifest.getMainAttributes();
            return new PluginInfo(
                manifestAttributes.getValue(ATTRIBUTE_NAME),
                manifestAttributes.getValue(ATTRIBUTE_VERSION),
                manifestAttributes.getValue(ATTRIBUTE_TIMESTAMP));
        } catch (IOException | IllegalArgumentException e) {
            // This is not intended to produce an effective exception
            return new PluginInfo();
        }
    }
}
