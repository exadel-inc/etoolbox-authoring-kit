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
package com.exadel.aem.toolkit.plugin.handlers.placement.sections;

import com.exadel.aem.toolkit.api.handlers.Target;

/**
 * Represents the notion of a container section that should be ignored in the current buildup. This section can have
 * its members much like an ordinary one. Attaching members to this section is a logical procedure which means that
 * the members will not be rendered but at the same time will be thought of as rightfully processed
 */
class IgnoredSection extends Section {

    private final String title;

    /**
     * Instance constructor
     * @param title Title of the section
     */
    IgnoredSection(String title) {
        super(false);
        this.title = title;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isIgnored() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Target createItemsContainer(Target host) {
        return null;
    }
}
