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

/**
 * The thread-local {@link PluginRuntimeContext} handler to be used within {@code PluginMojo} execution. Starts with
 * {@link EmptyRuntimeContext} and switches to the {@link LoadedRuntimeContext} upon proper runtime initialization
 * @see PluginMojo#execute()
 */
public class PluginRuntime {
    private static final ThreadLocal<PluginRuntimeContext> INSTANCE = ThreadLocal.withInitial(EmptyRuntimeContext::new);

    private PluginRuntime() {
    }

    /**
     * Gets current {@link PluginRuntimeContext}
     * @return {@code PluginRuntimeContext} instance
     */
    public static PluginRuntimeContext context() {
        return INSTANCE.get();
    }

    /**
     * Creates a Builder intended to accumulate plugin settings and produce a functional ("loaded") {@code PluginRuntimeContext}
     * @return {@link LoadedRuntimeContext.Builder} instance
     */
    static LoadedRuntimeContext.Builder contextBuilder() {
        return new LoadedRuntimeContext.Builder(INSTANCE::set);
    }

    /**
     * Disposes of current {@link LoadedRuntimeContext} instance by calling the {@link ThreadLocal#remove()} method
     */
    static void close() {
        INSTANCE.remove();
    }
}
