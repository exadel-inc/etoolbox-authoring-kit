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

import org.apache.commons.lang3.ArrayUtils;

import com.exadel.aem.toolkit.api.annotations.widgets.radio.RadioGroup;
import com.exadel.aem.toolkit.api.handlers.Handler;
import com.exadel.aem.toolkit.api.handlers.Handles;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.util.DialogConstants;

/**
 * {@code BiConsumer<Source, Target>} implementation used to create markup responsible for {@code RadioGroup} widget functionality
 * within the {@code cq:dialog} node
 */
@Handles(RadioGroup.class)
public class RadioGroupHandler extends OptionProviderHandler implements Handler {

    /**
     * Processes the user-defined data and writes it to {@link Target}
     * @param source Current {@link Source} instance
     * @param target Current {@link Target} instance
     */
    @Override
    public void accept(Source source, Target target) {
        RadioGroup radioGroup = source.adaptTo(RadioGroup.class);
        if (hasProvidedOptions(radioGroup.buttonProvider())) {
            appendOptionProvider(radioGroup.buttonProvider(), target);
            return;
        }
        if (ArrayUtils.isNotEmpty(radioGroup.buttons())) {
            Target items = target.getOrCreateTarget(DialogConstants.NN_ITEMS);
            Arrays.stream(radioGroup.buttons()).forEach(button -> appendOption(button, button.value(), items));
        }
        appendDataSource(radioGroup.datasource(), target);
    }

}
