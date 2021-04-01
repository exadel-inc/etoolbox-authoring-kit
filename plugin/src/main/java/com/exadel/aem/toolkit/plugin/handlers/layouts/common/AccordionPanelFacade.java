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
package com.exadel.aem.toolkit.plugin.handlers.layouts.common;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.layouts.AccordionPanel;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.targets.Targets;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;
import com.exadel.aem.toolkit.plugin.utils.NamingUtil;

/**
 * Presents a {@link SectionFacade} variant for handling the {@code Accordion} layout
 */
class AccordionPanelFacade extends SectionFacade {
    private final AccordionPanel panel;

    /**
     * Creates a new {@code SectionHelper} wrapped around the specified {@link AccordionPanel} object
     * @param panel    {@code AccordionPanel} object
     * @param isLayout True if the current section is a dialog layout section; false if it is a dialog widget section
     */
    AccordionPanelFacade(AccordionPanel panel, boolean isLayout) {
        super(isLayout);
        this.panel = panel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    String getTitle() {
        if (panel == null) {
            return StringUtils.EMPTY;
        }
        return panel.title();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Target createItemsContainer(Target container) {
        if (panel == null) {
            return container;
        }
        String nodeName = NamingUtil.getUniqueName(getTitle(), DialogConstants.NN_TAB, container);
        Target itemsContainer = container.createTarget(nodeName);
        itemsContainer.attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.CONTAINER)
            .attribute(DialogConstants.PN_JCR_TITLE, getTitle());
        Target configContainer = Targets.newInstance(DialogConstants.NN_PARENT_CONFIG);
        configContainer.attributes(panel, method -> !method.getName().equals(DialogConstants.PN_TITLE));
        if (!configContainer.isEmpty()) {
            itemsContainer.addTarget(configContainer);
        }
        return itemsContainer;
    }
}
