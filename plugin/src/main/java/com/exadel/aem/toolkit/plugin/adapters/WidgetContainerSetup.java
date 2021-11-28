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
package com.exadel.aem.toolkit.plugin.adapters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.layouts.Accordion;
import com.exadel.aem.toolkit.api.annotations.layouts.FixedColumns;
import com.exadel.aem.toolkit.api.annotations.layouts.Tabs;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.handlers.Adapts;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.handlers.containers.Section;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;

/**
 * Adapts a {@link Source} object that is considered to be a Java class member marked with any of the multi-section
 * container-type widget annotations, such as {@link com.exadel.aem.toolkit.api.annotations.layouts.Accordion} or {@link
 * com.exadel.aem.toolkit.api.annotations.layouts.Tabs}, to retrieve information on the declared sections
 */
@Adapts(Source.class)
public class WidgetContainerSetup {

    private static final Predicate<Target> CONTAINER_PREDICATE = node -> ResourceTypes.CONTAINER.equals(node.getAttribute(DialogConstants.PN_SLING_RESOURCE_TYPE));
    private static final Predicate<Target> WIDGET_NODE_PREDICATE = node -> !node.getAttribute(DialogConstants.PN_SLING_RESOURCE_TYPE, StringUtils.EMPTY).isEmpty();

    private final Source source;
    private List<Section> sections;
    private String titleHierarchy;

    /**
     * Instance constructor per the {@link Adapts} contract
     * @param source {@code Source} object that will be used for extracting data
     */
    public WidgetContainerSetup(Source source) {
        this.source = source;
    }

    /**
     * Initializes in the current instance the hierarchy of ancestral containers represented by their titles,
     * slash-separated
     * @param value {@code Target} instance that manifests the "current" container (the one built upon the {@code
     *              Source} from which this instance is adapted). Calls to this method can be chained
     * @return Current instance
     */
    public WidgetContainerSetup useHierarchyFrom(Target value) {
        if (value != null) {
            titleHierarchy = getTitleHierarchy(value);
        }
        return this;
    }

    /**
     * Retrieves whether the current source represents a Java class member annotated with any of the multi-section
     * container-type widget annotations
     * @return True or false
     */
    public boolean isPresent() {
        return source != null
            && Stream.of(Accordion.class, FixedColumns.class, Tabs.class)
            .anyMatch(annotationType -> source.tryAdaptTo(annotationType).isPresent());
    }

    /**
     * Retrieves container sections declared by the current source
     * @return Collection of {@link Section} objects
     */
    public List<Section> getSections() {
        if (source == null) {
            return Collections.emptyList();
        }
        if (sections != null) {
            return sections;
        }
        if (source.tryAdaptTo(Tabs.class).isPresent()) {
            sections = Arrays
                .stream(source.adaptTo(Tabs.class).value())
                .map(tab -> {
                    Section newSection = Section.from(tab, false);
                    newSection.setTitlePrefix(titleHierarchy);
                    return newSection; })
                .collect(Collectors.toList());

        } else if (source.tryAdaptTo(Accordion.class).isPresent()) {
            sections = Arrays
                .stream(source.adaptTo(Accordion.class).value())
                .map(accordionPanel -> {
                    Section newSection = Section.from(accordionPanel, false);
                    newSection.setTitlePrefix(titleHierarchy);
                    return newSection; })
                .collect(Collectors.toList());

        } else if (source.tryAdaptTo(FixedColumns.class).isPresent()) {
            sections = Arrays
                .stream(source.adaptTo(FixedColumns.class).value())
                .map(column -> {
                    Section newSection = Section.from(column, false);
                    newSection.setTitlePrefix(titleHierarchy);
                    return newSection; })
                .collect(Collectors.toList());
        }
        return sections;
    }

    /* ---------------
       Utility methods
       --------------- */

    private static String getTitleHierarchy(Target value) {
        List<String> result = new ArrayList<>();
        Target closestContainer = value.findParent(CONTAINER_PREDICATE);
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
            result.add(0, closestContainer.getAttribute(DialogConstants.PN_JCR_TITLE, StringUtils.EMPTY));
            closestContainer = closestWidgetNode.findParent(CONTAINER_PREDICATE);
        }
        return String.join(CoreConstants.SEPARATOR_SLASH, result);
    }
}
