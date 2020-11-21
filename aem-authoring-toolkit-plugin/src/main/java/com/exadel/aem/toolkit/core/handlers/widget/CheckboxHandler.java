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
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;

import java.lang.reflect.Member;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * {@code BiConsumer<Source, Target>} implementation used to create markup responsible for Granite UI {@code Checkbox} widget functionality
 * within the {@code cq:dialog} node
 */
class CheckboxHandler implements BiConsumer<Source, Target> {
    private static final String POSTFIX_FOR_ROOT_CHECKBOX = "Checkbox";

    /**
     * Processes the user-defined data and writes it to {@link Target}
     * @param source Current {@link Source} instance
     * @param target Current {@link Target} instance
     */
    @Override
    public void accept(Source source, Target target) {
        Checkbox checkbox = source.adaptTo(Checkbox.class);

        if (checkbox.sublist()[0] == Object.class) {
            target.mapProperties(checkbox);
            setTextAttribute(source, target);
        } else {
            Target checkboxElement = target.attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.NESTED_CHECKBOX_LIST)
                    .child(DialogConstants.NN_ITEMS)
                    .child(((Member) source.getSource()).getName() + POSTFIX_FOR_ROOT_CHECKBOX)
                    .attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.CHECKBOX)
                    .mapProperties(checkbox);

            appendNestedCheckBoxList(source, checkboxElement);
        }
    }

    /**
     * Creates and appends markup correspondent to a Granite UI nested {@code Checkbox} structure
     * @param source Current {@link Source} of a component class
     * @param target {@link Target} instance representing current node
     */
    private void appendNestedCheckBoxList(Source source, Target target) {
        Target itemsElement = target.child(DialogConstants.NN_SUBLIST)
                .attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.NESTED_CHECKBOX_LIST)
                .attribute(DialogConstants.PN_DISCONNECTED, source.adaptTo(Checkbox.class).disconnectedSublist())
                .child(DialogConstants.NN_ITEMS);

        appendCheckbox(source, itemsElement);
    }

    /**
     * Creates and appends single Granite UI {@code Checkbox} markup to the current XML node
     * @param source Current {@link Source} of a component class
     * @param target {@link Target} instance representing current node
     */
    private void appendCheckbox(Source source, Target target) {
        Checkbox checkbox = source.adaptTo(Checkbox.class);

        for (Class<?> sublistClass : checkbox.sublist()) {
            List<Source> sources = PluginReflectionUtility.getAllSourceFacades(sublistClass).stream()
                    .filter(f -> f.adaptTo(Checkbox.class) != null)
                    .collect(Collectors.toList());

            for (Source innerSource : sources) {
                Target checkboxElement = target.child(((Member) innerSource.getSource()).getName())
                        .attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.CHECKBOX)
                        .mapProperties(innerSource.adaptTo(Checkbox.class));
                setTextAttribute(innerSource, checkboxElement);

                if (innerSource.adaptTo(Checkbox.class).sublist()[0] != Object.class) {
                    appendNestedCheckBoxList(innerSource, checkboxElement);
                }
            }
        }
    }

    /**
     * Decides which property of the current field to use as the {@code text} attribute of checkbox node and populates it
     * @param source Current {@link Source} of a component class
     * @param target {@link Target} instance representing current node
     */
    private void setTextAttribute(Source source, Target target) {
        Checkbox checkbox = source.adaptTo(Checkbox.class);
        if (checkbox.text().isEmpty() && source.adaptTo(DialogField.class) != null) {
            target.attribute(DialogConstants.PN_TEXT, source.adaptTo(DialogField.class).label());
        } else if (checkbox.text().isEmpty()) {
            target.attribute(DialogConstants.PN_TEXT, ((Member) source.getSource()).getName());
        }
    }
}
