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
package com.exadel.aem.toolkit.plugin.handlers.widgets;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.widgets.color.ColorField;
import com.exadel.aem.toolkit.api.handlers.Handler;
import com.exadel.aem.toolkit.api.handlers.Handles;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;

/**
 * Implements {@code BiConsumer} to populate a {@link Target} instance with properties originating from a {@link Source}
 * object that define the Granite UI {@code ColorField} widget look and behavior
 */
@Handles(ColorField.class)
public class ColorFieldHandler implements Handler {
    private static final String NODE_NAME_COLOR = "color";
    private static final String SKIPPED_COLOR_NODE_NAME_SYMBOLS = "^\\w+";

    /**
     * Processes data that can be extracted from the given {@code Source} and stores it into the provided {@code Target}
     * @param source {@code Source} object used for data retrieval
     * @param target Resulting {@code Target} object
     */
    @Override
    public void accept(Source source, Target target) {
        ColorField colorField = source.adaptTo(ColorField.class);
        List<String> validCustomColors = ArrayUtils.isNotEmpty(colorField.customColors())
                ? Arrays.stream(colorField.customColors()).filter(StringUtils::isNotBlank).collect(Collectors.toList())
                : Collections.emptyList();
        if (validCustomColors.isEmpty()) {
            return;
        }
        Target itemsNode = target.getOrCreateTarget(DialogConstants.NN_ITEMS);
        for (String customColor: validCustomColors) {
            itemsNode.getOrCreateTarget(NODE_NAME_COLOR + customColor.toLowerCase().replace(SKIPPED_COLOR_NODE_NAME_SYMBOLS, StringUtils.EMPTY))
                    .attribute(DialogConstants.PN_PRIMARY_TYPE, DialogConstants.NT_UNSTRUCTURED)
                    .attributes(Collections.singletonMap(CoreConstants.PN_VALUE, customColor));
        }
    }
}
