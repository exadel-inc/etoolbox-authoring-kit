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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.exadel.aem.toolkit.api.handlers.SourceFacade;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.widgets.color.ColorField;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.util.DialogConstants;

/**
 * {@link Handler} implementation used to create markup responsible for Granite UI {@code ColorField} widget functionality
 * within the {@code cq:dialog} XML node
 */
class ColorFieldHandler implements Handler, BiConsumer<SourceFacade, Element> {
    private static final String NODE_NAME_COLOR = "color";
    private static final String SKIPPED_COLOR_NODE_NAME_SYMBOLS = "^\\w+";

    /**
     * Processes the user-defined data and writes it to XML entity
     * @param sourceFacade Current {@code SourceFacade} instance
     * @param element Current XML element
     */
    @Override
    public void accept(SourceFacade sourceFacade, Element element) {
        ColorField colorField = sourceFacade.adaptTo(ColorField.class);
        List<String> validCustomColors = ArrayUtils.isNotEmpty(colorField.customColors())
                ? Arrays.stream(colorField.customColors()).filter(StringUtils::isNotBlank).collect(Collectors.toList())
                : Collections.emptyList();
        if (validCustomColors.isEmpty()) {
            return;
        }
        Element itemsNode = getXmlUtil().createNodeElement(DialogConstants.NN_ITEMS);
        for (String customColor: validCustomColors) {
            Element colorNode = getXmlUtil().createNodeElement(
                    NODE_NAME_COLOR + customColor.toLowerCase().replaceAll(SKIPPED_COLOR_NODE_NAME_SYMBOLS, StringUtils.EMPTY),
                    DialogConstants.NT_UNSTRUCTURED,
                    Collections.singletonMap(DialogConstants.PN_VALUE, customColor));
            itemsNode.appendChild(colorNode);
        }
        element.appendChild(itemsNode);
    }
}
