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

import java.lang.reflect.Method;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.layouts.Tab;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.targets.Targets;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;
import com.exadel.aem.toolkit.plugin.utils.NamingUtil;

/**
 * Presents a {@link Section} variant for handling the {@code Tab} layout
 */
class TabSection extends Section {
    private static final Predicate<Method> MAIN_NODE_MEMBERS = method -> StringUtils.equalsAny(
        method.getName(),
        DialogConstants.PN_TITLE,
        DialogConstants.PN_TRACKING_ELEMENT);

    private final Tab tab;

    /**
     * Creates a new {@link Section} wrapped around the specified {@link Tab} object
     * @param tab      {@code Tab} object this helper wraps
     * @param isLayout True if the current section is a dialog layout section; false if it is a dialog widget section
     */
    public TabSection(Tab tab, boolean isLayout) {
        super(isLayout);
        this.tab = tab;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        if (tab == null) {
            return StringUtils.EMPTY;
        }
        return tab.title();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Target createItemsContainer(Target host) {
        if (tab == null) {
            return host;
        }
        String nodeName = NamingUtil.getUniqueName(getTitle(), DialogConstants.NN_TAB, host);
        Target itemsContainer = host.createTarget(nodeName);
        itemsContainer.attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.CONTAINER)
            .attributes(tab, MAIN_NODE_MEMBERS);
        String configContainerTag = isLayout()
            ? DialogConstants.NN_LAYOUT_CONFIG
            : DialogConstants.NN_PARENT_CONFIG;
        Target configContainer = Targets
            .newTarget(configContainerTag)
            .attributes(tab, MAIN_NODE_MEMBERS.negate());
        if (!configContainer.isEmpty()) {
            itemsContainer.addTarget(configContainer);
        }
        return itemsContainer;
    }
}
