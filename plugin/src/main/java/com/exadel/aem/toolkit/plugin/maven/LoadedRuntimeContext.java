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
import javax.xml.parsers.ParserConfigurationException;

import com.exadel.aem.toolkit.api.runtime.ExceptionHandler;
import com.exadel.aem.toolkit.plugin.exceptions.PluginException;
import com.exadel.aem.toolkit.plugin.exceptions.handlers.ExceptionHandlers;
import com.exadel.aem.toolkit.plugin.runtime.ReflectionContextHelper;
import com.exadel.aem.toolkit.plugin.runtime.XmlContextHelper;
import com.exadel.aem.toolkit.plugin.utils.XmlFactory;

/**
 * The implementation of {@link PluginRuntimeContext} for the ToolKit Maven plugin instance that
 * has been properly initialized
 */
class LoadedRuntimeContext implements PluginRuntimeContext {
    private static final String XML_EXCEPTION_MESSAGE = "Could not initialize XML runtime";

    private ReflectionContextHelper pluginReflections;
    private ExceptionHandler exceptionHandler;
    private XmlContextHelper xmlRuntime;

    /**
     * {@inheritDoc}
     */
    @Override
    public ReflectionContextHelper getReflection() {
        return pluginReflections;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XmlContextHelper getXmlUtility() {
        return xmlRuntime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XmlContextHelper newXmlUtility() {
        try {
            xmlRuntime = new XmlContextHelper(XmlFactory.newDocument());
        } catch (ParserConfigurationException e) {
            // Cannot proceed with the plugin flow if XML subsystem fails this early
            throw new PluginException(XML_EXCEPTION_MESSAGE, e);
        }
        return xmlRuntime;
    }


    /**
     * Accumulates data and performs necessary routines for creating the functional ("loaded") {@link PluginRuntimeContext}
     */
    static class Builder {

        private List<String> classPathElements;
        private String packageBase;
        private String terminateOn;
        private final Consumer<LoadedRuntimeContext> onComplete;

        /**
         * Creates a new instance of this Builder
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
         * AEM backend components like {@code com.acme.aem.components.*}. If not specified, all available components
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
            result.pluginReflections = ReflectionContextHelper.fromCodeScope(classPathElements, packageBase);
            result.exceptionHandler = ExceptionHandlers.forSetting(terminateOn);
            result.newXmlUtility();
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

