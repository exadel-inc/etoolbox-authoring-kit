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

package com.exadel.aem.toolkit.core.handlers;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.runtime.XmlUtility;
import com.exadel.aem.toolkit.core.handlers.widget.DialogWidget;
import com.exadel.aem.toolkit.core.handlers.widget.DialogWidgets;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.exadel.aem.toolkit.core.util.PluginXmlUtility;

/**
 * Manifests the common interface for AEM Authoring Toolkit plugin {@code widget}s', {@code editConfig}s',
 * {@code container}s' handlers. Encapsulates utility methods for fitting containers and widgets together
 */
public interface Handler {
    /**
     * Shortcut method for getting the {@link XmlUtility} descendant associated with the current context
     * @return The {@link PluginXmlUtility} instance
     */
    default PluginXmlUtility getXmlUtil() {
        return PluginRuntime.context().getXmlUtility();
    }

    /**
     * Processes the specified {@link Field}s and appends the generated XML markup to the specified container element
     * @param fields List of {@code Field}s of a component's Java class
     * @param containerElement XML definition of a pre-defined widget container
     */
    static void appendToContainer(List<Field> fields, Element containerElement) {
        appendToContainer(fields, containerElement, true);
    }

    /**
     * Processes the specified {@link Field}s and appends the generated XML markup to the specified container element
     * @param fields List of {@code Field}s of a component's Java class
     * @param containerElement XML definition of a pre-defined widget container
     * @param setResourceType True if particular {@code sling:resourceType} value must be set to the container element;
     *                        otherwise, false
     */
    static void appendToContainer(List<Field> fields, Element containerElement, boolean setResourceType){
        if (setResourceType) {
            containerElement.setAttribute(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, ResourceTypes.CONTAINER);
        }
        Element itemsElement = PluginRuntime.context().getXmlUtility().createNodeElement(DialogConstants.NN_ITEMS);
        containerElement.appendChild(itemsElement);

        for (Field field : fields) {
            DialogWidget widget = DialogWidgets.fromField(field);
            if (widget == null) {
                continue;
            }
            widget.append(itemsElement, field);
        }
    }
}
