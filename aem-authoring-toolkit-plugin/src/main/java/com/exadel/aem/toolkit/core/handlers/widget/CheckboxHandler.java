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
import com.exadel.aem.toolkit.api.handlers.TargetBuilder;
import com.exadel.aem.toolkit.core.TargetBuilderImpl;
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
class CheckboxHandler implements Handler, BiConsumer<SourceFacade, TargetBuilder> {
    private static final String POSTFIX_FOR_ROOT_CHECKBOX = "Checkbox";

    /**
     * Processes the user-defined data and writes it to XML entity
     * @param source Current {@code SourceFacade} instance
     * @param target Current {@code TargetFacade} instance
     */
    @Override
    public void accept(SourceFacade source, TargetBuilder target) {
        Checkbox checkbox = source.adaptTo(Checkbox.class);

        if (checkbox.sublist()[0] == Object.class) {
            target.mapProperties(checkbox);
            setTextAttribute(source, target);
        } else {
            target.attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.NESTED_CHECKBOX_LIST);
            TargetBuilder itemsElement = new TargetBuilderImpl(DialogConstants.NN_ITEMS);
            target.appendChild(itemsElement);

            TargetBuilder checkboxElement = new TargetBuilderImpl(((Member) source.getSource()).getName() + POSTFIX_FOR_ROOT_CHECKBOX)
                    .attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.CHECKBOX);
            checkboxElement.mapProperties(checkbox);
            itemsElement.appendChild(checkboxElement);

            appendNestedCheckBoxList(source, checkboxElement);
        }
    }

    /**
     * Creates and appends markup correspondent to a Granite UI nested {@code Checkbox} structure
     * @param source Current {@code SourceFacade} of a component class
     * @param target {@code TargetFacade} instance representing current XML node
     */
    private void appendNestedCheckBoxList(SourceFacade source, TargetBuilder target) {
        TargetBuilder sublist = new TargetBuilderImpl(DialogConstants.NN_SUBLIST)
                .attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.NESTED_CHECKBOX_LIST);
        sublist.attribute(DialogConstants.PN_DISCONNECTED, source.adaptTo(Checkbox.class).disconnectedSublist());
        target.appendChild(sublist);

        TargetBuilder itemsElement = new TargetBuilderImpl(DialogConstants.NN_ITEMS);
        sublist.appendChild(itemsElement);

        appendCheckbox(source, itemsElement);
    }

    /**
     * Creates and appends single Granite UI {@code Checkbox} markup to the current XML node
     * @param source Current {@code SourceFacade} of a component class
     * @param target {@code TargetFacade} instance representing current XML node
     */
    private void appendCheckbox(SourceFacade source, TargetBuilder target) {
        Checkbox checkbox = source.adaptTo(Checkbox.class);

        for (Class<?> sublistClass : checkbox.sublist()) {
            List<SourceFacade> sources = PluginReflectionUtility.getAllSourceFacades(sublistClass).stream()
                    .filter(f -> f.adaptTo(Checkbox.class) != null)
                    .collect(Collectors.toList());

            for (SourceFacade innerSourceFacade : sources) {
                TargetBuilder checkboxElement = new TargetBuilderImpl(((Member) innerSourceFacade.getSource()).getName())
                        .attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.CHECKBOX);
                checkboxElement.mapProperties(innerSourceFacade.adaptTo(Checkbox.class));
                setTextAttribute(innerSourceFacade, checkboxElement);

                target.appendChild(checkboxElement);

                if (innerSourceFacade.adaptTo(Checkbox.class).sublist()[0] != Object.class) {
                    appendNestedCheckBoxList(innerSourceFacade, checkboxElement);
                }
            }
        }
    }

    /**
     * Decides which property of the current field to use as the {@code text} attribute of checkbox node and populates it
     * @param source Current {@code SourceFacade} of a component class
     * @param target {@code TargetFacade} instance representing current XML node
     */
    private void setTextAttribute(SourceFacade source, TargetBuilder target) {
        Checkbox checkbox = source.adaptTo(Checkbox.class);
        if (checkbox.text().isEmpty() && source.adaptTo(DialogField.class) != null) {
            target.attribute(DialogConstants.PN_TEXT, source.adaptTo(DialogField.class).label());
        } else if (checkbox.text().isEmpty()) {
            target.attribute(DialogConstants.PN_TEXT, ((Member) source.getSource()).getName());
        }
    }
}
