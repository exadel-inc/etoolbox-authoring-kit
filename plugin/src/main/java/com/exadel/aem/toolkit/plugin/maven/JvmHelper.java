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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.toolchain.Toolchain;
import org.apache.maven.toolchain.ToolchainManager;
import org.codehaus.plexus.util.cli.Commandline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains utility methods for working with JVM settings
 */
class JvmHelper {

    private static final Logger LOG = LoggerFactory.getLogger(JvmHelper.class);

    private static final String PROPERTY_MAVEN_HOME = "maven.home";
    private static final String PROPERTY_CLASSWORLDS = "classworlds.conf";
    private static final String PROPERTY_CLASS_PATH = "java.class.path";

    private static final String ARGUMENT_FORMAT = "-D%s=%s";

    /**
     * Default (instantiation-restricting) constructor
     */
    private JvmHelper() {
    }

    /**
     * Gets whether the ToolKit's plugin needs to be launched in a separate process with the JDK specified in the
     * current Maven toolchain
     * @param toolchainManager {@link ToolchainManager} instance
     * @param session          {@link MavenSession} instance
     * @return True or false
     */
    static boolean shouldRelaunch(ToolchainManager toolchainManager, MavenSession session) {
        String javaHome = getJavaHome();
        String toolchainJavaHome = getJavaHome(toolchainManager, session);
        LOG.info("Java home: {}", javaHome);
        LOG.info("Toolchain Java home: {}", toolchainJavaHome);
        if (StringUtils.isAnyEmpty(javaHome, toolchainJavaHome) || StringUtils.equals(javaHome, toolchainJavaHome)) {
            return false;
        }
        return validateProperties();
    }

    /**
     * Retrieves the name of a Java executable per the current toolchain
     * @param toolchainManager {@link ToolchainManager} instance
     * @param session          {@link MavenSession} instance
     * @return String value; can be empty if a toolchain is not set up
     */
    static String getJavaExecutable(ToolchainManager toolchainManager, MavenSession session) {
        Toolchain jdkToolchain = toolchainManager.getToolchainFromBuildContext("jdk", session);
        return jdkToolchain != null ? jdkToolchain.findTool("java") : StringUtils.EMPTY;
    }

    /**
     * Gets the path to the Java installation specified in the current environment
     * @return {@code String} value, or empty string if not found
     */
    static String getJavaHome() {
        return StringUtils.firstNonBlank(
            System.getProperty("JAVA_HOME"),
            System.getProperty("java.home"));
    }

    /**
     * Gets the path to the Java installation specified in the current Maven toolchain
     * @param executable Path to the {@code java} binary
     * @return String value, or an empty string if the provided executable is invalid
     */
    static String getJavaHome(String executable) {
        Path result = Paths.get(executable).getParent();
        if ("bin".equals(result.getFileName().toString())) {
            result = result.getParent();
        }
        return result.toAbsolutePath().toString();
    }

    /**
     * Gets the path to the Java installation specified in the current Maven toolchain
     * @param toolchainManager {@link ToolchainManager} instance
     * @param session          {@link MavenSession} instance
     * @return String value, or an empty string if not found
     */
    private static String getJavaHome(ToolchainManager toolchainManager, MavenSession session) {
        String executable = getJavaExecutable(toolchainManager, session);
        if (StringUtils.isEmpty(executable)) {
            return StringUtils.EMPTY;
        }
        return getJavaHome(executable);
    }

    /**
     * Validates that the system properties required for successful JVM process forking are set
     * @return True or false
     */
    private static boolean validateProperties() {
        boolean result = true;
        for (String property : Arrays.asList(PROPERTY_CLASS_PATH, PROPERTY_CLASSWORLDS, PROPERTY_MAVEN_HOME)) {
            if (StringUtils.isEmpty(System.getProperty(property))) {
                LOG.warn("Property {} is not defined", property);
                result = false;
            }
        }
        return result;
    }

    /**
     * Composes a {@link Commandline} instance to launch the ToolKit's plugin in a separate JVM process
     * @return {@link CommandLineBuilder} object
     */
    static CommandLineBuilder commandLine() {
        return new CommandLineBuilder();
    }

    /**
     * Builds a {@link Commandline} instance to launch the ToolKit's plugin in a separate JVM process
     */
    static class CommandLineBuilder {

        private String executable;
        private String directory;
        private String pluginCommand;
        private Map<String, String> arguments;

        /**
         * Assigns the path to the {@code java} executable
         * @param value String value; a non-empty string is expected
         * @return This instance
         */
        CommandLineBuilder executable(String value) {
            executable = value;
            return this;
        }

        /**
         * Assigns the working directory for the current Maven project
         * @param value String value; a non-empty string is expected
         * @return This instance
         */
        CommandLineBuilder directory(String value) {
            directory = value;
            return this;
        }

        /**
         * Assigns the plugin command to be executed
         * @param value String value; a non-empty string is expected
         * @return This instance
         */
        CommandLineBuilder pluginCommand(String value) {
            pluginCommand = value;
            return this;
        }

        /**
         * Assigns a particular argument to be passed to the plugin
         * @param key  The argument name
         * @param value The argument value
         * @return This instance
         */
        CommandLineBuilder argument(String key, String value) {
            if (arguments == null) {
                arguments = new HashMap<>();
            }
            arguments.put(key, value);
            return this;
        }

        /**
         * Creates a {@link Commandline} instance based on the provided parameters
         * @return {@code Commandline} object
         */
        Commandline build() {
            Commandline commandline = new Commandline();
            commandline.setExecutable(executable);
            commandline.setWorkingDirectory(directory);
            List<String> commandLineArgs = new ArrayList<>();
            commandLineArgs.add(String.format(ARGUMENT_FORMAT, "maven.multiModuleProjectDirectory", directory));
            commandLineArgs.add(String.format(ARGUMENT_FORMAT, PROPERTY_MAVEN_HOME, System.getProperty(PROPERTY_MAVEN_HOME)));
            commandLineArgs.add(String.format(ARGUMENT_FORMAT, PROPERTY_CLASSWORLDS, System.getProperty(PROPERTY_CLASSWORLDS)));
            commandLineArgs.add("-classpath");
            commandLineArgs.add(System.getProperty(PROPERTY_CLASS_PATH));
            commandLineArgs.add("org.codehaus.classworlds.Launcher");
            commandLineArgs.add(pluginCommand);
            if (arguments != null) {
                arguments.forEach((key, value) -> commandLineArgs.add(String.format(ARGUMENT_FORMAT, key, value)));
            }
            commandLineArgs.add("--batch-mode");
            commandline.addArguments(commandLineArgs.toArray(new String[0]));
            return commandline;
        }
    }
}
