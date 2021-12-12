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

import java.lang.annotation.Annotation;
import java.util.List;

import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.handlers.placement.containers.Section;

/**
 * Collects and manages information on the layout/container sections (such as dialog tabs, accordion panels, or else
 * in-dialog tab, accordion, column containers, etc.) belonging to a Granite UI dialog, or a similar markup structure
 */
public abstract class SectionsRegistry {

    private final List<Section> availableSections;
    private final List<String> ignoredSections;

    /**
     * Instance constructor
     * @param availableSections List of {@link Section} instances
     * @param ignoredSections   List of string representing titles of ignored sections
     */
    SectionsRegistry(List<Section> availableSections, List<String> ignoredSections) {
        this.availableSections = availableSections;
        this.ignoredSections = ignoredSections;
    }

    /**
     * Retrieves the collection of {@code Section} objects available for the current markup
     * @return {@code List} instance
     */
    public List<Section> getAvailable() {
        return availableSections;
    }

    /**
     * Retrieves the collection of strings representing titles of ignored sections
     * @return {@code List} instance
     */
    public List<String> getIgnored() {
        return ignoredSections;
    }

    /* ---------------
       Factory methods
       --------------- */

    /**
     * Creates an instance of {@code SectionsRegistry} that matches a Granite UI dialog with a particular layout
     * @param source          {@code Source} instance used as the data supplier for the markup
     * @param target          The root of rendering for the current component
     * @param annotationTypes Collection of {@code Class<?>} objects representing types of container sections to
     *                        process
     * @return {@code SectionRegistry} object that contains layout sections of a dialog
     */
    public static SectionsRegistry from(Source source, Target target, List<Class<? extends Annotation>> annotationTypes) {
        return new DialogSectionsRegistry(source, target, annotationTypes);
    }

    /**
     * Creates an instance of {@code SectionsRegistry} that matches a Granite UI component (such as Tabs or Accordion)
     * @param source {@code Source} instance used as the data supplier for the markup
     * @param target The root of rendering for the current component
     * @return {@code SectionRegistry} object that contains container sections of a Granite UI component
     */
    public static SectionsRegistry from(Source source, Target target) {
        return new ContainerSectionsRegistry(source, target);
    }
}
