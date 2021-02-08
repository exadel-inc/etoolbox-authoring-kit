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
package com.exadel.aem.toolkit.plugin.handlers.widget;

import java.util.Arrays;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.ArrayUtils;

import com.exadel.aem.toolkit.api.annotations.widgets.radio.RadioButton;
import com.exadel.aem.toolkit.api.annotations.widgets.radio.RadioGroup;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.util.DialogConstants;
import com.exadel.aem.toolkit.plugin.util.PluginAnnotationUtility;
import com.exadel.aem.toolkit.plugin.util.PluginXmlUtility;

/**
 * {@code BiConsumer<Source, Target>} implementation used to create markup responsible for {@code RadioGroup} widget functionality
 * within the {@code cq:dialog} node
 */
class RadioGroupHandler implements BiConsumer<Source, Target> {
    /**
     * Processes the user-defined data and writes it to {@link Target}
     * @param source Current {@link Source} instance
     * @param target Current {@link Target} instance
     */
    @Override
    @SuppressWarnings({"deprecation", "squid:S1874"})
    // .acsListPath() and .acsListResourceType() method calls remain for compatibility reasons until v.2.0.0
    public void accept(Source source, Target target) {
        RadioGroup radioGroup = source.adaptTo(RadioGroup.class);
        if (ArrayUtils.isNotEmpty(radioGroup.buttons())) {
            Target items = target.getOrCreateTarget(DialogConstants.NN_ITEMS);
            Arrays.stream(radioGroup.buttons()).forEach(button -> renderButton(button, items));
        }
        PluginXmlUtility.appendDataSource(target, radioGroup.datasource(), radioGroup.acsListPath(), radioGroup.acsListResourceType());
    }

    private void renderButton(RadioButton buttonInstance, Target parentElement) {
        parentElement.createTarget(buttonInstance.value())
                .attributes(buttonInstance, PluginAnnotationUtility.getPropertyMappingFilter(buttonInstance));
    }
}
