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

import java.util.Arrays;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import org.w3c.dom.Element;

abstract class XmlNodeBuilderBase {
    private String name;
    private String attributeName;
    private BiPredicate<String, String> filter;
    private Consumer<Element> postprocessing;

    XmlNodeBuilderBase(String tagName, String attributeName) {
        this.name = tagName;
        this.attributeName = attributeName;
    }

    String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    String getAttributeName() {
        return attributeName;
    }

    Consumer<Element> getPostprocessing() {
        return postprocessing;
    }
    void setPostprocessing(Consumer<Element> postprocessing) {
        this.postprocessing = postprocessing;
    }

    void setFilter(BiPredicate<String, String> filter) {
        this.filter = filter;
    }
    BiPredicate<String, String> getFilter() {
        return filter;
    }

    abstract void store(String pluginId, String feature);
    void storeMany(String pluginId, String[] features) {
        Arrays.stream(features).forEach(feature -> store(pluginId, feature));
    }

    abstract Element build();

    abstract boolean isEmpty();
}
