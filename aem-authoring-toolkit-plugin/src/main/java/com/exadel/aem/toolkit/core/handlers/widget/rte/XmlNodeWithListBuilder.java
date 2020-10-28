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
package com.exadel.aem.toolkit.core.handlers.widget.rte;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.runtime.XmlUtility;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;

/**
 * Used to build XML nodes for flat-string array feature representations within RichTextEditor config XML, such as
 * {@code features} or {@code table} node
 */
class XmlNodeWithListBuilder extends XmlNodeBuilderBase {
    private List<String> argumentList;
    private XmlTreeWithListsBuilder childBuilder;

    XmlTreeWithListsBuilder getChildBuilder() {
        return childBuilder;
    }
    void setChildBuilder(XmlTreeWithListsBuilder childBuilder) {
        this.childBuilder = childBuilder;
    }

    XmlNodeWithListBuilder(String elementName, String featureAttributeName) {
        super(elementName, featureAttributeName);
        argumentList = new LinkedList<>();
    }

    @Override
    public void store(String pluginId, String feature) {
        if (getFilter() != null && !getFilter().test(pluginId,feature)) {
            return;
        }
        argumentList.add(feature);
    }

    @Override
    boolean isEmpty() {
        return argumentList.isEmpty();
    }

    @Override
    Element build() {
        return null;
    }
}
