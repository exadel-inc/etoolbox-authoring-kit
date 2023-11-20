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
package com.exadel.aem.toolkit.plugin.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.toolchain.Toolchain;
import org.apache.maven.toolchain.ToolchainManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains utility methods for working with Maven toolchain
 */
public class ToolchainUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ToolchainUtil.class);

    private static final String DIRECTORY_BIN = "bin";
    private static final String TOOL_JAVA = "java";
    private static final String TYPE_JDK = "jdk";

    /**
     * Default (instantiation-restricting) constructor
     */
    private ToolchainUtil() {
    }

    /**
     * Gets whether the ToolKit's plugin should reload with the JDK specified in the current Maven toolchain
     * @param toolchainManager {@link ToolchainManager} instance
     * @param session {@link MavenSession} instance
     * @return True or false
     */
    public static boolean shouldReload(ToolchainManager toolchainManager, MavenSession session) {
        String javaHome = System.getProperty(DialogConstants.PN_JAVA_HOME);
        String toolchainJavaHome = getJavaHome(toolchainManager, session);
        return StringUtils.isNoneEmpty(javaHome, toolchainJavaHome) && !StringUtils.equals(javaHome, toolchainJavaHome);
    }

    /**
     * Gets the path to the JDK specified in the current Maven toolchain
     * @param toolchainManager {@link ToolchainManager} instance
     * @param session {@link MavenSession} instance
     * @return {@code String} value, or empty string if not found
     */
    public static String getJavaHome(ToolchainManager toolchainManager, MavenSession session) {
        Toolchain jdkToolchain = toolchainManager.getToolchainFromBuildContext(TYPE_JDK, session);
        String javaPath = jdkToolchain != null ? jdkToolchain.findTool(TOOL_JAVA) : null;
        if (javaPath != null) {
            Path path = Paths.get(javaPath).getParent();
            if (DIRECTORY_BIN.equals(path.getFileName().toString())) {
                path = path.getParent();
            }
            return path.toAbsolutePath().toString();
        }
        return StringUtils.EMPTY;
    }
}
