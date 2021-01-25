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

import java.util.List;
import java.util.function.Consumer;

import com.exadel.aem.toolkit.api.runtime.ExceptionHandler;
import com.exadel.aem.toolkit.plugin.exceptions.handlers.ExceptionHandlers;
import com.exadel.aem.toolkit.plugin.util.PluginReflectionUtility;
import com.exadel.aem.toolkit.plugin.util.PluginXmlUtility;

/**
 * The implementation of {@link PluginRuntimeContext} for the AEM Authoring Toolkit plugin instance that
 * has been properly initialized
 */
class LoadedRuntimeContext implements PluginRuntimeContext {
    private PluginReflectionUtility pluginReflections;
    private ExceptionHandler exceptionHandler;
    private PluginXmlUtility xmlUtility;

    @Override
    public PluginReflectionUtility getReflectionUtility() {
        return pluginReflections;
    }

    @Override
    public ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    @Override
    public PluginXmlUtility getXmlUtility() {
        return xmlUtility;
    }


    /**
     * Accumulates data and performs necessary routines for creating the functional ("loaded") {@link PluginRuntimeContext}
     */
    static class Builder {
        private List<String> classPathElements;
        private String packageBase;
        private String terminateOn;
        private Consumer<LoadedRuntimeContext> onComplete;

        /**
         * Creates new instance of this Builder
         * @param onComplete Routine that will be triggered upon Builder completion (basically, assigning the created
         *                   {@code LoadedRuntimeContext} to the global {@code PluginRuntime} instance
         */
        Builder(Consumer<LoadedRuntimeContext> onComplete) {
            this.onComplete = onComplete;
        }

        /**
         * Assigns the classpath elements to this instance. This is used to initialize the {@code PluginReflections} registry
         * @param value List of strings representing classpath elements
         * @return This Builder instance
         */
        Builder classPathElements(List<String> value) {
            this.classPathElements = value;
            return this;
        }

        /**
         * Assigns the package base to this instance. Accepts a string representing package prefix of processable
         * AEM backend components, like {@code com.acme.aem.components.*}. If not specified, all available components
         * will be processed
         * @param value String representing package base as described above
         * @return This Builder instance
         */
        Builder packageBase(String value) {
            this.packageBase = value;
            return this;
        }

        /**
         * Assigns the {@code terminateOn} setting to this instance
         * @param value String containing a list of terminating and non-terminating exceptions
         * @return This Builder instance
         */
        Builder terminateOn(String value) {
            this.terminateOn = value;
            return this;
        }

        /**
         * Creates a functional ("loaded") {@code PluginRuntimeContext}
         * and feeds it to the provided {@code onComplete} routine
         */
        void build() {
            if (!isValid()) {
                return;
            }
            LoadedRuntimeContext result = new LoadedRuntimeContext();
            result.pluginReflections = PluginReflectionUtility.fromCodeScope(classPathElements, packageBase);
            result.exceptionHandler = ExceptionHandlers.forSetting(terminateOn);
            result.xmlUtility = new PluginXmlUtility();
            this.onComplete.accept(result);
        }

        /**
         * Gets whether this Builder has enough data to create a loaded {@code PluginRuntimeContext}
         * @return True or false
         */
        private boolean isValid() {
            return classPathElements != null && !classPathElements.isEmpty();
        }
    }
}

