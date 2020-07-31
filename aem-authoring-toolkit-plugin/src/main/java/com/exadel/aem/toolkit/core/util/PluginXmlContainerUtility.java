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

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ClassUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.exadel.aem.toolkit.core.exceptions.InvalidFieldContainerException;
import com.exadel.aem.toolkit.core.handlers.widget.DialogWidget;
import com.exadel.aem.toolkit.core.handlers.widget.DialogWidgets;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;

/**
 * Contains utility methods that handle adding nodes describing Granite widgets to a widget container node
 */
public class PluginXmlContainerUtility {
    private static final String DUPLICATE_FIELDS_MESSAGE_TEMPLATE = "Field name \"%s\" in class \"%s\": " +
            "collides with same name in superclass '%s'. This may cause unexpected behavior while saving data";

    /**
     * Default (private) constructor
     */
    private PluginXmlContainerUtility() {
    }

    /**
     * Processes the specified {@link Field}s and appends the generated XML markup to the specified container element
     * @param container XML definition of a pre-defined widget container
     * @param fields List of {@code Field}s of a component's Java class
     */
    public static void append(Element container, List<Field> fields) {
        Map<Field, String> managedFields = new LinkedHashMap<>();
        Element itemsElement = PluginRuntime.context().getXmlUtility().createNodeElement(DialogConstants.NN_ITEMS);
        container.appendChild(itemsElement);

        for (Field field : fields) {
            DialogWidget widget = DialogWidgets.fromField(field);
            if (widget == null) {
                continue;
            }
            Element newElement = widget.appendTo(itemsElement, field);
            managedFields.put(field, newElement.getTagName());
        }

        if (container.hasChildNodes()) {
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
    private static void checkForDuplicateFields(Element container, Map<Field, String> managedFields) {
        List<String> childElementsTagNames = IntStream
                .range(0, container.getChildNodes().getLength())
                .mapToObj(index -> container.getChildNodes().item(index))
                .map(Node::getNodeName)
                .collect(Collectors.toList());
        if (childElementsTagNames.size() == new HashSet<>(childElementsTagNames).size()) {
            return;
        }
        for (String tagName : childElementsTagNames) {
            checkForDuplicateFields(tagName, managedFields);
        }
    }

    /**
     * Called from {@link PluginXmlContainerUtility#checkForDuplicateFields(Element, Map)} to test Tests the provided
     * collection of fields and a particular duplicating tag name. Throws an exception if a field from a superclass
     * is positioned below the correspondent field from a subclass, therefore, will have precedence
     * @param tagName String representing the tag name in question
     * @param managedFields {@code Map<Field, String>} that matches rendered fields to corresponding element names
     */
    private static void checkForDuplicateFields(String tagName, Map<Field, String> managedFields) {
        LinkedList<Field> matchedFields = managedFields.entrySet().stream()
                .filter(entry -> entry.getValue().equals(tagName))
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(LinkedList::new));
        Iterator<Field> iterator = matchedFields.descendingIterator();
        while (iterator.hasNext()) {
            Field currentField = iterator.next();
            if (!iterator.hasNext()) {
                return;
            }
            Field nextField = iterator.next();
            if (!ClassUtils.isAssignable(currentField.getDeclaringClass(), nextField.getDeclaringClass())) {
                PluginRuntime
                        .context()
                        .getExceptionHandler()
                        .handle(new InvalidFieldContainerException(String.format(
                                DUPLICATE_FIELDS_MESSAGE_TEMPLATE,
                                currentField.getName(),
                                nextField.getDeclaringClass().getSimpleName(),
                                currentField.getDeclaringClass().getSimpleName())));
            }
        }
    }
}
