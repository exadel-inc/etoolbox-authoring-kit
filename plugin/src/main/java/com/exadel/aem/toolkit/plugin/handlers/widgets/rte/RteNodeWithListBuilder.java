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

import java.util.LinkedList;
import java.util.List;

import com.exadel.aem.toolkit.api.handlers.Target;

/**
 * Used to build XML nodes for flat-string array feature representations within RichTextEditor config, such as
 * {@code features} or {@code table} node
 */
class RteNodeWithListBuilder extends RteNodeBuilderBase {

    private final List<String> argumentList;
    private RteTreeWithListsBuilder childBuilder;

    /**
     * Initializes a class instance
     * @param tagName String representing the name of the node being built
     * @param featureAttributeName String representing the name of feature
     */
    RteNodeWithListBuilder(String tagName, String featureAttributeName) {
        super(tagName, featureAttributeName);
        argumentList = new LinkedList<>();
    }

    /**
     * Retrieves the child {@link RteNodeWithListBuilder} object associated with this instance
     * @return {@code RteNodeWithListBuilder} instance
     */
    RteTreeWithListsBuilder getChildBuilder() {
        return childBuilder;
    }

    /**
     * Assigns the child {@link RteNodeWithListBuilder} object
     * @param childBuilder {@code RteNodeWithListBuilder} instance
     */
    void setChildBuilder(RteTreeWithListsBuilder childBuilder) {
        this.childBuilder = childBuilder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void store(String pluginId, String feature) {
        if (getFilter() != null && !getFilter().test(pluginId, feature)) {
            return;
        }
        argumentList.add(feature);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    boolean isEmpty() {
        return argumentList.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Target build(Target parent) {
        if (!isEmpty()) {
            Target result = parent.getOrCreateTarget(getName());
            result.attribute(getAttributeName(), argumentList.toString().replace(" ", ""));
            if (childBuilder != null) childBuilder.build(result);
            if (getPostprocessing() != null) getPostprocessing().accept(result);
            return result;
        }
        return null;
    }
}
