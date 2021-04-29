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
package com.exadel.aem.toolkit.plugin.handlers.widgets.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.ImmutableMap;

import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.MultiField;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.Multiple;
import com.exadel.aem.toolkit.api.annotations.widgets.property.Property;
import com.exadel.aem.toolkit.api.handlers.Handler;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.handlers.common.CasualAnnotationsHandler;
import com.exadel.aem.toolkit.plugin.handlers.widgets.MultiFieldHandler;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.targets.Targets;
import com.exadel.aem.toolkit.plugin.utils.ClassUtil;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;

/**
 * Implements {@code BiConsumer} to modify {@link Target} instance in order to create an ad-hoc {@code Multifield}
 * for a {@link Multiple}-marked dialog field
 */

public class MultipleAnnotationHandler implements BiConsumer<Source, Target> {
    private static final String POSTFIX_NESTED = "_nested";

    /**
     * Processes data that can be extracted from the given {@code Source} and stores it into the provided {@code Target}
     * @param source {@code Source} object used for data retrieval
     * @param target Resulting {@code Target} object
     */
    @Override
    public void accept(Source source, Target target) {
        if (source.adaptTo(Multiple.class) == null) {
            return;
        }

        boolean isComposite = false;
        if (isFieldSet(target)) {
            wrapFieldSet(target);
            isComposite = true;

        } else if (isMultifield(target)) {
            wrapNestedMultifield(source, target);
            isComposite = true;

        } else {
            wrapSingularField(source, target);
        }

        // Facilitate the modified targetFacade to work as Multifield
        if (isComposite) {
            target.getOrCreateTarget(DialogConstants.NN_FIELD).attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.CONTAINER);
            target.attribute(DialogConstants.PN_COMPOSITE, true);
        }
        target.getAttributes().remove(DialogConstants.PN_NAME);
        target.attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.MULTIFIELD);

        // Since this targetFacade has just "emerged" as a multifield, we need to check if there are multifield bound
        // (custom) handlers and run such one more time
        List<Handler> multifieldHandlers = PluginRuntime
            .context()
            .getReflection()
            .getHandlers(target.getScope(), MultiField.class)
            .stream() // Because the built-in MultiField handler should not be applied for the second time, only custom ones
            .filter(handler -> !handler.getClass().equals(MultiFieldHandler.class))
            .collect(Collectors.toList());
        new CasualAnnotationsHandler(multifieldHandlers).accept(source, target);
    }

    /**
     * Gets whether the given {@link Target} is a representation of a fieldset (judged by the fact it has a nonempty subnode
     * named "items")
     * @param target {@code Target} instance
     * @return True or false
     */
    private boolean isFieldSet(Target target) {
        return target.getTarget(DialogConstants.NN_ITEMS) != null
            && !target.getTarget(DialogConstants.NN_ITEMS).getChildren().isEmpty();
    }

    /**
     * Gets whether the currently rendered XML element is a Granite {@code Multifield} element (judged by the fact
     * it has an appropriate resource type and a non-empty subnode named "items")
     * @param target {@code Target} instance
     * @return True or false
     */
    private boolean isMultifield(Target target) {
        if (StringUtils.equals(target.getAttribute(DialogConstants.PN_SLING_RESOURCE_TYPE), ResourceTypes.MULTIFIELD)) {
            return true;
        }
        boolean hasSingularFieldNode = target.getChildren().size() == 1
            && DialogConstants.NN_FIELD.equals(target.getChildren().get(0).getName());
        if (!hasSingularFieldNode) {
            return false;
        }
        // Assuming this is a custom multifield, i.e. the target does not match any of the known resource types
        return ClassUtil.getConstantValues(ResourceTypes.class).values()
            .stream()
            .map(Object::toString)
            .noneMatch(restype -> restype.equals(target.getAttribute(DialogConstants.PN_SLING_RESOURCE_TYPE)));
    }

    /**
     * Transforms the provided {@code Target} by internally moving child entities and attributes so that a nested
     * field structure created
     * @param source {@code Source} instance referring to the class member being processed
     * @param target Previously created {@code Target} being converted to a synthetic multifield
     */
    private void wrapSingularField(Source source, Target target) {
        Target fieldSubresource = target.createTarget(DialogConstants.NN_FIELD);
        // Move content to the new wrapper
        transferProperties(target, fieldSubresource, getTransferPolicies(source));
    }

    /**
     * Transforms the provided {@code Target} by internally moving child entities and attributes so that a nested
     * FieldSet structure created
     * @param target Previously created {@code Target} being converted to a synthetic multifield
     */
    private void wrapFieldSet(Target target) {
        Target fieldSubresource = target.createTarget(DialogConstants.NN_FIELD);
        // Get the existing "items" node and remove leading "./"-s from "name" attributes of particular items
        Target itemsSubresource = target.getTarget(DialogConstants.NN_ITEMS);
        itemsSubresource.getChildren().forEach(child -> {
            String modifiedName = StringUtils.removeStart(
                child.getAttributes().get(DialogConstants.PN_NAME),
                DialogConstants.RELATIVE_PATH_PREFIX);
            child.attribute(DialogConstants.PN_NAME, modifiedName);
        });
        // Append the existing "items" subresource to the newly created "field" subresource
        fieldSubresource.getChildren().add(itemsSubresource);
        target.getChildren().remove(itemsSubresource);
        // Move "name" property of the passed target to the "field" subresource
        transferProperties(
                target,
                fieldSubresource,
                ImmutableMap.of(CoreConstants.SEPARATOR_AT + DialogConstants.PN_NAME, PropertyTransferPolicy.MOVE_TO_NESTED_NODE));
    }

    /**
     * Creates a {@code source} node wrapping an existing {@code multifield} that will be used within a synthetic multifield
     * @param source {@code Source} instance referring to the class member being processed
     * @param target Previously created {@code Target} being converted to a synthetic multifield
     */
    private void wrapNestedMultifield(Source source, Target target) {
        // We will create new "field" subresource, but we need it "detached" not to mingle with existing "field" subresource
        Target fieldSubresource = Targets.newInstance(DialogConstants.NN_FIELD, target);
        Target itemsSubresource = fieldSubresource.createTarget(DialogConstants.NN_ITEMS);
        Target nestedMultifield = itemsSubresource.createTarget(source.getName() + POSTFIX_NESTED);

        // Move existing multifield attributes to the nested multifield
        Map<String, PropertyTransferPolicy> standardPolicies = getTransferPolicies(source);
        Map<String, PropertyTransferPolicy> multifieldPolicies = new LinkedHashMap<>();
        multifieldPolicies.put(CoreConstants.SEPARATOR_AT + DialogConstants.PN_COMPOSITE, PropertyTransferPolicy.COPY_TO_NESTED_NODE);
        multifieldPolicies.put(DialogConstants.RELATIVE_PATH_PREFIX + DialogConstants.NN_FIELD, PropertyTransferPolicy.MOVE_TO_NESTED_NODE);
        standardPolicies.forEach(multifieldPolicies::put);
        multifieldPolicies.put(CoreConstants.SEPARATOR_AT + DialogConstants.PN_SLING_RESOURCE_TYPE, PropertyTransferPolicy.COPY_TO_NESTED_NODE);
        transferProperties(target, nestedMultifield, multifieldPolicies);

        // Set the "name" attribute of the "source" node of the current multifield
        // At the same time, alter the "name" attribute of the nested multifield to not get mixed with the name of the current one
        Target nestedMultifieldFieldSubresource = nestedMultifield.getTarget(DialogConstants.NN_FIELD);
        String nestedMultifieldFieldName = StringUtils.defaultString(nestedMultifieldFieldSubresource.getAttributes().get(DialogConstants.PN_NAME));

        fieldSubresource.attribute(DialogConstants.PN_NAME, nestedMultifieldFieldName);
        nestedMultifieldFieldSubresource.getAttributes().put(DialogConstants.PN_NAME, nestedMultifieldFieldName + POSTFIX_NESTED);
        target.getChildren().add(fieldSubresource);
    }

    /**
     * Generates set of node transfer policies to properly distribute XML element requisites between the wrapper level
     * and the nested node level while converting a singular element to a multifield
     * @param source {@code Source} instance referring to the class member being processed
     * @return Map containing attribute/child names, either plain or wild-carded, and the action appropriate, whether
     * to copy element, move, or leave intact. The wildcard symbol ({@code *}) is to specify a common policy for multiple
     * elements
     */
    private static Map<String, PropertyTransferPolicy> getTransferPolicies(Source source) {
        Map<String, PropertyTransferPolicy> transferPolicies = new LinkedHashMap<>();

        // All "standard" @DialogField props will belong to the Multifield node unless in the cases specified below,
        // while "widget-specific" properties will move to the "inner" node
        Arrays.stream(DialogField.class.getDeclaredMethods())
                .forEach(method -> {
                    String propertyName = method.getAnnotation(PropertyRendering.class) != null
                            ? StringUtils.defaultIfEmpty(method.getAnnotation(PropertyRendering.class).name(), method.getName())
                            : method.getName();
                    transferPolicies.put(CoreConstants.SEPARATOR_AT + propertyName, PropertyTransferPolicy.LEAVE_IN_MULTIFIELD);
                });
        // Also, all the values set via @Property will belong to the Multifield node
        Arrays.stream(source.adaptTo(Property[].class))
            .forEach(property -> transferPolicies.put(CoreConstants.SEPARATOR_AT + property.name(), PropertyTransferPolicy.LEAVE_IN_MULTIFIELD));
        // Need to override policy for "name" as has been stored in a loop above
        transferPolicies.put(CoreConstants.SEPARATOR_AT + DialogConstants.PN_NAME, PropertyTransferPolicy.MOVE_TO_NESTED_NODE);
        // Some attribute values are expected to be moved or copied though have set to "skipped" above
        transferPolicies.put(CoreConstants.SEPARATOR_AT + DialogConstants.PN_PRIMARY_TYPE, PropertyTransferPolicy.COPY_TO_NESTED_NODE);
        transferPolicies.put(CoreConstants.SEPARATOR_AT + DialogConstants.PN_DISABLED, PropertyTransferPolicy.COPY_TO_NESTED_NODE);
        transferPolicies.put(CoreConstants.SEPARATOR_AT + DialogConstants.PN_RENDER_HIDDEN, PropertyTransferPolicy.COPY_TO_NESTED_NODE);
        transferPolicies.put(CoreConstants.SEPARATOR_AT + DialogConstants.PN_REQUIRED, PropertyTransferPolicy.MOVE_TO_NESTED_NODE);
        // Rest of element attributes will move to the inner node
        transferPolicies.put(CoreConstants.SEPARATOR_AT + DialogConstants.WILDCARD, PropertyTransferPolicy.MOVE_TO_NESTED_NODE);
        // While all child nodes will stay as the properties of multifield
        transferPolicies.put(DialogConstants.RELATIVE_PATH_PREFIX + DialogConstants.WILDCARD, PropertyTransferPolicy.LEAVE_IN_MULTIFIELD);

        return transferPolicies;
    }

    /**
     * Migrates attributes and child nodes between {@code source} and {@code target}. Whether particular attributes
     * and child nodes are copied, moved or left alone, is defined by the {@code policies} map
     * @param from     Element to serve as the source of migration
     * @param to       Element to serve as the target of migration
     * @param policies Map containing attribute names (must start with {@code @}), child node names (must start with
     *                 {@code ./}) and the appropriate action, whether to copy element, move, or leave intact. The wildcard
     *                 symbol ({@code *}) is to specify a common policy for multiple elements
     */
    private static void transferProperties(Target from, Target to, Map<String, PropertyTransferPolicy> policies) {
        // Process attributes
        List<String> removableAttributes = new ArrayList<>();
        for (String attribute : from.getAttributes().keySet()) {
            PropertyTransferPolicy policy = getPolicyForProperty(
                policies,
                CoreConstants.SEPARATOR_AT + attribute);
            if (policy != PropertyTransferPolicy.LEAVE_IN_MULTIFIELD) {
                to.attribute(attribute, from.getAttributes().get(attribute));
            }
            if (policy == PropertyTransferPolicy.MOVE_TO_NESTED_NODE) {
                removableAttributes.add(attribute);
            }
        }
        removableAttributes.forEach(attribute -> from.getAttributes().remove(attribute));
        // Process child nodes
        List<Target> removableChildren = new ArrayList<>();
        for (Target child : from.getChildren()) {
            PropertyTransferPolicy policy = getPolicyForProperty(
                policies,
                DialogConstants.RELATIVE_PATH_PREFIX + child.getName());
            if (policy == PropertyTransferPolicy.LEAVE_IN_MULTIFIELD) {
                continue;
            }
            to.getChildren().add(child);
            if (policy == PropertyTransferPolicy.MOVE_TO_NESTED_NODE) {
                removableChildren.add(child);
            }
        }
        removableChildren.forEach(child -> from.getChildren().remove(child));
    }

    /**
     * Called to pick up an appropriate {@link PropertyTransferPolicy}
     * from the set of provided policies
     * @param policies {@code Map} describing available policies
     * @param propertyToken String representing the name of the current attribute or child node
     * @return The selected policy, or the default policy if no appropriate option found
     */
    private static PropertyTransferPolicy getPolicyForProperty(
        Map<String, PropertyTransferPolicy> policies,
        String propertyToken) {
        return policies.entrySet().stream()
            .filter(entry -> entry.getKey().endsWith(DialogConstants.WILDCARD)
                ? propertyToken.startsWith(StringUtils.stripEnd(entry.getKey(), DialogConstants.WILDCARD))
                : propertyToken.equals(entry.getKey()))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(PropertyTransferPolicy.LEAVE_IN_MULTIFIELD);
    }

    /**
     * Enumerates available property transfer policies
     */
    private enum PropertyTransferPolicy {
        LEAVE_IN_MULTIFIELD,
        COPY_TO_NESTED_NODE,
        MOVE_TO_NESTED_NODE
    }
}
