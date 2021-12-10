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
package com.exadel.aem.toolkit.plugin.targets;

import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.handlers.placement.MembersRegistry;

/**
 * Presents an abstraction of a "root" target entity for rendering AEM components and their configurations. This can
 * be thought of as a root node of a Granite UI dialog, an in-place edit config or another JCR entity of that kind.
 * The {@code RootTarget} is special for its ability to hold a registry of associated Java class members
 * @see MembersRegistry
 */
public interface RootTarget extends Target {

    /**
     * Retrieves a {@link MembersRegistry} associated with this instance
     * @return {@code MembersRegistry} object, or null
     */
    MembersRegistry getMembers();

    /**
     * Assigns a {@link MembersRegistry} object to this instance
     * @param value {@code MembersRegistry} object, non-null value expected
     */
    void setMembers(MembersRegistry value);
}
