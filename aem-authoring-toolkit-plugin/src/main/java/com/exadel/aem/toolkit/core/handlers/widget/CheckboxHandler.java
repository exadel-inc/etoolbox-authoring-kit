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
package com.exadel.aem.toolkit.core.handlers.widget;

import java.lang.reflect.Member;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.exadel.aem.toolkit.api.handlers.SourceFacade;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.widgets.Checkbox;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;

/**
 * {@link Handler} implementation used to create markup responsible for Granite UI {@code Checkbox} widget functionality
 * within the {@code cq:dialog} XML node
 */
class CheckboxHandler implements Handler, BiConsumer<SourceFacade, Element> {
    private static final String POSTFIX_FOR_ROOT_CHECKBOX = "Checkbox";

    /**
     * Processes the user-defined data and writes it to XML entity
     * @param sourceFacade Current {@code SourceFacade} instance
     * @param element Current XML element
     */
    @Override
    public void accept(SourceFacade sourceFacade, Element element) {
        Checkbox checkbox = sourceFacade.adaptTo(Checkbox.class);

        if (checkbox.sublist()[0] == Object.class) {
            getXmlUtil().mapProperties(element, checkbox);
            setTextAttribute(sourceFacade, element);
        } else {
            element.setAttribute(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, ResourceTypes.NESTED_CHECKBOX_LIST);
            Element itemsElement = getXmlUtil().createNodeElement(DialogConstants.NN_ITEMS);
            element.appendChild(itemsElement);

            Element checkboxElement = getXmlUtil().createNodeElement(((Member) sourceFacade.getSource()).getName() + POSTFIX_FOR_ROOT_CHECKBOX, ResourceTypes.CHECKBOX);
            getXmlUtil().mapProperties(checkboxElement, checkbox);
            itemsElement.appendChild(checkboxElement);

            appendNestedCheckBoxList(sourceFacade, checkboxElement);
        }
    }

    /**
     * Creates and appends markup correspondent to a Granite UI nested {@code Checkbox} structure
     * @param sourceFacade Current {@code SourceFacade} of a component class
     * @param element {@code Element} instance representing current XML node
     */
    private void appendNestedCheckBoxList(SourceFacade sourceFacade, Element element) {
        Element sublist = getXmlUtil().createNodeElement(DialogConstants.NN_SUBLIST, ResourceTypes.NESTED_CHECKBOX_LIST);
        getXmlUtil().setAttribute(sublist, DialogConstants.PN_DISCONNECTED, sourceFacade.adaptTo(Checkbox.class).disconnectedSublist());
        element.appendChild(sublist);

        Element itemsElement = getXmlUtil().createNodeElement(DialogConstants.NN_ITEMS);
        sublist.appendChild(itemsElement);

        appendCheckbox(sourceFacade, itemsElement);
    }

    /**
     * Creates and appends single Granite UI {@code Checkbox} markup to the current XML node
     * @param sourceFacade Current {@code SourceFacade} of a component class
     * @param element {@code Element} instance representing current XML node
     */
    private void appendCheckbox(SourceFacade sourceFacade, Element element) {
        Checkbox checkbox = sourceFacade.adaptTo(Checkbox.class);

        for (Class<?> sublistClass : checkbox.sublist()) {
            List<SourceFacade> sourceFacades = PluginReflectionUtility.getAllSourceFacades(sublistClass).stream()
                    .filter(f -> f.adaptTo(Checkbox.class) != null)
                    .collect(Collectors.toList());

            for (SourceFacade innerSourceFacade : sourceFacades) {
                Element checkboxElement = getXmlUtil().createNodeElement(((Member) innerSourceFacade.getSource()).getName(), ResourceTypes.CHECKBOX);
                getXmlUtil().mapProperties(checkboxElement, innerSourceFacade.adaptTo(Checkbox.class));
                setTextAttribute(innerSourceFacade, checkboxElement);

                element.appendChild(checkboxElement);

                if (innerSourceFacade.adaptTo(Checkbox.class).sublist()[0] != Object.class) {
                    appendNestedCheckBoxList(innerSourceFacade, checkboxElement);
                }
            }
        }
    }

    /**
     * Decides which property of the current field to use as the {@code text} attribute of checkbox node and populates it
     * @param sourceFacade Current {@code SourceFacade} of a component class
     * @param element {@code Element} instance representing current XML node
     */
    private void setTextAttribute(SourceFacade sourceFacade, Element element) {
        Checkbox checkbox = sourceFacade.adaptTo(Checkbox.class);
        if (checkbox.text().isEmpty() && sourceFacade.adaptTo(DialogField.class) != null) {
            element.setAttribute(DialogConstants.PN_TEXT, sourceFacade.adaptTo(DialogField.class).label());
        } else if (checkbox.text().isEmpty()) {
            element.setAttribute(DialogConstants.PN_TEXT, ((Member) sourceFacade.getSource()).getName());
        }
    }
}
