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
package com.exadel.aem.toolkit.plugin.handlers.placement;

import java.util.List;

import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.plugin.utils.ClassUtil;

/**
 * Collects and manages information on Java class members that can be rendered in a particular container, such as a
 * dialog with its tabs or panels, or else an in-dialog container widget. This registry is designed to be shared among
 * various handlers to avoid rendering the same member several times
 */
public class MembersRegistry {

    private MembersRegistry parent;
    private final List<Source> members;

    /**
     * Creates a new independent registry instance
     * @param source {@code Source} instance used as the data supplier for the markup
     */
    public MembersRegistry(Source source) {
        members = ClassUtil.getSources(source.adaptTo(Class.class));
    }

    /**
     * Creates a new dependent registry instance. This one will respect a parent registry with members stored in it, and
     * will take account of additional members (e.g. added to a container "from outside" in a later stage of rendering
     * (via {@code @Place} or a similar mechanism)
     * @param parent  {@link MembersRegistry} instance that stores the basic set of class members
     * @param members List of additional {@code Source} objects representing class members
     */
    public MembersRegistry(MembersRegistry parent, List<Source> members) {
        this.parent = parent;
        this.members = members;
    }

    /**
     * Retrieves a list of members currently available for placement (those that have not been already placed in a
     * container)
     * @return List of {@code Source}s representing class members; possibly an empty list
     */
    public List<Source> getAvailable() {
        return members;
    }

    /**
     * Certifies that the given member has been placed in a container and is not available for further placement
     * @param member {@code Source} object that represents a class member
     */
    public void checkIn(Source member) {
        if (parent != null) {
            parent.checkIn(member);
        }
        members.remove(member);
    }
}
