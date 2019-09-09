/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

import org.w3c.dom.Element;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;

public class FixedColumnsHandler implements Handler, BiConsumer<Class<?>, Element> {
    @Override
    public void accept(Class<?> clazz, Element parentElement) {
        Element content = getXmlUtil().createNodeElement(DialogConstants.NN_CONTENT, ResourceTypes.CONTAINER);

        Element layout = getXmlUtil().createNodeElement(
                DialogConstants.NN_LAYOUT,
                Collections.singletonMap(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, ResourceTypes.FIXED_COLUMNS)
        );
        Element contentItems = getXmlUtil().createNodeElement(DialogConstants.NN_ITEMS);

        Element column = getXmlUtil().createNodeElement(
                DialogConstants.NN_COLUMN,
                Collections.singletonMap(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, ResourceTypes.CONTAINER)
        );

        parentElement.appendChild(content);

        content.appendChild(layout);
        content.appendChild(contentItems);

        contentItems.appendChild(column);

        List<Field> allFields = PluginReflectionUtility.getAllNonStaticFields(clazz);
        Handler.appendContainer(allFields, column);
    }
}
