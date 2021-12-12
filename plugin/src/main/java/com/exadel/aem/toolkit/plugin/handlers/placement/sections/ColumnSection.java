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
package com.exadel.aem.toolkit.plugin.handlers.placement.sections;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.layouts.Column;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;
import com.exadel.aem.toolkit.plugin.utils.NamingUtil;

/**
 * Presents a {@link Section} variant for handling the {@code Accordion} layout
 */
class ColumnSection extends Section {
    private final Column column;

    /**
     * Creates a new {@link Section} wrapped around the specified {@link Column} object
     * @param isLayout True if the current section is a dialog layout section; false if it is a dialog widget section
     * @param column {@code Column} object
     */
    public ColumnSection(Column column, boolean isLayout) {
        super(false);
        this.column = column;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        if (column == null) {
            return StringUtils.EMPTY;
        }
        return column.title();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Target createItemsContainer(Target host) {
        if (column == null) {
            return host;
        }
        String nodeName = NamingUtil.getUniqueName(getTitle(), DialogConstants.NN_COLUMN, host);
        Target itemsContainer = host.createTarget(nodeName);
        itemsContainer.attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.CONTAINER);
        return itemsContainer;
    }
}
