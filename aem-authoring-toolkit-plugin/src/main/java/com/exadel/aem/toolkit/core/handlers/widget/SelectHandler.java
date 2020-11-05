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

import java.util.function.BiConsumer;

import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.core.util.PluginXmlUtility;
import org.apache.commons.lang3.ArrayUtils;

import com.exadel.aem.toolkit.api.annotations.widgets.select.Option;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Select;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.util.DialogConstants;

/**
 * {@link Handler} implementation used to create markup responsible for Granite {@code Select} widget functionality
 * within the {@code cq:dialog} XML node
 */
class SelectHandler implements Handler, BiConsumer<Source, Target> {
    /**
     * Processes the user-defined data and writes it to XML entity
     * @param source Current {@code SourceFacade} instance
     * @param target Current XML targetFacade
     */
    @Override
    @SuppressWarnings({"deprecation", "squid:S1874"})
    // .acsListPath() and .acsListResourceType() method calls, as well as .addNoneOption() processing
    // remain for compatibility reasons until v.2.0.0
    public void accept(Source source, Target target) {
        Select select = source.adaptTo(Select.class);
        if (ArrayUtils.isNotEmpty(select.options())) {
            Target items = target.child(DialogConstants.NN_ITEMS);
            for (Option option: select.options()) {
                items.child().mapProperties(option);
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
