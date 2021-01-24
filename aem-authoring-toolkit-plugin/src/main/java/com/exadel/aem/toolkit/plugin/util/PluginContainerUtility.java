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
import com.exadel.aem.toolkit.plugin.util.predicate.Filtering;
import com.exadel.aem.toolkit.plugin.util.predicate.Sorting;

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
     * @param useReportingClass True to use {@link Source#getReportingClass()} to look for ignored members (this is
     *                         the case for {@code Multifield} or {@code FieldSet}-bound members);
     *                         False to use same {@link Source#getValueType()} as for the rest of method logic
     * @return {@code List<Source>} containing placeable members, or an empty collection
     */
    public static List<Source> getContainerEntries(Source container, boolean useReportingClass) {
        Class<?> valueTypeClass = container.getValueType();
        Class<?> reportingClass = useReportingClass ? container.getReportingClass() : valueTypeClass;
        // Build the collection of ignored members at nesting class level
        // (apart from those defined for the container class itself)
        Stream<ClassMemberSettings> classLevelIgnoredMembers = Stream.empty();
        if (reportingClass.isAnnotationPresent(Ignore.class)) {
            classLevelIgnoredMembers = Arrays.stream(reportingClass.getAnnotation(Ignore.class).members())
                .map(memberPtr -> new ClassMemberSettings(memberPtr).populateDefaultSource(reportingClass));
        } else if (reportingClass.isAnnotationPresent(IgnoreFields.class)) {
            classLevelIgnoredMembers = Arrays.stream(reportingClass.getAnnotation(IgnoreFields.class).value())
                .map(memberPtr -> new ClassMemberSettings(memberPtr).populateDefaultSource(reportingClass));
        }
        // Now build collection of ignored members at member level
        Stream<ClassMemberSettings> fieldLevelIgnoredMembers = Stream.empty();
        if (container.adaptTo(Ignore.class) != null) {
            fieldLevelIgnoredMembers = Arrays.stream(container.adaptTo(Ignore.class).members())
                .map(memberPtr -> new ClassMemberSettings(memberPtr).populateDefaultSource(valueTypeClass));
        } else if (container.adaptTo(IgnoreFields.class) != null) {
            fieldLevelIgnoredMembers = Arrays.stream(container.adaptTo(IgnoreFields.class).value())
                .map(memberPtr -> new ClassMemberSettings(memberPtr).populateDefaultSource(valueTypeClass));
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
        Predicate<Source> nonIgnoredMembers = Filtering.getNotIgnoredSourcesPredicate(allIgnoredFields);
        Predicate<Source> dialogFields = DialogWidgets::isPresent;
        return PluginReflectionUtility.getAllSources(valueTypeClass, Arrays.asList(nonIgnoredMembers, dialogFields));
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
                .sorted(Sorting::compareByOrigin)
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
                        sameNameFieldsByOrigin.getLast().getReportingClass().getSimpleName(),
                        sameNameFields.getLast().getReportingClass().getSimpleName())));
    }
}
