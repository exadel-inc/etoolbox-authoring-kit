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

import java.util.List;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.ArrayUtils;

import com.exadel.aem.toolkit.api.annotations.widgets.select.Option;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Select;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.util.DialogConstants;
import com.exadel.aem.toolkit.plugin.util.PluginAnnotationUtility;
import com.exadel.aem.toolkit.plugin.util.PluginXmlUtility;

/**
 * {@code BiConsumer<Source, Target>} implementation used to create markup responsible for Granite {@code Select} widget functionality
 * within the {@code cq:dialog} node
 */
class SelectHandler extends OptionProviderHandler implements BiConsumer<Source, Target> {
    /**
     * Processes the user-defined data and writes it to {@link Target}
     * @param source Current {@link Source} instance
     * @param target Current {@link Target} instance
     */
    @Override
    @SuppressWarnings({"deprecation", "squid:S1874"})
    // .acsListPath() and .acsListResourceType() method calls, as well as .addNoneOption() processing
    // remain for compatibility reasons until v.2.0.0
    public void accept(Source source, Target target) {
        Select select = source.adaptTo(Select.class);
        if (hasProvidedOptions(select.optionProvider())) {
            appendOptionProvider(select.optionProvider(), target);
            return;
        }
        if (ArrayUtils.isNotEmpty(select.options())) {
            Target items = target.getOrCreateTarget(DialogConstants.NN_ITEMS);
            for (Option option: select.options()) {
                List<Target> existing = items.findChildren(t -> option.value().equals(t.getAttribute(DialogConstants.PN_VALUE)));
                Target item = existing.isEmpty()
                    ? items.createTarget(DialogConstants.DOUBLE_QUOTE + option.value() + DialogConstants.DOUBLE_QUOTE)
                    : items.getTarget(DialogConstants.DOUBLE_QUOTE + option.value() + DialogConstants.DOUBLE_QUOTE);
                item.attributes(option, PluginAnnotationUtility.getPropertyMappingFilter(option));
            }
        }
        Target dataSourceElement = PluginXmlUtility.appendDataSource(
                target,
                select.datasource(),
                select.acsListPath(),
                select.acsListResourceType());
        if (dataSourceElement != null && select.addNoneOption()) {
            dataSourceElement.attribute(DialogConstants.PN_ADD_NONE, true);
        }
    }
}
