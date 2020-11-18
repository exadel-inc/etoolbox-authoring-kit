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

package com.exadel.aem.toolkit.core.handlers.widget;

import com.exadel.aem.toolkit.api.annotations.main.ClassMember;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.IgnoreFields;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.exadel.aem.toolkit.core.util.PluginObjectPredicates;
import com.exadel.aem.toolkit.core.util.PluginObjectUtility;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;

import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Mix-in utility class for widget handlers that contains the base logic for {@link Handler}s dealing with widget
 * collections, such as {@link com.exadel.aem.toolkit.api.annotations.widgets.FieldSet}
 * or {@link com.exadel.aem.toolkit.api.annotations.widgets.MultiField}
 */
interface WidgetSetHandler extends Handler, BiConsumer<Source, Target> {

    /**
     * Retrieves the list of fields applicable to the current container, by calling {@link PluginReflectionUtility#getAllSourceFacades(Class)} (Class)}
     * with additional predicates that allow to sort out the fields set to be ignored at source level and at nesting class
     * level, and then sort out the non-widget fields
     * @param source Current {@code Source} instance
     * @param containerType {@code Class} representing the type of the container
     * @return {@code List<SourceFacade>} containing renderable fields, or an empty collection
     */
    default List<Source> getContainerSourceFacades(Source source, Class<?> containerType) {
        // Extract type of the Java class being the current rendering source
        Class<?> componentType = source.getProcessedClass();
        // Build the collection of ignored fields that may be defined at source level and at nesting class level
        // (apart from those defined for the container class itself)
        Stream<ClassMember> classLevelIgnoredFields = componentType != null && componentType.isAnnotationPresent(IgnoreFields.class)
                ? Arrays.stream(componentType.getAnnotation(IgnoreFields.class).value())
                .map(classField -> PluginObjectUtility.modifyIfDefault(classField,
                        ClassMember.class,
                        DialogConstants.PN_SOURCE_CLASS,
                        componentType))
                : Stream.empty();
        Stream<ClassMember> fieldLevelIgnoredFields = source.adaptTo(IgnoreFields.class) != null
                ? Arrays.stream(source.adaptTo(IgnoreFields.class).value())
                .map(classField -> PluginObjectUtility.modifyIfDefault(classField,
                        ClassMember.class,
                        DialogConstants.PN_SOURCE_CLASS,
                        containerType))
                : Stream.empty();
        List<ClassMember> allIgnoredFields = Stream.concat(classLevelIgnoredFields, fieldLevelIgnoredFields)
                .filter(classField -> PluginReflectionUtility.getClassHierarchy(containerType).stream()
                        .anyMatch(superclass -> superclass.equals(classField.source())))
                .collect(Collectors.toList());

        // Create filters to sort out ignored fields (apart from those defined for the container class)
        // and to banish non-widget fields
        // Return the filtered source list
        Predicate<Member> nonIgnoredFields = PluginObjectPredicates.getNotIgnoredMembersPredicate(allIgnoredFields);
        Predicate<Member> dialogFields = DialogWidgets::isPresent;
        return PluginReflectionUtility.getAllSourceFacades(containerType, Arrays.asList(nonIgnoredFields, dialogFields));
    }
}
