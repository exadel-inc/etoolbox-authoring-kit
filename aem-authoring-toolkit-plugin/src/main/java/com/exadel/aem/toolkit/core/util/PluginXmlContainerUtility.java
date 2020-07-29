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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    private static final String DUPLICATE_FIELDS_MESSAGE_TEMPLATE = "The following duplicate field names detected: %s. " +
            "This may cause unexpected behavior while saving data";

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
        Element itemsElement = PluginRuntime.context().getXmlUtility().createNodeElement(DialogConstants.NN_ITEMS);
        container.appendChild(itemsElement);

        for (Field field : fields) {
            DialogWidget widget = DialogWidgets.fromField(field);
            if (widget == null) {
                continue;
            }
            widget.appendTo(itemsElement, field);
        }

        if (container.hasChildNodes()) {
            checkForDuplicateFields(itemsElement);
        }
    }

    /**
     * Tests the provided container for possible duplicate widget nodes (those sharing the same tag name), and issues
     * an exception if found
     * @param container XML definition of an immediate parent for widget nodes (typically, an {@code items} element)
     */
    private static void checkForDuplicateFields(Element container) {
        List<String> childElementsTagNames = IntStream
                .range(0, container.getChildNodes().getLength())
                .mapToObj(index -> container.getChildNodes().item(index))
                .map(Node::getNodeName)
                .collect(Collectors.toList());
        Set<String> childElementsUniqueNames = new HashSet<>(childElementsTagNames);
        if (childElementsTagNames.size() != childElementsUniqueNames.size()) {
            childElementsUniqueNames.forEach(childElementsTagNames::remove);
            String nonUniqueNames = childElementsTagNames.stream()
                    .distinct()
                    .collect(Collectors.joining(DialogConstants.INLINE_ITEM_SEPARATOR));
            PluginRuntime
                    .context()
                    .getExceptionHandler()
                    .handle(new InvalidFieldContainerException(String.format(DUPLICATE_FIELDS_MESSAGE_TEMPLATE, nonUniqueNames)));
        }
    }
}
