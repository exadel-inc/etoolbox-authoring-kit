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
package com.exadel.aem.toolkit.plugin.handlers.layouts;

import java.util.Map;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableMap;

import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.writers.DialogLayout;

/**
 * Presents factory logic for building either a fixed-columns or nested-container Granite UI dialog
 */
public class ContainerHandlers {
    private static final Map<DialogLayout, BiConsumer<Source, Target>> HANDLERS = ImmutableMap.of(
        DialogLayout.FIXED_COLUMNS, new FixedColumnsHandler(),
        DialogLayout.ACCORDION, new AccordionContainerHandler(),
        DialogLayout.TABS, new TabsContainerHandler()
    );

    /**
     * Default (instantiation-preventing) constructor
     */
    private ContainerHandlers() {
    }

    /**
     * Retrieves the container handler instance for the given layout
     * @param layout Non-null {@link DialogLayout} value to match the container
     * @return Container handler for either fixed-columns, tabbed, accordion-shaped, etc. layout
     */
    public static BiConsumer<Source, Target> forLayout(DialogLayout layout) {
        return HANDLERS.getOrDefault(layout, HANDLERS.get(DialogLayout.FIXED_COLUMNS));
    }
}
