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

    @AttributeDefinition(name = "Enabled", description = "Enable listening to configuration changes")
    boolean enabled() default false;

    @AttributeDefinition(name = "Clean up PIDs", description = "List of configuration PIDs to clean up on startup")
    String[] cleanUp() default {};
}
