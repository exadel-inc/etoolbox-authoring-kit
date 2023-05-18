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
 * The implementation of {@link PluginRuntimeContext} for the ToolKit Maven plugin instance that has been properly
 * initialized
 */
class LoadedRuntimeContext implements PluginRuntimeContext {
    private static final String XML_EXCEPTION_MESSAGE = "Could not initialize XML runtime";

    private PluginSettings settings;
    private ReflectionContextHelper pluginReflections;
    private ExceptionHandler exceptionHandler;
    private XmlContextHelper xmlRuntime;

    /**
     * {@inheritDoc}
     */
    @Override
    public PluginSettings getSettings() {
        return settings;
    }

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
            // Cannot proceed with the plugin flow if the XML subsystem fails this early
            throw new PluginException(XML_EXCEPTION_MESSAGE, e);
        }
        return xmlRuntime;
    }

    /**
     * Accumulates data and performs necessary routines for creating the functional ("loaded") {@link
     * PluginRuntimeContext}
     */
    public static class Builder {

        private List<String> classPathElements;
        private PluginSettings settings = new PluginSettings();
        private ExceptionHandler exceptionHandler;
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
         * Assigns the collection of classpath elements to this instance. This property is used to initialize the {@code
         * PluginReflections} registry
         * @param value List of strings representing classpath elements. A non-empty list is expected
         * @return This Builder instance
         */
        Builder classPathElements(List<String> value) {
            this.classPathElements = value;
            return this;
        }

        /**
         * Assigns the {@link PluginSettings} object to this instance. Settings are defined within the Maven subsystem
         * to control the plugin execution
         * @param value {@code PluginSettings} object. A non-null value is expected
         * @return This instance
         */
        Builder settings(PluginSettings value) {
            this.settings = value;
            return this;
        }

        /**
         * Assigns a particular {@link ExceptionHandler}. The handler is used to override the exception handler derived
         * from the plugin's settings (usually in test cases)
         * @param value {@code ExceptionHandler} object. A non-null value is expected
         * @return This instance
         */
        Builder exceptionHandler(ExceptionHandler value) {
            this.exceptionHandler = value;
            return this;
        }

        /**
         * Creates a functional ("loaded") {@code PluginRuntimeContext} and feeds it to the provided {@code onComplete}
         * routine
         */
        void build() {
            if (!isValid()) {
                return;
            }
            LoadedRuntimeContext result = new LoadedRuntimeContext();
            result.settings = settings;
            result.pluginReflections = ReflectionContextHelper.fromCodeScope(classPathElements);
            result.exceptionHandler = exceptionHandler != null
                ? exceptionHandler
                : ExceptionHandlers.forSetting(settings.getTerminateOnRule());
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

