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

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.widgets.Checkbox;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.handlers.SourceFacade;
import com.exadel.aem.toolkit.api.handlers.TargetFacade;
import com.exadel.aem.toolkit.core.TargetFacadeFacadeImpl;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;

import java.lang.reflect.Member;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * {@link Handler} implementation used to create markup responsible for Granite UI {@code Checkbox} widget functionality
 * within the {@code cq:dialog} XML node
 */
class CheckboxHandler implements Handler, BiConsumer<SourceFacade, TargetFacade> {
    private static final String POSTFIX_FOR_ROOT_CHECKBOX = "Checkbox";

    /**
     * Processes the user-defined data and writes it to XML entity
     * @param sourceFacade Current {@code SourceFacade} instance
     * @param targetFacade Current {@code TargetFacade} instance
     */
    @Override
    public void accept(SourceFacade sourceFacade, TargetFacade targetFacade) {
        Checkbox checkbox = sourceFacade.adaptTo(Checkbox.class);

        if (checkbox.sublist()[0] == Object.class) {
            targetFacade.mapProperties(checkbox);
            setTextAttribute(sourceFacade, targetFacade);
        } else {
            targetFacade.setAttribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.NESTED_CHECKBOX_LIST);
            TargetFacade itemsElement = new TargetFacadeFacadeImpl(DialogConstants.NN_ITEMS);
            targetFacade.appendChild(itemsElement);

            TargetFacade checkboxElement = new TargetFacadeFacadeImpl(((Member) sourceFacade.getSource()).getName() + POSTFIX_FOR_ROOT_CHECKBOX)
                    .setAttribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.CHECKBOX);
            checkboxElement.mapProperties(checkbox);
            itemsElement.appendChild(checkboxElement);

            appendNestedCheckBoxList(sourceFacade, checkboxElement);
        }
    }

    /**
     * Creates and appends markup correspondent to a Granite UI nested {@code Checkbox} structure
     * @param sourceFacade Current {@code SourceFacade} of a component class
     * @param targetFacade {@code TargetFacade} instance representing current XML node
     */
    private void appendNestedCheckBoxList(SourceFacade sourceFacade, TargetFacade targetFacade) {
        TargetFacade sublist = new TargetFacadeFacadeImpl(DialogConstants.NN_SUBLIST)
                .setAttribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.NESTED_CHECKBOX_LIST);
        sublist.setAttribute(DialogConstants.PN_DISCONNECTED, sourceFacade.adaptTo(Checkbox.class).disconnectedSublist());
        targetFacade.appendChild(sublist);

        TargetFacade itemsElement = new TargetFacadeFacadeImpl(DialogConstants.NN_ITEMS);
        sublist.appendChild(itemsElement);

        appendCheckbox(sourceFacade, itemsElement);
    }

    /**
     * Creates and appends single Granite UI {@code Checkbox} markup to the current XML node
     * @param sourceFacade Current {@code SourceFacade} of a component class
     * @param targetFacade {@code TargetFacade} instance representing current XML node
     */
    private void appendCheckbox(SourceFacade sourceFacade, TargetFacade targetFacade) {
        Checkbox checkbox = sourceFacade.adaptTo(Checkbox.class);

        for (Class<?> sublistClass : checkbox.sublist()) {
            List<SourceFacade> sourceFacades = PluginReflectionUtility.getAllSourceFacades(sublistClass).stream()
                    .filter(f -> f.adaptTo(Checkbox.class) != null)
                    .collect(Collectors.toList());

            for (SourceFacade innerSourceFacade : sourceFacades) {
                TargetFacade checkboxElement = new TargetFacadeFacadeImpl(((Member) innerSourceFacade.getSource()).getName())
                        .setAttribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.CHECKBOX);
                checkboxElement.mapProperties(innerSourceFacade.adaptTo(Checkbox.class));
                setTextAttribute(innerSourceFacade, checkboxElement);

                targetFacade.appendChild(checkboxElement);

                if (innerSourceFacade.adaptTo(Checkbox.class).sublist()[0] != Object.class) {
                    appendNestedCheckBoxList(innerSourceFacade, checkboxElement);
                }
            }
        }
    }

    /**
     * Decides which property of the current field to use as the {@code text} attribute of checkbox node and populates it
     * @param sourceFacade Current {@code SourceFacade} of a component class
     * @param targetFacade {@code TargetFacade} instance representing current XML node
     */
    private void setTextAttribute(SourceFacade sourceFacade, TargetFacade targetFacade) {
        Checkbox checkbox = sourceFacade.adaptTo(Checkbox.class);
        if (checkbox.text().isEmpty() && sourceFacade.adaptTo(DialogField.class) != null) {
            targetFacade.setAttribute(DialogConstants.PN_TEXT, sourceFacade.adaptTo(DialogField.class).label());
        } else if (checkbox.text().isEmpty()) {
            targetFacade.setAttribute(DialogConstants.PN_TEXT, ((Member) sourceFacade.getSource()).getName());
        }
    }
}
