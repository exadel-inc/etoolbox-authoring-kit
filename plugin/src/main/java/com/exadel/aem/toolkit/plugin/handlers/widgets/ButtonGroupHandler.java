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

import org.apache.commons.lang3.ArrayUtils;

import com.exadel.aem.toolkit.api.annotations.widgets.buttongroup.ButtonGroup;
import com.exadel.aem.toolkit.api.annotations.widgets.buttongroup.ButtonGroupItem;
import com.exadel.aem.toolkit.api.handlers.Handler;
import com.exadel.aem.toolkit.api.handlers.Handles;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;

/**
 * Implements {@code BiConsumer} to populate a {@link Target} instance with properties originating from a {@link Source}
 * object that define the Granite UI {@code Select} widget look and behavior
 */
@Handles(ButtonGroup.class)
public class ButtonGroupHandler extends OptionProviderHandler implements Handler {

    /**
     * Processes data that can be extracted from the given {@code Source} and stores it into the provided {@code Target}
     * @param source {@code Source} object used for data retrieval
     * @param target Resulting {@code Target} object
     */
    @Override
    public void accept(Source source, Target target) {
        ButtonGroup buttonGroup = source.adaptTo(ButtonGroup.class);
        if (hasProvidedOptions(buttonGroup.itemProvider())) {
            appendOptionProvider(buttonGroup.itemProvider(), target);
            return;
        }
        if (ArrayUtils.isNotEmpty(buttonGroup.items())) {
            Target items = target.getOrCreateTarget(DialogConstants.NN_ITEMS);
            for (ButtonGroupItem item : buttonGroup.items()) {
                appendOption(item, item.value(), items);
            }
        }
    }
}
