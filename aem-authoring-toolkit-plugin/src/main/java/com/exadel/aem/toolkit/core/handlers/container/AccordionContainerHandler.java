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

import com.exadel.aem.toolkit.api.annotations.container.AccordionPanel;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.handlers.container.common.ContainerHandler;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import org.w3c.dom.Element;

/**
 * The {@link Handler} for a tabbed TouchUI dialog
 */
public class AccordionContainerHandler implements ContainerHandler {
    private static final String DEFAULT_TAB_NAME = "accordion";
    private static final String NO_TABS_DEFINED_EXCEPTION_MESSAGE = "No accordions defined for the dialog at ";

    /**
     * Implements {@code BiConsumer<Class<?>, Element>} pattern
     * to process component-backing Java class and append the results to the XML root node
     *
     * @param componentClass {@code Class<?>} instance used as the source of markup
     * @param parentElement  XML document root element
     */
    @Override
    public void accept(Class<?> componentClass, Element parentElement) {
        acceptParent(componentClass, parentElement, AccordionPanel.class, DialogConstants.NN_ACCORDION, ResourceTypes.ACCORDION, NO_TABS_DEFINED_EXCEPTION_MESSAGE, DEFAULT_TAB_NAME);
    }
}
