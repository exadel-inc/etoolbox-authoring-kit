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

import java.lang.reflect.Field;
import java.util.function.BiConsumer;

import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.Multiple;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;

import static com.exadel.aem.toolkit.core.util.DialogConstants.NN_ITEM;
import static com.exadel.aem.toolkit.core.util.DialogConstants.PN_NAME;

public class MultipleHandler implements BiConsumer<Element, Field> {
    @Override
    public void accept(Element element, Field field) {
        if (!field.isAnnotationPresent(Multiple.class)) {
            return;
        }
        Element itemElement = PluginRuntime.context().getXmlUtility().createNodeElement(NN_ITEM);
        PluginRuntime.context().getXmlUtility().migrateElementContent(element, itemElement);
        element.appendChild(itemElement);
        element.removeAttribute(PN_NAME);
        element.setAttribute(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, ResourceTypes.MULTIFIELD);
    }
}
