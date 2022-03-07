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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.layouts.Accordion;
import com.exadel.aem.toolkit.api.annotations.layouts.FixedColumns;
import com.exadel.aem.toolkit.api.annotations.layouts.Place;
import com.exadel.aem.toolkit.api.annotations.layouts.Tabs;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.handlers.MemberSource;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.handlers.placement.sections.Section;
import com.exadel.aem.toolkit.plugin.sources.Sources;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;

/**
 * Extends {@link SectionsRegistry} to collect and manage information on container sections (such as tabs,
 * accordion panels, or columns) that reside within the dialog on the widget level
 */
class ContainerSectionsRegistry extends SectionsRegistry {

    private static final Predicate<Target> CONTAINER_PREDICATE = node -> ResourceTypes.CONTAINER.equals(node.getAttribute(DialogConstants.PN_SLING_RESOURCE_TYPE));
    private static final Predicate<Target> WIDGET_NODE_PREDICATE = node -> !node.getAttribute(DialogConstants.PN_SLING_RESOURCE_TYPE, StringUtils.EMPTY).isEmpty();

    /**
     * Creates a new registry instance
     * @param source {@code Source} instance used as the data supplier for the markup
     * @param target The root of rendering for the current component
     */
    public ContainerSectionsRegistry(Source source, Target target) {
        super(
            collectSections(source, getTitlePrefix(source, target)),
            collectIgnoredSections(source));
    }

    /**
     * Retrieves container sections declared by the provided source. Every section's title is prefixed with combined
     * titles of the upstream sections, e.g. if a widget-like tab container resides within a tabbed layout dialog, the
     * title of a tab is preserved as {@code My Dialog Tab/My internal tab}. This helps to accurately address a proper
     * container in e.g. {@code @Place} annotation
     * @param source      {@code Source} instance used as the data supplier for the markup
     * @param titlePrefix The prefix to use
     * @return Collection of {@link Section} objects
     */
    private static List<Section> collectSections(Source source, String titlePrefix) {
        if (source == null) {
            return Collections.emptyList();
        }
        if (source.tryAdaptTo(Tabs.class).isPresent()) {
            return Arrays
                .stream(source.adaptTo(Tabs.class).value())
                .map(tab -> {
                    Section newSection = Section.from(tab, false);
                    newSection.setTitlePrefix(titlePrefix);
                    return newSection; })
                .collect(Collectors.toList());

        } else if (source.tryAdaptTo(Accordion.class).isPresent()) {
            return Arrays
                .stream(source.adaptTo(Accordion.class).value())
                .map(accordionPanel -> {
                    Section newSection = Section.from(accordionPanel, false);
                    newSection.setTitlePrefix(titlePrefix);
                    return newSection; })
                .collect(Collectors.toList());

        } else if (source.tryAdaptTo(FixedColumns.class).isPresent()) {
            return Arrays
                .stream(source.adaptTo(FixedColumns.class).value())
                .map(column -> {
                    Section newSection = Section.from(column, false);
                    newSection.setTitlePrefix(titlePrefix);
                    return newSection; })
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Retrieves the prefix for the full "title-style" hierarchical address of the current container (e.g. {@code
     * "My Tab/My Accordion Panel/My Inner Tab"}). If this container has its {@code @Place} directive, the title is
     * according to the {@code @Place} value; otherwise it is defined by the titles of containers this one is
     * currently nested in
     * @param source {@code Source} instance matching the current container
     * @param target {@code Target} instance that represents the render of the current source
     * @return String value; an empty string is returned if parent containers have not been detected
     */
    private static String getTitlePrefix(Source source, Target target) {
        // First try to extract the "place" value from the given source
        Place placeSetting = source.adaptTo(Place.class);
        if (placeSetting != null && StringUtils.isNotBlank(placeSetting.value())) {
            return placeSetting.value();
        }
        // If no such value, collect data on what way the current target is nested into other named targets, and produce
        // a slash-delimited result
        List<String> resultChunks = new ArrayList<>();
        Target closestContainer = target.findParent(CONTAINER_PREDICATE);
        while (closestContainer != null) {
            Target closestWidgetNode = closestContainer.findParent(WIDGET_NODE_PREDICATE);
            boolean isContainerWidget =
                closestWidgetNode != null
                    && StringUtils.equalsAny(
                    closestWidgetNode.getAttribute(DialogConstants.PN_SLING_RESOURCE_TYPE),
                    ResourceTypes.ACCORDION,
                    ResourceTypes.TABS,
                    ResourceTypes.FIXED_COLUMNS);
            if (!isContainerWidget) {
                break;
            }
            resultChunks.add(0, closestContainer.getAttribute(DialogConstants.PN_JCR_TITLE, StringUtils.EMPTY));
            closestContainer = closestWidgetNode.findParent(CONTAINER_PREDICATE);
        }
        return String.join(CoreConstants.SEPARATOR_SLASH, resultChunks);
    }

    private static List<String> collectIgnoredSections(Source source) {
        Set<String> result = new HashSet<>(collectIgnored(source));
        result.addAll(collectIgnored(Sources.fromClass(source.adaptTo(MemberSource.class).getReportingClass())));
        return new ArrayList<>(result);
    }
}
