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

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Attribute;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.utils.AnnotationUtil;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;
import com.exadel.aem.toolkit.plugin.utils.NamingUtil;

/**
 * Presents a {@link SectionFacade} variant for handling the legacy flavor of {@code Tab} layout
 */
@SuppressWarnings("deprecation") // Processing of container.Tab is retained for compatibility and will be removed
                                 // in a version after 2.0.2
class LegacyTabFacade extends SectionFacade {

    private final Tab tab;

    /**
     * Creates a new {@code SectionHelper} wrapped around the specified {@link Tab} object
     * @param tab      {@code Tab} object
     * @param isLayout True if the current section is a dialog layout section; false if it is a dialog widget section
     */
    LegacyTabFacade(Tab tab, boolean isLayout) {
        super(isLayout);
        this.tab = tab;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    String getTitle() {
        if (tab == null) {
            return StringUtils.EMPTY;
        }
        return tab.title();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Target createItemsContainer(Target container) {
        if (tab == null) {
            return container;
        }
        String nodeName = NamingUtil.getUniqueName(getTitle(), DialogConstants.NN_TAB, container);
        Target itemsContainer = container.createTarget(nodeName);
        itemsContainer
            .attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.CONTAINER)
            .attribute(DialogConstants.PN_JCR_TITLE, getTitle());
        Attribute attributeAnnotation = tab.attribute();
        itemsContainer.attributes(attributeAnnotation, AnnotationUtil.getPropertyMappingFilter(attributeAnnotation));
        if (attributeAnnotation.data().length > 0) {
            Target graniteDataElement = container.getOrCreateTarget(DialogConstants.NN_GRANITE_DATA);
            Arrays.stream(attributeAnnotation.data())
                .forEach(data -> graniteDataElement.attribute(data.name(), data.value()));
        }

        return itemsContainer;
    }
}
