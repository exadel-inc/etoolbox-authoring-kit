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
package com.exadel.aem.toolkit.core.handlers.container;

import java.util.List;
import java.util.function.BiConsumer;

import com.exadel.aem.toolkit.api.handlers.SourceFacade;
import com.exadel.aem.toolkit.api.handlers.TargetBuilder;
import com.exadel.aem.toolkit.core.TargetBuilderImpl;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;
import com.exadel.aem.toolkit.core.util.PluginXmlContainerUtility;

/**
 * The {@link Handler} for a fixed-columns TouchUI dialog.
 */
public class FixedColumnsHandler implements Handler, BiConsumer<Class<?>, TargetBuilder> {
    /**
     * Implements {@code BiConsumer<Class<?>, Element>} pattern
     * to process component-backing Java class and append the results to the XML root node
     * @param componentClass {@code Class<?>} instance used as the source of markup
     * @param target XML document root element
     */
    @Override
    public void accept(Class<?> componentClass, TargetBuilder target) {
        TargetBuilder content = new TargetBuilderImpl(DialogConstants.NN_CONTENT)
            .attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.CONTAINER);

        TargetBuilder layout = new TargetBuilderImpl(DialogConstants.NN_LAYOUT)
                .attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.FIXED_COLUMNS);

        TargetBuilder contentItems = new TargetBuilderImpl(DialogConstants.NN_ITEMS);

        TargetBuilder contentItemsColumn = new TargetBuilderImpl(
                DialogConstants.NN_COLUMN)
                .attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.CONTAINER);

        target.appendChild(content);

        content.appendChild(layout);
        content.appendChild(contentItems);

        contentItems.appendChild(contentItemsColumn);

        List<SourceFacade> allSourceFacades = PluginReflectionUtility.getAllSourceFacades(componentClass);
        PluginXmlContainerUtility.append(contentItemsColumn, allSourceFacades);
    }
}
