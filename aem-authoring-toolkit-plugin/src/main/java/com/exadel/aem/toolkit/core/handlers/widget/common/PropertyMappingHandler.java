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
package com.exadel.aem.toolkit.core.handlers.widget.common;

import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.api.handlers.MemberWrapper;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;
import org.w3c.dom.Element;

import java.util.function.BiConsumer;

/**
 * Handler for storing sets of generic widget properties to a Granite UI widget XML node
 */
public class PropertyMappingHandler implements BiConsumer<Element, MemberWrapper> {
    /**
     * Processes the user-defined data and writes it to XML entity
     * @param element XML element
     * @param memberWrapper Current {@code MemberWrapper} instance
     */
    @Override
    public void accept(Element element, MemberWrapper memberWrapper) {
        PluginReflectionUtility.getMemberAnnotations(memberWrapper.getMember())
                .filter(a -> a.annotationType().isAnnotationPresent(PropertyMapping.class))
                .forEach(a -> PluginRuntime.context().getXmlUtility().mapProperties(element, a));
    }
}
