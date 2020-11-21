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

package com.exadel.aem.toolkit.core.util;

import java.lang.reflect.Member;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;

import com.exadel.aem.toolkit.core.exceptions.InvalidFieldContainerException;
import com.exadel.aem.toolkit.core.handlers.widget.DialogWidget;
import com.exadel.aem.toolkit.core.handlers.widget.DialogWidgets;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;

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
     * Processes the specified {@link Member}s and appends the generated XML markup to the specified container element
     * @param sources List of {@code Member}s of a component's Java class
     * @param container {@link Target} definition of a pre-defined widget container
     */
    public static void append(List<Source> sources, Target container) {
        Map<Source, String> managedFields = new LinkedHashMap<>();
        Target itemsElement = container.child(DialogConstants.NN_ITEMS);

        for (Source source : sources) {
            DialogWidget widget = DialogWidgets.fromSourceFacade(source);
            if (widget == null) {
                continue;
            }
            Target newElement = widget.appendTo(source, itemsElement);
            managedFields.put(source, newElement.getName());
        }

        if (!container.listChildren().isEmpty()) {
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
        List<String> childElementsTagNames = container.listChildren().stream()
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
                        ((Member) sameNameFieldsByOrigin.getLast().getSource()).getName(),
                        ((Member) sameNameFieldsByOrigin.getLast().getSource()).getDeclaringClass().getSimpleName(),
                        ((Member) sameNameFields.getLast().getSource()).getDeclaringClass().getSimpleName())));
    }
}
