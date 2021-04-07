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
package com.exadel.aem.toolkit.plugin.handlers.widgets;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.widgets.Checkbox;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.handlers.Handler;
import com.exadel.aem.toolkit.api.handlers.Handles;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.utils.AnnotationUtil;
import com.exadel.aem.toolkit.plugin.utils.ClassUtil;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;

/**
 * Implements {@code BiConsumer} to populate a {@link Target} instance with properties originating from a {@link Source}
 * object that define the Granite UI {@code Checkbox} widget look and behavior
 */
@Handles(Checkbox.class)
public class CheckboxHandler implements Handler {
    private static final String POSTFIX_FOR_ROOT_CHECKBOX = "Checkbox";

    /**
     * Processes data that can be extracted from the given {@code Source} and stores it into the provided {@code Target}
     * @param source {@code Source} object used for data retrieval
     * @param target Resulting {@code Target} object
     */
    @Override
    public void accept(Source source, Target target) {
        Checkbox checkbox = source.adaptTo(Checkbox.class);

        Predicate<Method> mappingFilter = AnnotationUtil.getPropertyMappingFilter(checkbox);
        if (ArrayUtils.isEmpty(checkbox.sublist())) {
            target.attributes(checkbox, mappingFilter);
            setTextAttribute(source, target);
        } else {
            Target checkboxElement = target.attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.NESTED_CHECKBOX_LIST)
                    .getOrCreateTarget(DialogConstants.NN_ITEMS)
                    .getOrCreateTarget(source.getName() + POSTFIX_FOR_ROOT_CHECKBOX)
                    .attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.CHECKBOX)
                    .attributes(checkbox, mappingFilter);

            appendNestedCheckBoxList(source, checkboxElement);
        }
    }

    /**
     * Creates and appends markup correspondent to a Granite UI nested {@code Checkbox} structure
     * @param source Current {@link Source} of a component class
     * @param target {@link Target} instance representing current node
     */
    private void appendNestedCheckBoxList(Source source, Target target) {
        Target itemsElement = target.getOrCreateTarget(DialogConstants.NN_SUBLIST)
                .attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.NESTED_CHECKBOX_LIST)
                .attribute(DialogConstants.PN_DISCONNECTED, source.adaptTo(Checkbox.class).disconnectedSublist())
                .getOrCreateTarget(DialogConstants.NN_ITEMS);

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
            List<Source> sources = ClassUtil.getSources(sublistClass).stream()
                    .filter(f -> f.adaptTo(Checkbox.class) != null)
                    .collect(Collectors.toList());

            for (Source innerSource : sources) {
                Checkbox innerCheckbox = innerSource.adaptTo(Checkbox.class);
                Target checkboxElement = target.getOrCreateTarget(innerSource.getName())
                        .attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.CHECKBOX)
                        .attributes(innerCheckbox, AnnotationUtil.getPropertyMappingFilter(innerCheckbox));
                setTextAttribute(innerSource, checkboxElement);

                if (ArrayUtils.isNotEmpty(innerSource.adaptTo(Checkbox.class).sublist())) {
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
            target.attribute(CoreConstants.PN_TEXT, source.adaptTo(DialogField.class).label());
        } else if (checkbox.text().isEmpty()) {
            target.attribute(CoreConstants.PN_TEXT, source.getName());
        }
    }
}
