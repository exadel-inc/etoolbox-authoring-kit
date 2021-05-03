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
package com.exadel.aem.toolkit.plugin.handlers.widgets.rte;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.core.CoreConstants;

/**
 * Used to build nodes for feature representations with nested XML nodes within RichTextEditor config XML, such as
 * {@code plugins} node
 */
class RteTreeWithListsBuilder extends RteNodeBuilderBase {

    private final Map<String, Set<String>> argumentTree;
    private final boolean addPluginIdsToFeatures;

    /**
     * Initializes a class instance
     * @param tagName String representing the name of the node being built
     * @param attributeName String representing the name of feature
     */
    RteTreeWithListsBuilder(
        String tagName,
        String attributeName) {
        this(tagName, attributeName, false);
    }

    /**
     * Instance constructor
     * @param tagName String representing the name of the node being built
     * @param attributeName String representing the name of feature
     * @param addPluginIdsToFeatures Defines whether plugin IDs should be stored
     */
    RteTreeWithListsBuilder(
            String tagName,
            String attributeName,
            boolean addPluginIdsToFeatures) {
        super(tagName, attributeName);
        this.argumentTree = new HashMap<>();
        this.addPluginIdsToFeatures = addPluginIdsToFeatures;
    }

    /**
     * Copying constructor
     * @param sample {@link RteTreeWithListsBuilder} instance to copy values from
     */
    RteTreeWithListsBuilder(RteTreeWithListsBuilder sample) {
        super(sample.getName(), sample.getAttributeName());
        this.addPluginIdsToFeatures = sample.addPluginIdsToFeatures;
        this.setPostprocessing(sample.getPostprocessing());
        this.argumentTree = new HashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void store(String pluginId, String feature) {
        String mapKey = StringUtils.substringBefore(feature, RichTextEditorHandler.PLUGIN_FEATURE_SEPARATOR);
        // If pluginId is the same as 'plugin#' part in feature token, do not duplicate the 'plugin' part in the map
        // otherwise store feature token as it comes (might be needed if this is a popover builder, and it contains tokens
        // (i.e. buttons) that come from different plugins)
        String mapValue = (StringUtils.isNoneEmpty(pluginId, mapKey) && !pluginId.equals(mapKey))
                ? feature
                : StringUtils.substringAfter(feature, RichTextEditorHandler.PLUGIN_FEATURE_SEPARATOR);
        if (!StringUtils.isEmpty(pluginId)) mapKey = pluginId;
        if (getFilter() != null && !getFilter().test(mapKey, mapValue)) {
            return;
        }

        if (StringUtils.isNoneEmpty(mapKey, mapValue)) {
            argumentTree.computeIfAbsent(mapKey, k -> new LinkedHashSet<>()).add(mapValue);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    boolean isEmpty() {
        return argumentTree.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Target build(Target parent) {
        Target result = parent.getOrCreateTarget(getName());
        argumentTree.forEach((pluginId, features) -> createChildNode(result, pluginId, features));
        return result;
    }

    /**
     * Creates a nested XML node within the tree-like XML config
     * @param pluginId RTE Plugin name
     * @param features Feature identifiers
     */
    private void createChildNode(Target parent, String pluginId, Set<String> features) {
        Target node = parent.getOrCreateTarget(pluginId);
        List<String> valueList = features.stream()
                // If 'addPluginIdsToFeatures' flag is set, each entry must be brought andThen 'plugin#feature' format
                // unless it is already preserved in this format or is in 'plugin:feature:feature' format (like e.g. 'paraformat' button)
                .map(value -> addPluginIdsToFeatures && !value.contains(RichTextEditorHandler.PLUGIN_FEATURE_SEPARATOR) && !value.contains(CoreConstants.SEPARATOR_COLON)
                        ? pluginId + RichTextEditorHandler.PLUGIN_FEATURE_SEPARATOR + value
                        : value)
                .collect(Collectors.toList());
        if (valueList.size() > 1) {
            node.attribute(getAttributeName(), valueList.toString().replace(StringUtils.SPACE, StringUtils.EMPTY));
        } else {
            node.attribute(getAttributeName(), valueList.get(0));
        }
        if (getPostprocessing() != null) getPostprocessing().accept(node);
    }
}
