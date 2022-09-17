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

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.plugin.exceptions.handlers.ExceptionHandlers;

/**
 * Contains settings specified in the Maven's POM file(-s) that control the execution of the ToolKit's plugin
 */
public class PluginSettings {

    private String componentsPathBase;

    private String componentsReferenceBase;

    private String terminateOn;

    /**
     * Retrieves the root path within the components file structure. This is used to define a corresponding file system
     * entry for every AEM component-backing Java class
     * @return String value
     */
    public String getComponentsPathBase() {
        return StringUtils.defaultString(componentsPathBase);
    }

    /**
     * Retrieves the root package qualifier for the AEM components processable by this plugin like {@code
     * com.acme.aem.components.*}. If not specified, all available components will be processed
     * @return Optional string value
     */
    public String getComponentsReferenceBase() {
        return componentsReferenceBase;
    }

    /**
     * Retrieves a string value that describes in what exception cases the execution of the plugin is terminated
     * @return Optional string value
     * @see ExceptionHandlers
     */
    public String getTerminateOnRule() {
        return terminateOn;
    }

    /**
     * Initializes a {@code Builder} instance used to populate a {@link PluginSettings} object with values
     * @return {@code Builder} object
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Used to create a {@link PluginSettings} object and populate in with values
     */
    static class Builder {
        private String componentsPathBase;
        private String componentsReferenceBase;
        private String terminateOn;

        /**
         * Assigns the {@code componentsPathBase} value
         * @param value String value; a non-blank string is expected
         * @return This instance
         * @see PluginSettings#getComponentsPathBase()
         */
        public Builder componentsPathBase(String value) {
            componentsPathBase = value;
            return this;
        }

        /**
         * Assigns the {@code componentsReferenceBase} value used to filter available component classes
         * @param value Optional string value
         * @return This instance
         * @see PluginSettings#getComponentsReferenceBase()
         */
        public Builder componentsReferenceBase(String value) {
            componentsReferenceBase = value;
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
            result.componentsPathBase = this.componentsPathBase;
            result.componentsReferenceBase = this.componentsReferenceBase;
            result.terminateOn = this.terminateOn;
            return result;
        }
    }
}
