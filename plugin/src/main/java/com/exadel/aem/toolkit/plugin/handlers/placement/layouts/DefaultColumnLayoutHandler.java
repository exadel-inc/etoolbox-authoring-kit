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
package com.exadel.aem.toolkit.plugin.handlers.placement.layouts;

import java.util.function.BiConsumer;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.handlers.placement.PlacementHelper;
import com.exadel.aem.toolkit.plugin.handlers.placement.registries.MembersRegistry;
import com.exadel.aem.toolkit.plugin.targets.RootTarget;
import com.exadel.aem.toolkit.plugin.utils.ClassUtil;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;

/**
 * Presents the implementation of layout handler for a fixed-columns Granite UI dialog
 */
class DefaultColumnLayoutHandler implements BiConsumer<Source, Target> {

    /**
     * Processes data that can be extracted from the given {@code Source} and stores it into the provided {@code Target}
     * @param source {@code Source} object used for data retrieval
     * @param target Resulting {@code Target} object
     */
    @Override
    public void accept(Source source, Target target) {
        Target contentItemsColumn = target.getOrCreateTarget(DialogConstants.NN_CONTENT)
                .attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.CONTAINER)
                .getOrCreateTarget(DialogConstants.NN_LAYOUT)
                .attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.FIXED_COLUMNS)
                .getParent()
                .getOrCreateTarget(DialogConstants.NN_ITEMS)
                .getOrCreateTarget(DialogConstants.NN_COLUMN)
                .attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.CONTAINER);

        MembersRegistry membersRegistry = new MembersRegistry(ClassUtil.getSources(source.adaptTo(Class.class)));
        target.getRoot().adaptTo(RootTarget.class).setMembers(membersRegistry);

         PlacementHelper.builder()
            .container(contentItemsColumn)
            .members(membersRegistry)
            .build()
            .doPlacement();
         // For historic reasons, we allow "orphaned" members to be rendered via this layout handler, therefore,
         // MembersRegistry is not reviewed in the end unlike in ComplexLayoutHandler
    }
}
