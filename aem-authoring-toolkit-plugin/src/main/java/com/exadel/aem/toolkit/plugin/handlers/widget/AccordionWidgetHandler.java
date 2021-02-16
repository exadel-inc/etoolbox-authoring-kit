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

import com.exadel.aem.toolkit.api.annotations.layouts.AccordionWidget;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.handlers.container.common.WidgetContainerHandler;

/**
 * Handler used to prepare data for {@link AccordionWidget} widget rendering
 */
class AccordionWidgetHandler extends WidgetContainerHandler {

    /**
     * Implements the {@code BiConsumer<Source, Target} pattern to process settings specified by {@link AccordionWidget}
     * and provide data for widget rendering
     * @param source Member that defines an {@code Accordion}
     * @param target Data structure used for rendering
     */
    @Override
    public void accept(Source source, Target target) {
        populateContainer(source, target, AccordionWidget.class);
    }
}
