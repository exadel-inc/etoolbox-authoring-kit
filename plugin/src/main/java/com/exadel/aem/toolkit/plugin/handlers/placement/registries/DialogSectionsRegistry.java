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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

import com.exadel.aem.toolkit.api.annotations.layouts.Accordion;
import com.exadel.aem.toolkit.api.annotations.layouts.AccordionPanel;
import com.exadel.aem.toolkit.api.annotations.layouts.Column;
import com.exadel.aem.toolkit.api.annotations.layouts.FixedColumns;
import com.exadel.aem.toolkit.api.annotations.layouts.Tab;
import com.exadel.aem.toolkit.api.annotations.layouts.Tabs;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.meta.Scopes;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.handlers.placement.sections.Section;
import com.exadel.aem.toolkit.plugin.utils.ClassUtil;

/**
 * Extends {@link SectionsRegistry} to collect and manage information on dialog layout sections (such as dialog tabs,
 * accordion panels, or columns)
 */
class DialogSectionsRegistry extends SectionsRegistry {

    /**
     * Creates a new registry instance
     * @param source          {@code Source} instance used as the data supplier for the markup
     * @param target          The root of rendering for the current component
     * @param annotationTypes Collection of {@code Class<?>} objects representing types of container sections to
     *                        process
     */
    DialogSectionsRegistry(Source source, Target target, List<Class<? extends Annotation>> annotationTypes) {
        super(
            collectSections(source.adaptTo(Class.class), target.getScope(), annotationTypes),
            collectIgnored(source));
    }

    /**
     * Retrieves a collection of container sections derived from the specified source class. A hierarchy of ancestor
     * classes is built to take into account inherited sections
     * @param componentClass  {@code Class<?>} instance used as the source of markup
     * @param scope           String value defining whether to handle the current Java class as a {@code Dialog} source
     *                        or a {@code DesignDialog} source
     * @param annotationTypes One or more {@code Class<?>} objects representing types of container sections to process
     * @return Ordered list of container sections
     */
    private static List<Section> collectSections(
        Class<?> componentClass,
        String scope,
        List<Class<? extends Annotation>> annotationTypes) {

        // Retrieve superclasses of the current class, from the top of the hierarchy to the most immediate ancestor,
        // populate container section registry, and store members that are within @Tab or @AccordionPanel-marked classes
        // (because we will not have access to them later)
        List<Section> containerSectionsFromSuperClasses = collectSections(
            ClassUtil.getInheritanceTree(componentClass, false),
            scope,
            annotationTypes);

        // Retrieve tabs or accordion panels of the current class same way
        List<Section> containerSectionsFromCurrentClass = collectSections(
            Collections.singletonList(componentClass),
            scope,
            annotationTypes);

        return mergeSectionsFromCurrentClassAndSuperclasses(
            containerSectionsFromCurrentClass,
            containerSectionsFromSuperClasses);
    }

    /**
     * Retrieves a collection of container sections derived from the specified hierarchical collection of classes
     * @param hierarchy       The {@code Class<?>}-es to search for defined container items
     * @param scope           String value defining whether to handle the current Java class as a {@code Dialog} source
     *                        or a {@code DesignDialog} source
     * @param annotationTypes One or more {@code Class<?>} objects representing types of container sections to process
     * @return Ordered list of container sections
     */
    private static List<Section> collectSections(
        List<Class<?>> hierarchy,
        String scope,
        List<Class<? extends Annotation>> annotationTypes) {

        List<Section> result = new ArrayList<>();
        for (Class<?> classEntry : hierarchy) {
            appendSectionsFromNestedClasses(result, classEntry, annotationTypes);
            appendSectionsFromCurrentClass(result, classEntry, scope);
        }
        return result;
    }

    /**
     * Puts all sections, such as tabs or accordion panels, from the nested classes of the current class into the
     * accumulating collection
     * @param accumulator     The collection of container sections
     * @param componentClass  {@code Class<?>} instance used as the source of markup
     * @param annotationTypes One or more {@code Class<?>} objects representing types of container sections to process
     */
    private static void appendSectionsFromNestedClasses(
        List<Section> accumulator,
        Class<?> componentClass,
        List<Class<? extends Annotation>> annotationTypes) {

        List<Class<?>> nestedClasses = Arrays.stream(componentClass.getDeclaredClasses())
            .filter(nestedCls -> annotationTypes.stream().anyMatch(nestedCls::isAnnotationPresent))
            .collect(Collectors.toList());
        Collections.reverse(nestedClasses);
        for (Class<?> nestedClass : nestedClasses) {
            Annotation matchedAnnotation = annotationTypes
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
     * @param scope          String value defining whether to handle the current Java class as a {@code Dialog} source
     *                       or a {@code DesignDialog} source
     */
    @SuppressWarnings("deprecation") // Processing of container.Tab class and Dialog#tabs() method is retained for
    // compatibility and will be removed in a version after 2.0.2
    private static void appendSectionsFromCurrentClass(List<Section> accumulator, Class<?> componentClass, String scope) {
        com.exadel.aem.toolkit.api.annotations.container.Tab[] legacyTabs = null;
        Tab[] tabs = null;
        AccordionPanel[] panels = null;
        Column[] columns = null;

        if (Scopes.CQ_DIALOG.equals(scope) && componentClass.getDeclaredAnnotation(Dialog.class) != null) {
            Dialog dialogAnnotation = componentClass.getDeclaredAnnotation(Dialog.class);
            legacyTabs = dialogAnnotation.tabs();
        }
        if (componentClass.getDeclaredAnnotation(Tabs.class) != null) {
            tabs = componentClass.getDeclaredAnnotation(Tabs.class).value();
        } else if (componentClass.getDeclaredAnnotation(Accordion.class) != null) {
            panels = componentClass.getDeclaredAnnotation(Accordion.class).value();
        } else if (componentClass.getDeclaredAnnotation(FixedColumns.class) != null) {
            columns = componentClass.getDeclaredAnnotation(FixedColumns.class).value();
        }
        Stream.of(
            ArrayUtils.nullToEmpty(legacyTabs),
            ArrayUtils.nullToEmpty(tabs),
            ArrayUtils.nullToEmpty(panels),
            ArrayUtils.nullToEmpty(columns))
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
        // we consider that the "proper" order of contained items is defined right here, and place contained items
        // from the current class first, then the rest of the contained items.
        // Otherwise, we consider the contained items of the current class to be an "addendum" of contained items
        // from superclasses and put them in the end
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
     * @param primary   List of {@code SectionHelper} instances. The order of this list will be preserved
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
