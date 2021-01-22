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

package com.exadel.aem.toolkit.plugin.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.exadel.aem.toolkit.api.annotations.main.ClassField;
import com.exadel.aem.toolkit.api.annotations.main.ClassMember;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.Ignore;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.IgnoreFields;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.exceptions.InvalidFieldContainerException;
import com.exadel.aem.toolkit.plugin.handlers.widget.DialogWidget;
import com.exadel.aem.toolkit.plugin.handlers.widget.DialogWidgets;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;

/**
 * Contains utility methods that handle adding nodes describing Granite widgets to a widget container node
 */
public class PluginContainerUtility {
    private static final String DUPLICATE_FIELDS_MESSAGE_TEMPLATE = "Field named \"%s\" in class \"%s\" " +
            "collides with the field having same name in superclass \"%s\". This may cause unexpected behavior";

    /**
     * Default (private) constructor
     */
    private PluginContainerUtility() {
    }

    /**
     * Retrieves the list of sources that manifest widgets assignable to the current container
     * (i.e. to a dialog-defining class, a tab-defining class, a fieldset panel-defining class, etc).
     * This is performed by calling {@link PluginReflectionUtility#getAllSources(Class)}
     * with additional predicates that allow to sort out sources (class members) set to be ignored at either
     * the "member itself" level and at "declaring class" level. Afterwards the non-widget fields are sorted out
     * @param container Current {@link Source} instance
     * @param useReferredClass True to use {@link Source#getProcessedClass()} to look for ignored members (this is the case
     *                         for {@code Multifield} or {@code FieldSet}-bound members);
     *                         False to use same {@link Source#getContainerClass()} as for the rest of method logic
     * @return {@code List<Source>} containing placeable members, or an empty collection
     */
    public static List<Source> getContainerEntries(Source container, boolean useReferredClass) {
        Class<?> declaringClass = container.getContainerClass();
        Class<?> currentlyReferredClass = useReferredClass ? container.getProcessedClass() : declaringClass;
        // Build the collection of ignored members at nesting class level
        // (apart from those defined for the container class itself)
        Stream<Annotation> classLevelIgnoredMembers = Stream.empty();
        if (currentlyReferredClass.isAnnotationPresent(Ignore.class)) {
            classLevelIgnoredMembers = Arrays.stream(currentlyReferredClass.getAnnotation(Ignore.class).members())
                .map(memberPtr -> PluginObjectUtility.modifyIfDefault(
                    memberPtr,
                    ClassMember.class,
                    DialogConstants.PN_SOURCE_CLASS,
                    currentlyReferredClass));
        } else if (currentlyReferredClass.isAnnotationPresent(IgnoreFields.class)) {
            classLevelIgnoredMembers = Arrays.stream(currentlyReferredClass.getAnnotation(IgnoreFields.class).value())
                .map(memberPtr -> PluginObjectUtility.modifyIfDefault(
                    memberPtr,
                    ClassField.class,
                    DialogConstants.PN_SOURCE_CLASS,
                    currentlyReferredClass));
        }
        // Now build collection of ignored members at member level
        Stream<Annotation> fieldLevelIgnoredMembers = Stream.empty();
        if (container.adaptTo(Ignore.class) != null) {
            fieldLevelIgnoredMembers = Arrays.stream(container.adaptTo(Ignore.class).members())
                .map(memberPtr -> PluginObjectUtility.modifyIfDefault(
                    memberPtr,
                    ClassMember.class,
                    DialogConstants.PN_SOURCE_CLASS,
                    declaringClass));
        } else if (container.adaptTo(IgnoreFields.class) != null) {
            fieldLevelIgnoredMembers = Arrays.stream(container.adaptTo(IgnoreFields.class).value())
                .map(memberPtr -> PluginObjectUtility.modifyIfDefault(
                    memberPtr,
                    ClassField.class,
                    DialogConstants.PN_SOURCE_CLASS,
                    declaringClass));
        }

        // Join the collections and make sure that only members from any of the superclasses of the current source's class
        // are present
        List<Annotation> allIgnoredFields = Stream
            .concat(classLevelIgnoredMembers, fieldLevelIgnoredMembers)
            .filter(memberPtr -> {
                Class<?> referencedClass = memberPtr instanceof ClassMember
                    ? ((ClassMember) memberPtr).source()
                    : ((ClassField) memberPtr).source();
                return PluginReflectionUtility.getClassHierarchy(declaringClass)
                    .stream()
                    .anyMatch(superclass -> superclass.equals(referencedClass));
            })
            .collect(Collectors.toList());

        // Create filters to sort out ignored fields (apart from those defined for the container class)
        // and to banish non-widget fields
        // Return the filtered field list
        Predicate<Member> nonIgnoredMembers = PluginObjectPredicates.getNotIgnoredMembersPredicate(allIgnoredFields);
        Predicate<Member> dialogFields = DialogWidgets::isPresent;
        return PluginReflectionUtility.getAllSources(declaringClass, Arrays.asList(nonIgnoredMembers, dialogFields));
    }

    /**
     * Appends provided {@link Source}s to the {@link Target} manifesting a container node
     * @param container {@link Target} manifesting a pre-defined widget container
     * @param sources List of sources, such as members of a Java class
     */
    public static void appendToContainer(Target container, List<Source> sources) {
        Map<Source, String> managedFields = new LinkedHashMap<>();
        Target itemsElement = container.getOrCreate(DialogConstants.NN_ITEMS);

        for (Source source : sources) {
            DialogWidget widget = DialogWidgets.fromSource(source);
            if (widget == null) {
                continue;
            }
            Target newElement = widget.appendTo(source, itemsElement);
            managedFields.put(source, newElement.getName());
        }

        if (!container.getChildren().isEmpty()) {
            checkForDuplicates(managedFields);
        }
    }

    /**
     * Tests the provided collection of members for possible duplicates (members that share the same tag name),
     * and throws an exception if a member from a superclass is positioned after the correspondent
     * member from a subclass, therefore, will "shadow" it and produce unexpected UI display
     * @param processedSources {@code Map} instance that matches renderable sources (Java class members)
     *                        to the genera tag names
     */
    private static void checkForDuplicates(Map<Source, String> processedSources) {
        Collection<String> processedTagNames = processedSources.values();
        Set<String> distinctTagNames = new HashSet<>(processedTagNames);
        if (distinctTagNames.size() ==  processedTagNames.size()) {
            return;
        }
        for (String tagName : distinctTagNames) {
            checkForDuplicates(tagName, processedSources);
        }
    }

    /**
     * Tests the provided collection of members for a particular duplicating tag name. Throws an exception
     * if a member from a superclass is positioned after the corresponding member from a subclass, therefore,
     * will "shadow" it and produce unexpected UI display
     * @param tagName String representing the tag name in question
     * @param managedMembers {@code Map} instance that matches renderable sources (class members)
     *                       to corresponding node names
     */
    private static void checkForDuplicates(String tagName, Map<Source, String> managedMembers) {
        LinkedList<Source> sameNameFields = managedMembers.entrySet().stream()
                .filter(entry -> entry.getValue().equals(tagName))
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(LinkedList::new));
        LinkedList<Source> sameNameFieldsByOrigin = sameNameFields.stream()
                .sorted(PluginObjectPredicates::compareByOrigin)
                .collect(Collectors.toCollection(LinkedList::new));

        if (sameNameFields.getLast().equals(sameNameFieldsByOrigin.getLast())) {
            return;
        }
        PluginRuntime
                .context()
                .getExceptionHandler()
                .handle(new InvalidFieldContainerException(String.format(
                        DUPLICATE_FIELDS_MESSAGE_TEMPLATE,
                        sameNameFieldsByOrigin.getLast().getName(),
                        sameNameFieldsByOrigin.getLast().getProcessedClass().getSimpleName(),
                        sameNameFields.getLast().getProcessedClass().getSimpleName())));
    }
}
