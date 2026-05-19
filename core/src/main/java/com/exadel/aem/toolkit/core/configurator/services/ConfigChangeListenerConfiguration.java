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
package com.exadel.aem.toolkit.core.configurator.services;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * Defines configuration properties for {@link ConfigChangeListener}
 * <p><u>Note</u>: This class is not a part of the public API and is subject to change. Do not use it in your own
 * code</p>
 */
@ObjectClassDefinition(name = "EToolbox Authoring Kit - Configurator")
public @interface ConfigChangeListenerConfiguration {

    /**
     * Defines whether the listener is enabled
     * @return True or false
     */
    @AttributeDefinition(name = "Enabled", description = "Enable listening to configuration changes")
    boolean enabled() default false;

    /**
     * Defines a list of configuration PIDs to clean up on startup
     * @return Array of strings
     */
    @AttributeDefinition(name = "Clean up PIDs", description = "List of configuration PIDs to clean up on startup")
    String[] cleanUp() default {};

    /**
     * Defines the number of retries when a resource cannot be immediately resolved after a change event. A value of
     * {@code 0} disables retrying
     * @return Retry count
     */
    @AttributeDefinition(
        name = "Resource-resolve retry count",
        description = "Number of times to retry resolving a changed resource before treating it as removed")
    int resolveRetryCount() default 3;

    /**
     * Defines the delay in milliseconds between consecutive resource-resolve retries
     * @return Delay in milliseconds
     */
    @AttributeDefinition(
        name = "Resource-resolve retry delay (ms)",
        description = "Milliseconds to wait between resource-resolve retries")
    long resolveRetryDelay() default 200L;
}
