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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.container.IgnoreTabs;
import com.exadel.aem.toolkit.api.annotations.layouts.Accordion;
import com.exadel.aem.toolkit.api.annotations.layouts.AccordionPanel;
import com.exadel.aem.toolkit.api.annotations.layouts.Tab;
import com.exadel.aem.toolkit.api.annotations.layouts.Tabs;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.meta.Scopes;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.Ignore;
import com.exadel.aem.toolkit.api.handlers.Adapts;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.handlers.containers.Section;
import com.exadel.aem.toolkit.plugin.utils.ClassUtil;

/**
 * Adapts a {@link Source} object that is considered to be a Java class having any of the layout annotations, such as
 * {@link Accordion} or {@link Tabs}, to retrieve information on the declared sections
 */
@Adapts(Source.class)
public class DialogContainerSetup {

    private final Source source;
    private String scope = Scopes.CQ_DIALOG;
    @SuppressWarnings("deprecation") // Processing of container.Tab class is retained for
                                     // compatibility and will be removed in a version after 2.0.2
    private List<Class<? extends Annotation>> inClassAnnotations = Arrays.asList(
        Tab.class,
        com.exadel.aem.toolkit.api.annotations.container.Tab.class
    );

    private List<Section> sections;
    private List<String> ignoredSections;

    /**
     * Instance constructor per the {@link Adapts} contract
     * @param source {@code Source} object that will be used for extracting data
     */
    public DialogContainerSetup(Source source) {
        this.source = source;
    }

    /**
     * Assigns a scope to the current instance. This value is used to detect what container annotation should be the
     * provided {@code Source} be adapted to. Calls to this method can be chained
     * @param value Non-blank string; a value of {@link Target#getScope()} is expected
     * @return Current instance
     */
    public DialogContainerSetup useScope(String value) {
        scope = StringUtils.defaultIfBlank(value, scope);
        return this;
    }

    /**
     * Assigns to the current instance a collection of types of in-class annotations to look for. These are annotations
     * that can be used with the nested classes belonging to the current class or any of its superclasses. Ex.: {@link Tab}.
     * Calls to this method can be chained
     * @param value Non-empty list of {@code Class} instances
     * @return Current instance
     */
    public DialogContainerSetup useInClassAnnotations(List<Class<? extends Annotation>> value) {
        if (value != null && !value.isEmpty()) {
            inClassAnnotations = value;
        }
        return this;
    }

    /**
     * Retrieves container sections declared by the current source
     * @return Collection of {@link Section} objects, non-null
     */
    public List<Section> getSections() {
        if (source == null) {
            return Collections.emptyList();
        }
        if (sections != null) {
            return sections;
        }
        sections = getSectionsInternal(source.adaptTo(Class.class), scope, inClassAnnotations);
        return sections;
    }

    /**
     * Retrieves the names of container sections set to be ignored
     * @return Collection of strings, non-null
     */
    @SuppressWarnings("deprecation") // Processing of IgnoreTabs annotation is retained for
    // compatibility and will be removed in a version after 2.0.2
    public List<String> getIgnoredSections() {
        if (source == null) {
            return Collections.emptyList();
        }
        if (ignoredSections != null) {
            return ignoredSections;
        }

        ignoredSections = new ArrayList<>();
        if (source.tryAdaptTo(IgnoreTabs.class).isPresent()) {
            ignoredSections = Arrays.asList(source.adaptTo(IgnoreTabs.class).value());
        }
        if (source.tryAdaptTo(Ignore.class).isPresent()
            && source.adaptTo(Ignore.class).sections().length > 0) {
            ignoredSections.addAll(Arrays.asList(source.adaptTo(Ignore.class).sections()));
        }
        return ignoredSections;
    }

    /* ---------------
       Utility methods
       --------------- */

    /**
     * Retrieves a collection of container sections derived from the specified source class. In order to take inherited
     * sections into account, a hierarchy of ancestor classes is built
     * @param componentClass    {@code Class<?>} instance used as the source of markup
     * @param scope             String value defining whether to handle the current Java class as a {@code Dialog}
     *                          source, or a {@code DesignDialog} source
     * @param annotationClasses One or more {@code Class<?>} objects representing types of container sections to
     *                          process
     * @return Ordered list of container sections
     */
    private static List<Section> getSectionsInternal(
        Class<?> componentClass,
        String scope,
        List<Class<? extends Annotation>> annotationClasses) {

        // Retrieve superclasses of the current class, from top of the hierarchy to the most immediate ancestor,
        // populate container section registry and store members that are within @Tab or @AccordionPanel-marked classes
        // (because we will not have access to them later)
        List<Section> containerSectionsFromSuperClasses = getSectionsInternal(
            ClassUtil.getInheritanceTree(componentClass, false),
            scope,
            annotationClasses);

        // Retrieve tabs or accordion panels of the current class same way
        List<Section> containerSectionsFromCurrentClass = getSectionsInternal(
            Collections.singletonList(componentClass),
            scope,
            annotationClasses);

        return mergeSectionsFromCurrentClassAndSuperclasses(
            containerSectionsFromCurrentClass,
            containerSectionsFromSuperClasses);
    }

    /**
     * Retrieves a collection of container sections derived from the specified hierarchical collection of classes
     * @param hierarchy         The {@code Class<?>}-es to search for defined container items
     * @param scope             String value defining whether to handle the current Java class as a {@code Dialog}
     *                          source, or a {@code DesignDialog} source
     * @param annotationClasses One or more {@code Class<?>} objects representing types of container sections to
     *                          process
     * @return Ordered list of container sections
     */
    private static List<Section> getSectionsInternal(
        List<Class<?>> hierarchy,
        String scope,
        List<Class<? extends Annotation>> annotationClasses) {

        List<Section> result = new ArrayList<>();
        for (Class<?> classEntry : hierarchy) {
            appendSectionsFromNestedClasses(result, classEntry, annotationClasses);
            appendSectionsFromCurrentClass(result, classEntry, scope);
        }
        return result;
    }

    /**
     * Puts all sections, such as tabs or accordion panels, from the nested classes of the current class into the
     * accumulating collection
     * @param accumulator       The collection of container sections
     * @param componentClass    {@code Class<?>} instance used as the source of markup
     * @param annotationClasses One or more {@code Class<?>} objects representing types of container sections to
     *                          process
     */
    private static void appendSectionsFromNestedClasses(
        List<Section> accumulator,
        Class<?> componentClass,
        List<Class<? extends Annotation>> annotationClasses) {

        List<Class<?>> nestedClasses = Arrays.stream(componentClass.getDeclaredClasses())
            .filter(nestedCls -> annotationClasses.stream().anyMatch(nestedCls::isAnnotationPresent))
            .collect(Collectors.toList());
        Collections.reverse(nestedClasses);
        for (Class<?> nestedClass : nestedClasses) {
            Annotation matchedAnnotation = annotationClasses
                .stream()
                .filter(nestedClass::isAnnotationPresent)
                .findFirst()
                .map(nestedClass::getDeclaredAnnotation)
                .orElse(null);
            Section section = Section.from(matchedAnnotation, true);
            if (section == null) {
                continue;
            }
            section.getSources().addAll(ClassUtil.getSources(nestedClass));
            accumulator.add(section);
        }
    }

    /**
     * Puts all sections, such as tabs or accordion panels, from the current Java class that represents a Granite UI
     * dialog into the accumulating collection
     * @param accumulator    The collection of container sections
     * @param componentClass {@code Class<?>} instance used as the source of markup
     * @param scope          String value defining whether to handle the current Java class as a {@code Dialog} source,
     *                       or a {@code DesignDialog} source
     */
    @SuppressWarnings("deprecation") // Processing of container.Tab class and Dialog#tabs() method is retained for
    // compatibility and will be removed in a version after 2.0.2
    private static void appendSectionsFromCurrentClass(List<Section> accumulator, Class<?> componentClass, String scope) {
        com.exadel.aem.toolkit.api.annotations.container.Tab[] legacyTabs = null;
        Tab[] tabs = null;
        AccordionPanel[] panels = null;

        if (Scopes.CQ_DIALOG.equals(scope) && componentClass.getDeclaredAnnotation(Dialog.class) != null) {
            Dialog dialogAnnotation = componentClass.getDeclaredAnnotation(Dialog.class);
            legacyTabs = dialogAnnotation.tabs();
        }
        if (componentClass.getDeclaredAnnotation(Tabs.class) != null) {
            tabs = componentClass.getDeclaredAnnotation(Tabs.class).value();
        } else if (componentClass.getDeclaredAnnotation(Accordion.class) != null) {
            panels = componentClass.getDeclaredAnnotation(Accordion.class).value();
        }
        Stream.of(
            ArrayUtils.nullToEmpty(legacyTabs),
            ArrayUtils.nullToEmpty(tabs),
            ArrayUtils.nullToEmpty(panels))
            .flatMap(Arrays::stream)
            .forEach(section -> accumulator.add(Section.from((Annotation) section, true)));
    }

    /**
     * Composes the "overall" collection of container sections (such as tabs or accordion panels) by merging sections
     * from the current class and from the hierarchy of superclasses in the proper order
     * @param sectionsFromCurrentClass Collection of sections defined in the current class
     * @param sectionsFromSuperClasses Collection of sections defined in the hierarchy of superclasses
     * @return Resulting ordered list of sections
     */
    private static List<Section> mergeSectionsFromCurrentClassAndSuperclasses(
        List<Section> sectionsFromCurrentClass,
        List<Section> sectionsFromSuperClasses) {
        // Whether the current class has any sections that match sections from superclasses,
        // we consider that the "right" order of container items is defined herewith, and place container items
        // from the current class first, then rest of the container items.
        // Otherwise, we consider the container items of the current class to be an "addendum" of container items
        // from superclasses, and put them in the end
        boolean sectionTitlesIntersect = sectionsFromCurrentClass
            .stream()
            .anyMatch(section -> sectionsFromSuperClasses.stream().anyMatch(otherSection -> otherSection.getTitle().equals(section.getTitle())));
        if (sectionTitlesIntersect) {
            return mergeSections(sectionsFromCurrentClass, sectionsFromSuperClasses);
        }
        return mergeSections(sectionsFromSuperClasses, sectionsFromCurrentClass);
    }

    /**
     * Composes a synthetic collection from two separate lists of {@link Section} objects with priority defined
     * @param primary   List of {@code SectionHelper} instances the order of which will be preserved
     * @param secondary List of {@code SectionHelper} instances that will supplement the primary list
     * @return Resulting ordered list of sections
     */
    private static List<Section> mergeSections(List<Section> primary, List<Section> secondary) {
        List<Section> result = new ArrayList<>(primary);
        for (Section other : secondary) {
            Section matchingFromCurrentClass = result
                .stream()
                .filter(section -> section.getTitle().equals(other.getTitle()))
                .findFirst()
                .orElse(null);
            if (matchingFromCurrentClass != null) {
                matchingFromCurrentClass.merge(other);
            } else {
                result.add(other);
            }
        }
        return result;
    }
}
