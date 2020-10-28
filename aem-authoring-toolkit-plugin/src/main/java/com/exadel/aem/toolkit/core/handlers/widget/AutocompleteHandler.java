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

import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.widgets.autocomplete.Autocomplete;
import com.exadel.aem.toolkit.api.handlers.SourceFacade;
import com.exadel.aem.toolkit.api.handlers.TargetFacade;
import com.exadel.aem.toolkit.core.TargetFacadeFacadeImpl;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.util.DialogConstants;

import java.util.function.BiConsumer;

/**
 * {@link Handler} implementation used to create markup responsible for Granite UI {@code Multifield} widget functionality
 * within the {@code cq:dialog} XML node
 */
class AutocompleteHandler implements Handler, BiConsumer<SourceFacade, TargetFacade> {
    /**
     * Processes the user-defined data and writes it to XML entity
     *
     * @param sourceFacade Current {@code SourceFacade} instance
     * @param targetFacade Current {@code TargetFacade} instance
     */
    @Override
    public void accept(SourceFacade sourceFacade, TargetFacade targetFacade) {
        Autocomplete autocomplete = sourceFacade.adaptTo(Autocomplete.class);
        TargetFacade datasource = new TargetFacadeFacadeImpl(DialogConstants.NN_DATASOURCE)
                .setAttribute(DialogConstants.PN_SLING_RESOURCE_TYPE, autocomplete.datasource().annotationType().getAnnotation(ResourceType.class).value());
        datasource.mapProperties(autocomplete.datasource());
        targetFacade.appendChild(datasource);

        TargetFacade options = new TargetFacadeFacadeImpl(DialogConstants.NN_OPTIONS)
                .setAttribute(DialogConstants.PN_SLING_RESOURCE_TYPE, autocomplete.datasource().annotationType().getAnnotation(ResourceType.class).value());
        datasource.mapProperties(autocomplete.options());
        targetFacade.appendChild(options);

        TargetFacade values = new TargetFacadeFacadeImpl(DialogConstants.NN_VALUES)
                .setAttribute(DialogConstants.PN_SLING_RESOURCE_TYPE, autocomplete.datasource().annotationType().getAnnotation(ResourceType.class).value());
        values.mapProperties(autocomplete.values());
        targetFacade.appendChild(values);
    }
}
