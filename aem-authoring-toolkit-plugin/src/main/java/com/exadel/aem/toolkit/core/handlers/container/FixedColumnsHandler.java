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
package com.exadel.aem.toolkit.core.handlers.container;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.handlers.MemberWrapper;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.w3c.dom.Element;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * The {@link Handler} for a fixed-columns TouchUI dialog.
 */
public class FixedColumnsHandler implements Handler, BiConsumer<Class<?>, Element> {
    /**
     * Implements {@code BiConsumer<Class<?>, Element>} pattern
     * to process component-backing Java class and append the results to the XML root node
     * @param componentClass {@code Class<?>} instance used as the source of markup
     * @param parentElement XML document root element
     */
    @Override
    public void accept(Class<?> componentClass, Element parentElement) {
        Element content = getXmlUtil().createNodeElement(DialogConstants.NN_CONTENT, ResourceTypes.CONTAINER);

        Element layout = getXmlUtil().createNodeElement(
                DialogConstants.NN_LAYOUT,
                Collections.singletonMap(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, ResourceTypes.FIXED_COLUMNS)
        );
        Element contentItems = getXmlUtil().createNodeElement(DialogConstants.NN_ITEMS);

        Element contentItemsColumn = getXmlUtil().createNodeElement(
                DialogConstants.NN_COLUMN,
                Collections.singletonMap(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, ResourceTypes.CONTAINER)
        );

        parentElement.appendChild(content);

        content.appendChild(layout);
        content.appendChild(contentItems);

        contentItems.appendChild(contentItemsColumn);

        List<MemberWrapper> allFields = PluginReflectionUtility.getAllMembers(componentClass)
                .stream().map(MemberWrapper::new).collect(Collectors.toList());
        Handler.appendToContainer(contentItemsColumn, allFields);
    }
}
