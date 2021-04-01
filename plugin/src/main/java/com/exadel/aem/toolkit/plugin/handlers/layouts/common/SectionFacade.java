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
package com.exadel.aem.toolkit.plugin.handlers.layouts.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;

import com.exadel.aem.toolkit.api.annotations.layouts.AccordionPanel;
import com.exadel.aem.toolkit.api.annotations.layouts.Tab;
import com.exadel.aem.toolkit.api.handlers.Target;

/**
 * Represents an abstraction of a container section such as {@code Tab} or {@code AccordionPanel}
 * that contains a list of members designed to be rendered within this container element.
 * Used to compose an ordered "container element registry" for a component class
 */
abstract class SectionFacade {

    private final List<Member> members;
    private final boolean isLayout;

    /**
     * Creates a new {@code SectionHelper} with an empty list of associated members
     * @param isLayout True if the current section is a dialog layout section; false if it is a dialog widget section
     */
    SectionFacade(boolean isLayout) {
        this.isLayout = isLayout;
        this.members = new ArrayList<>();
    }

    /**
     * Retrieves the {@code title} value of the annotation this section is bound to
     * @return String value
     */
    abstract String getTitle();

    /**
     * Returns true if the current section is a dialog layout section; false if it is a dialog widget section
     * @return True or false
     */
    boolean isLayoutSection() {
        return isLayout;
    }

    /**
     * Gets the collection of members associated with the current container
     * @return {@code List<Member>} instance, non-null
     */
    List<Member> getMembers() {
        return members;
    }

    /**
     * Produces for further rendering the data structure representing a container section with required attributes populated
     * @param container {@code Target} that will represent the section parent
     * @return {code Target} element being a child of the given target, or the provided target itself
     */
    abstract Target createItemsContainer(Target container);

    /**
     * Merges a foreign {@code ContainerInfo} to the current instance, basically by adding other instance's fields
     * while preserving the same  reference
     * @param other Foreign {@code ContainerInfo} object
     */
    void merge(SectionFacade other) {
        this.members.addAll(other.getMembers());
    }

    /**
     * Creates a new {@link SectionFacade} instance for the {@code Annotation} given considering its type
     * @param annotation The {@code Annotation} object to wrap
     * @return {@code SectionHelper} instance, or null in case of an invalid {@code annotation} argument
     */
    @SuppressWarnings("deprecation") // Processing of container.Tab is retained for compatibility and will be removed
                                     // in a version after 2.0.2
    static SectionFacade from(Annotation annotation) {
        if (annotation == null) {
            return null;
        }
        if (annotation.annotationType().equals(Tab.class)) {
            return new TabFacade((Tab) annotation, true);
        }
        if (annotation.annotationType().equals(AccordionPanel.class)) {
            return new AccordionPanelFacade((AccordionPanel) annotation, true);
        }
        return new LegacyTabFacade((com.exadel.aem.toolkit.api.annotations.container.Tab) annotation, true);
    }
}
