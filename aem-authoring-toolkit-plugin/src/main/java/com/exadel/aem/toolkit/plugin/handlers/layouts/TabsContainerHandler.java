/*
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http:www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.exadel.aem.toolkit.plugin.handlers.layouts;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.layouts.Tab;
import com.exadel.aem.toolkit.api.annotations.layouts.Tabs;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.handlers.layouts.common.ContainerHandler;
import com.exadel.aem.toolkit.plugin.target.Targets;
import com.exadel.aem.toolkit.plugin.util.DialogConstants;

/**
 * The {@code Handler} for a tabbed TouchUI dialog
 */
public class TabsContainerHandler extends ContainerHandler {
    private static final Predicate<Method> LAYOUT_PROPERTIES_FILTER = method ->
        StringUtils.equalsAny(
            method.getName(),
            DialogConstants.PN_TYPE,
            DialogConstants.PN_PADDING);

    /**
     * Implements {@code BiConsumer<Class<?>, Element>} pattern
     * to process component-backing Java class and append the results to the XML root node
     * @param componentClass {@code Class<?>} instance used as the source of markup
     * @param parentElement  XML document root element
     */
    @Override
    public void accept(Class<?> componentClass, Target parentElement) {
        populateContainer(
            componentClass,
            parentElement,
            Arrays.asList(Tab.class,
                com.exadel.aem.toolkit.api.annotations.container.Tab.class)
        );
        Tabs tabsAnnotation = componentClass.getDeclaredAnnotation(Tabs.class);
        Target layoutContainer = null;
        if (tabsAnnotation != null) {
            layoutContainer = Targets.newInstance(DialogConstants.NN_LAYOUT)
                .attributes(tabsAnnotation, LAYOUT_PROPERTIES_FILTER);
        }
        if (layoutContainer != null && !layoutContainer.isEmpty() && parentElement.exists(DialogConstants.NN_CONTENT)) {
            parentElement.getTarget(DialogConstants.NN_CONTENT).addTarget(layoutContainer, 0);
        }
    }
}
