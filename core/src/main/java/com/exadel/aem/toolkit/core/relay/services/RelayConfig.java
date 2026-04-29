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
package com.exadel.aem.toolkit.core.relay.services;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * OSGi Metatype configuration for the relay provider host. Defines path and user mappings and optional
 * change-reporting settings
 */
@ObjectClassDefinition(name = "EToolbox Authoring Kit - Relay")
public @interface RelayConfig {

    /**
     * Gets whether this relay configuration is active
     * @return True or false
     */
    @AttributeDefinition(
        name = "Enable",
        description = "Is the relay enabled?"
    )
    boolean enabled();

    /**
     * Gets the list of JCR path mapping rules
     * @return A non-null array of path mapping rule strings; might be empty
     */
    @AttributeDefinition(
        name = "Path mappings",
        description = "List of path mapping rules."
    )
    String[] pathMappings();

    /**
     * Gets the optional list of user mapping rules. Each entry is either a {@code "user:password"} credential
     * string or a registered subservice name
     * @return A non-null array of user mapping rule strings; might be empty
     */
    @AttributeDefinition(
        name = "User mappings",
        description = "Optional list of user mapping rules. "
            + "The mapping target is either \"user:password\" or \"registered_subservice\"."
    )
    String[] userMappings();

    /**
     * Gets the optional list of JCR paths or XPath expressions whose matching resources are reported as changed
     * when the relay is enabled or disabled
     * @return A non-null array of path or XPath expression strings; might be empty
     */
    @AttributeDefinition(
        name = "Announce changes",
        description = "Optional list of JCR paths or XPath expressions. "
            + "Target paths matching the list will be announced as changed as the relay is enabled or disabled."

    )
    String[] announced();
}

