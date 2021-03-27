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

import java.lang.reflect.Method;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.layouts.Tabs;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.handlers.layouts.common.WidgetContainerHandler;
import com.exadel.aem.toolkit.plugin.util.DialogConstants;

/**
 * {@link WidgetContainerHandler} implementation used to prepare data needed for {@code Tabs} widget functionality
 */
public class TabsWidgetHandler extends WidgetContainerHandler {
    private static final Predicate<Method> WIDGET_PROPERTIES_FILTER = method ->
        !StringUtils.equalsAny(
            method.getName(),
            DialogConstants.PN_TYPE,
            DialogConstants.PN_PADDING);

    /**
     * Implements the {@code BiConsumer<Source, Target} pattern to process user input data specific for {@link Tabs}
     * and provide data for widget rendering
     * @param source Member that defines a {@code Tabs} widget
     * @param target Data structure used for rendering
     */
    @Override
    public void accept(Source source, Target target) {
        target.attributes(source.adaptTo(Tabs.class), WIDGET_PROPERTIES_FILTER); // We do not use the auto-mapping facility
        // because @Tabs can be used class-level and should not mess with "true" auto-mapped class annotations
        populateNestedContainer(source, target, Tabs.class);
    }
}
