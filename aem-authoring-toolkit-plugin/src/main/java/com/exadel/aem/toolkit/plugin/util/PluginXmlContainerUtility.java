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

import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.exadel.aem.toolkit.api.annotations.main.ClassMember;
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
public class PluginXmlContainerUtility {
    private static final String DUPLICATE_FIELDS_MESSAGE_TEMPLATE = "Field named \"%s\" in class \"%s\" " +
            "collides with the field having same name in superclass \"%s\". This may cause unexpected behavior";

    /**
     * Default (private) constructor
     */
    private PluginXmlContainerUtility() {
    }

    /**
     * Retrieves the list of sources that define widgets assignable to the current container.
     * This is performed by calling {@link PluginReflectionUtility#getAllSources(Class)}
     * with additional predicates that allow to sort out sources (class members) set to be ignored at either
     * the "member itself" level and at "declaring class" level. Afterwards the non-widget fields are sorted out
     * @param current Current {@link Source} instance
     * @param useReferredClass True to use {@link Source#getProcessedClass()} to look for ignored members (this is the case
     *                         for {@code Multifield} or {@code FieldSet}-bound members);
     *                         False to use same {@link Source#getContainerClass()} as for the rest of method logic
     * @return {@code List<Source>} containing renderable members, or an empty collection
     */
    public static List<Source> getPlaceableSources(Source current, boolean useReferredClass) {
        Class<?> containerType = current.getContainerClass();
        Class<?> componentType = useReferredClass ? current.getProcessedClass() : containerType;
        // Build the collection of ignored fields that may be defined at field level and at nesting class level
        // (apart from those defined for the container class itself)
        Stream<ClassMember> classLevelIgnoredFields = componentType.isAnnotationPresent(IgnoreFields.class)
            ? Arrays.stream(componentType.getAnnotation(IgnoreFields.class).value())
            .map(classField -> PluginObjectUtility.modifyIfDefault(classField,
                ClassMember.class,
                DialogConstants.PN_SOURCE_CLASS,
                componentType))
            : Stream.empty();
        Stream<ClassMember> fieldLevelIgnoredFields = current.adaptTo(IgnoreFields.class) != null
            ? Arrays.stream(current.adaptTo(IgnoreFields.class).value())
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
        // Return the filtered field list
        Predicate<Member> nonIgnoredFields = PluginObjectPredicates.getNotIgnoredMembersPredicate(allIgnoredFields);
        Predicate<Member> dialogFields = DialogWidgets::isPresent;
        return PluginReflectionUtility.getAllSources(containerType, Arrays.asList(nonIgnoredFields, dialogFields));
    }

    /**
     * Processes the specified {@link Member}s and appends the generated XML markup to the specified container element
     * @param sources List of {@code Member}s of a component's Java class
     * @param container {@link Target} definition of a pre-defined widget container
     */
    public static void append(List<Source> sources, Target container) {
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
            checkForDuplicateFields(itemsElement, managedFields);
        }
    }

    /**
     * Tests the provided collection of fields for possible duplications (fields that generate nodes sharing
     * the same tag name), and throws an exception if a field from a superclass is positioned below the correspondent
     * field from a subclass, therefore, will have precedence
     * @param container XML definition of an immediate parent for widget nodes (typically, an {@code items} element)
     * @param managedFields {@code Map<Field, String>} that matches rendered fields to corresponding element names
     */
    private static void checkForDuplicateFields(Target container, Map<Source, String> managedFields) {
        List<String> childElementsTagNames = container.getChildren().stream()
                .map(Target::getName)
                .collect(Collectors.toList());
        if (childElementsTagNames.size() == new HashSet<>(childElementsTagNames).size()) {
            return;
        }
        for (String tagName : childElementsTagNames) {
            checkForDuplicateFields(tagName, managedFields);
        }
    }

    /**
     * Tests the provided
     * collection of fields and a particular duplicating tag name. Throws an exception if a field from a superclass
     * is positioned below the corresponding field from a subclass, therefore, will have precedence
     * @param tagName String representing the tag name in question
     * @param managedFields {@code Map<Field, String>} that matches rendered fields to corresponding element names
     */
    private static void checkForDuplicateFields(String tagName, Map<Source, String> managedFields) {
        LinkedList<Source> sameNameFields = managedFields.entrySet().stream()
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
