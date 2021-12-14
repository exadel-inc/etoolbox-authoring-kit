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
package com.exadel.aem.toolkit.plugin.handlers.placement.registries;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import com.exadel.aem.toolkit.api.annotations.container.IgnoreTabs;
import com.exadel.aem.toolkit.api.annotations.layouts.Accordion;
import com.exadel.aem.toolkit.api.annotations.layouts.FixedColumns;
import com.exadel.aem.toolkit.api.annotations.layouts.Tabs;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.Ignore;
import com.exadel.aem.toolkit.api.handlers.MemberSource;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.handlers.placement.sections.Section;

/**
 * Collects and manages information on the layout/container sections (such as dialog tabs, accordion panels, or else
 * in-dialog tab, accordion, column containers, etc.) belonging to a Granite UI dialog, or a similar markup structure
 */
public abstract class SectionsRegistry {

    private final List<Section> sections;

    /**
     * Instance constructor
     * @param availableSections List of {@link Section} instances
     * @param ignoredSections   List of string representing titles of ignored sections
     */
    SectionsRegistry(List<Section> availableSections, List<String> ignoredSections) {
        this.sections = new ArrayList<>(availableSections);
        for (String ignoredTitle : ignoredSections) {
            sections.stream().filter(section -> section.isMatch(ignoredTitle)).findFirst().ifPresent(sections::remove);
            sections.add(Section.ignored(ignoredTitle));
        }
    }

    /**
     * Retrieves the collection of {@code Section} objects available for the current markup
     * @return {@code List} instance
     */
    public List<Section> getAvailable() {
        return sections;
    }

    /* ---------------
       Utility methods
       --------------- */

    /**
     * Gets whether the given {@code Source} object can be used as a section registry base, i.e. carries a
     * section-initializing directive, such as a {@code Tabs} or {@code Accordion} annotation
     * @param source {@code Source} instance used as the data supplier for the markup
     * @return True or false
     */
    public static boolean isAvailableFor(Source source) {
        if (!(source instanceof MemberSource)) {
            return false;
        }
        return Stream.of(Tabs.class, Accordion.class, FixedColumns.class).anyMatch(sectionType -> source.tryAdaptTo(sectionType).isPresent());
    }

    /**
     * Retrieves the list of ignored sections' titles
     * @param source {@code Source} instance used as the data supplier for the markup
     * @return List of titles, or an empty list
     */
    @SuppressWarnings("deprecation") // Processing of IgnoreTabs annotation is retained for
    // compatibility and will be removed in a version after 2.0.2
    static List<String> collectIgnored(Source source) {
        List<String> result = new ArrayList<>();
        if (source.tryAdaptTo(IgnoreTabs.class).isPresent()) {
            result.addAll(Arrays.asList(source.adaptTo(IgnoreTabs.class).value()));
        }
        if (source.tryAdaptTo(Ignore.class).isPresent()
            && source.adaptTo(Ignore.class).sections().length > 0) {
            result.addAll(Arrays.asList(source.adaptTo(Ignore.class).sections()));
        }
        return result;
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
