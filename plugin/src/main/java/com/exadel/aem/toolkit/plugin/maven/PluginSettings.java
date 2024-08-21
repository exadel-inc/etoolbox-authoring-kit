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

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.exadel.aem.toolkit.plugin.exceptions.handlers.ExceptionHandlers;
import com.exadel.aem.toolkit.plugin.utils.ClassUtil;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;

/**
 * Contains settings specified in the Maven's POM file(-s) that control the execution of the ToolKit's plugin
 */
public class PluginSettings {

    public static final PluginSettings EMPTY = new PluginSettings();

    private String defaultPathBase;

    private Set<ReferenceEntry> referenceEntries;

    private String terminateOn;

    /**
     * Default (instantiation-restricting) constructor
     */
    PluginSettings() {
    }

    /**
     * Retrieves the root path within the components file structure. In a multi-package project, the routine attempts to
     * pick up the most appropriate path judging by the component's class and falls back to the path specified in the
     * current ToolKit plugin instance. The result is used to define a corresponding file system entry for every AEM
     * component-backing Java class
     * @param component The {@code Class} for which to pick up a proper path
     * @return String value
     */
    public String getPathBase(Class<?> component) {
        if (referenceEntries == null) {
            return StringUtils.defaultString(defaultPathBase);
        }
        String matchedPathBase = referenceEntries
            .stream()
            .filter(entry -> entry.matches(component))
            .map(ReferenceEntry::getPathBase)
            .findFirst()
            .orElse(null);
        if (StringUtils.isNotEmpty(matchedPathBase)) {
            return matchedPathBase;
        }
        return StringUtils.defaultString(defaultPathBase);
    }

    /**
     * Retrieves a string value that describes in what exception cases the execution of the plugin is terminated
     * @return Optional string value
     * @see ExceptionHandlers
     */
    public String getTerminateOnRule() {
        return StringUtils.defaultIfEmpty(terminateOn, DialogConstants.VALUE_NONE);
    }

    /**
     * Initializes a {@code Builder} instance used to populate a {@link PluginSettings} object with values
     * @return {@code Builder} object
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * Used to create a {@link PluginSettings} object and populate it with values
     */
    static class Builder {
        private String pathBase;
        private Set<ReferenceEntry> referenceEntries;
        private String terminateOn;

        /**
         * Assigns the value that corresponds to the current path base ({@code componentsPathBase} in the plugin's
         * settings)
         * @param value String value; a non-blank string is expected
         * @return This instance
         * @see PluginSettings#getPathBase(Class) ()
         */
        public Builder defaultPathBase(String value) {
            pathBase = value;
            return this;
        }

        /**
         * Assigns the pair of values that match the path base ({@code componentsPathBase} in plugin's settings) to the
         * name of the package ({@code componentsReferenceBase} in plugin's settings)
         * @param pathValue      String value; if a null or a blank value is provided, nothing is assigned
         * @param referenceValue String value; if a null or a blank value is provided, nothing is assigned
         * @return This instance
         * @see PluginSettings#getPathBase(Class)
         */
        @SuppressWarnings("UnusedReturnValue")
        public Builder referenceEntry(String pathValue, String referenceValue) {
            if (StringUtils.isAnyBlank(pathValue, referenceValue)) {
                return this;
            }
            if (referenceEntries == null) {
                referenceEntries = new LinkedHashSet<>();
            }
            referenceEntries.add(new ReferenceEntry(pathValue, referenceValue));
            return this;
        }

        /**
         * Assigns the {@code terminateOn} value used in exception processing
         * @param value Optional string value
         * @return This instance
         * @see PluginSettings#getTerminateOnRule()
         */
        public Builder terminateOn(String value) {
            terminateOn = value;
            return this;
        }

        /**
         * Creates and populates a {@link PluginSettings} object
         * @return {@code PluginSettings} instance
         */
        public PluginSettings build() {
            PluginSettings result = new PluginSettings();
            result.defaultPathBase = this.pathBase;
            result.referenceEntries = this.referenceEntries;
            result.terminateOn = this.terminateOn;
            return result;
        }
    }

    /**
     * Represents a match between a {@code componentsPathBase} setting and a {@code componentsReferenceBase} setting as
     * specified in the ToolKit's plugin config for a Maven module
     */
    private static class ReferenceEntry {
        private final String pathBase;
        private final String referenceBase;

        /**
         * Instance constructor
         * @param pathBase      A string representing the {@code componentsPathBase} setting
         * @param referenceBase A string representing the {@code componentsReferenceBase} setting
         */
        ReferenceEntry(String pathBase, String referenceBase) {
            this.pathBase = pathBase;
            this.referenceBase = referenceBase;
        }

        /**
         * Retrieves the path base
         * @return String value; non-null
         */
        public String getPathBase() {
            return StringUtils.defaultString(pathBase);
        }

        /**
         * Gets whether the current entry can represent the given component judging by its Java package
         * @param component AEM component-backing Java class
         * @return True or false
         */
        public boolean matches(Class<?> component) {
            return ClassUtil.matchesReference(component, referenceBase);
        }

        /**
         * Compares the current instance with another object
         * @param other Another object
         * @return True if the objects are equal, false otherwise
         */
        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (other == null || getClass() != other.getClass()) {
                return false;
            }
            ReferenceEntry that = (ReferenceEntry) other;
            return new EqualsBuilder().append(pathBase, that.pathBase).append(referenceBase, that.referenceBase).isEquals();
        }

        /**
         * Calculates the hash code of the current instance
         * @return Integer value
         */
        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37).append(pathBase).append(referenceBase).toHashCode();
        }
    }
}
