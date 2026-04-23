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

@ObjectClassDefinition(name = "EToolbox Authoring Kit - Relay Configuration")
public @interface RelayConfig {

    @AttributeDefinition(
        name = "Enable",
        description = "Is the relay enabled?"
    )
    boolean enabled();

    @AttributeDefinition(
        name = "Path mappings",
        description = "List of path mapping rules."
    )
    String[] pathMappings();

    @AttributeDefinition(
        name = "User mappings",
        description = "Optional list of user mapping rules. " +
            "The mapping target is either \"user:password\" or \"registered_subservice\"."
    )
    String[] userMappings() default {};

    @AttributeDefinition(
        name = "Report paths",
        description = "Optional list of XPath expressions. " +
            "Target paths matching the list will be reported as changed as the relay is enabled or disabled."

    )
    String[] reportedPaths() default {};
}

