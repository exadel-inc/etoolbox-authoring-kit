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

package com.exadel.aem.toolkit.plugin.handlers.widget.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.ImmutableMap;

import com.exadel.aem.toolkit.api.annotations.main.JcrConstants;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.MultiField;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.Multiple;
import com.exadel.aem.toolkit.api.annotations.widgets.property.Property;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.target.TargetImpl;
import com.exadel.aem.toolkit.plugin.util.DialogConstants;

/**
 * Handler for creating ad-hoc {@code Multifield}s for {@link Multiple}-marked dialog fields
 */
public class MultipleAnnotationHandler implements BiConsumer<Source, Target> {
    private static final String PREFIX_GRANITE = "granite:*";
    private static final String POSTFIX_NESTED = "_nested";


    /**
     * Processes the user-defined data and writes it to XML entity
     * @param source Current {@code SourceFacade} instance
     * @param target XML targetFacade
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
            target.get(DialogConstants.NN_FIELD).attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.CONTAINER);
            target.attribute(DialogConstants.PN_COMPOSITE, true);
        }
        target.getAttributes().remove(DialogConstants.PN_NAME);
        target.attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.MULTIFIELD);

        // Since this targetFacade has just "emerged" as a multifield, we need to check if there are multifield bound
        // (custom) handlers and run such one more time
        PluginRuntime.context().getReflectionUtility().getCustomDialogWidgetHandlers(Collections.singletonList(MultiField.class))
                .forEach(handler -> handler.accept(source, target));
    }

    /**
     * Gets whether the given {@link Target} is a representation of a fieldset (judged by the fact it has an nonempty subnode
     * named "items")
     * @param target {@code Target} instance
     * @return True or false
     */
    private boolean isFieldSet(Target target) {
        return target.get(DialogConstants.NN_ITEMS) != null
            && !target.get(DialogConstants.NN_ITEMS).getChildren().isEmpty();
    }

    /**
     * Gets whether the currently rendered XML element is a Granite {@code Multifield} element (judged by the fact
     * it has appropriate resource type and a nonempty subnode named "items")
     * @param target {@code Target} instance
     * @return True or false
     */
    private boolean isMultifield(Target target) {
        return StringUtils.equals(target.getAttributes().get(DialogConstants.PN_SLING_RESOURCE_TYPE), ResourceTypes.MULTIFIELD)
            && target.get(DialogConstants.NN_FIELD) != null
            && !target.get(DialogConstants.NN_FIELD).getChildren().isEmpty();
    }

    /**
     * Creates a {@code source} node encapsulating source element's properties to be used within a synthetic multifield
     * @param source Previously rendered {@code Element} being converted to a synthetic multifield
     * @param source Current {@code Field} instance
     * @return {@code Element} representing the {@code source} node
     */
    private void wrapSingularField(Source source, Target target) {
        Target fieldSubresource = target.create(DialogConstants.NN_FIELD);
        // Move content to the new wrapper
        transferProperties(target, fieldSubresource, getTransferPolicies(source));
    }

    /**
     * Creates a {@code Target} instance containing a set of nodes that will be subsequently used within a synthetic
     * multifield
     * @param target Previously created {@code Target} that is now being converted to a synthetic multifield
     * @return {@code Target} representing the {@code SourceFacade} node
     */
    private void wrapFieldSet(Target target) {
        Target fieldSubresource = target.create(DialogConstants.NN_FIELD);
        // Get the existing "items" node and remove leading "./"-s from "name" attributes of particular items
        Target itemsSubresource = target.get(DialogConstants.NN_ITEMS);
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
                ImmutableMap.of(DialogConstants.ATTRIBUTE_PREFIX + DialogConstants.PN_NAME, PropertyTransferPolicy.MOVE));
    }

    /**
     * Creates a {@code source} node wrapping an existing {@code multifield} that will be used within a synthetic multifield
     * @param source Previously rendered {@code Element} being converted to a synthetic multifield
     * @param source Current {@code SourceFacade} instance
     * @return {@code Element} representing the {@code source} node
     */
    private void wrapNestedMultifield(Source source, Target target) {
        // Wee will create new "field" subresource but we need it "detached" not to mingle with existing "field" subresource
        Target fieldSubresource = new TargetImpl(DialogConstants.NN_FIELD, target);
        Target itemsSubresource = fieldSubresource.create(DialogConstants.NN_ITEMS);
        Target nestedMultifield = itemsSubresource.create(source.getName() + POSTFIX_NESTED);

        // Move existing multifield attributes to the nested multifield
        Map<String, PropertyTransferPolicy> standardPolicies = getTransferPolicies(source);
        Map<String, PropertyTransferPolicy> multifieldPolicies = new LinkedHashMap<>();
        multifieldPolicies.put(DialogConstants.ATTRIBUTE_PREFIX + DialogConstants.PN_COMPOSITE, PropertyTransferPolicy.COPY);
        multifieldPolicies.put(DialogConstants.RELATIVE_PATH_PREFIX + DialogConstants.NN_FIELD, PropertyTransferPolicy.MOVE);
        standardPolicies.forEach(multifieldPolicies::put);
        multifieldPolicies.put(DialogConstants.ATTRIBUTE_PREFIX + DialogConstants.PN_SLING_RESOURCE_TYPE, PropertyTransferPolicy.COPY);
        transferProperties(target, nestedMultifield, multifieldPolicies);

        // Set the "name" attribute of the "source" node of the current multifield
        // At the same time, alter the "name" attribute of the nested multifield to not get mixed with the name of the current one
        Target nestedMultifieldFieldSubresource = nestedMultifield.get(DialogConstants.NN_FIELD);
        String nestedMultifieldFieldName = StringUtils.defaultString(nestedMultifieldFieldSubresource.getAttributes().get(DialogConstants.PN_NAME));

        fieldSubresource.attribute(DialogConstants.PN_NAME, nestedMultifieldFieldName);
        nestedMultifieldFieldSubresource.getAttributes().put(DialogConstants.PN_NAME, nestedMultifieldFieldName + POSTFIX_NESTED);
        target.getChildren().add(fieldSubresource);
    }

    /**
     * Generates set of node transfer policies to properly distribute XML element requisites between the wrapper level
     * and the nested element level while converting a singular element to a multifield
     * @param source Current {@code SourceFacade} instance
     * @return {@code Map<String, XmlTransferPolicy>} instance
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
                    transferPolicies.put(DialogConstants.ATTRIBUTE_PREFIX + propertyName, PropertyTransferPolicy.SKIP);
                });
        // Also, all the values set via @Property will belong to the Multifield node
        Arrays.stream(source.adaptTo(Property[].class))
            .forEach(property -> transferPolicies.put(DialogConstants.ATTRIBUTE_PREFIX + property.name(), PropertyTransferPolicy.SKIP));
        // Need to override policy for "name" as has been stored in a loop above
        transferPolicies.put(DialogConstants.ATTRIBUTE_PREFIX + DialogConstants.PN_NAME, PropertyTransferPolicy.MOVE);
        // Some attribute values are expected to be moved or copied though have set to "skipped" above
        transferPolicies.put(DialogConstants.ATTRIBUTE_PREFIX + JcrConstants.PN_PRIMARY_TYPE, PropertyTransferPolicy.COPY);
        transferPolicies.put(DialogConstants.ATTRIBUTE_PREFIX + DialogConstants.PN_DISABLED, PropertyTransferPolicy.COPY);
        transferPolicies.put(DialogConstants.ATTRIBUTE_PREFIX + DialogConstants.PN_RENDER_HIDDEN, PropertyTransferPolicy.COPY);
        transferPolicies.put(DialogConstants.ATTRIBUTE_PREFIX + DialogConstants.PN_REQUIRED, PropertyTransferPolicy.MOVE);
        // Need to leave "granite:"-prefixed props (as they are probably set via @Attribute) at the multifield level
        transferPolicies.put(DialogConstants.ATTRIBUTE_PREFIX + PREFIX_GRANITE, PropertyTransferPolicy.MOVE);
        // Rest of element attributes will move to the inner source
        transferPolicies.put(DialogConstants.ATTRIBUTE_PREFIX + DialogConstants.WILDCARD, PropertyTransferPolicy.MOVE);
        // While all child nodes will stay as the property of multifield
        transferPolicies.put(DialogConstants.RELATIVE_PATH_PREFIX + DialogConstants.WILDCARD, PropertyTransferPolicy.SKIP);

        return transferPolicies;
    }

    /**
     * Migrates attributes and child nodes between {@code source} and {@code target}. Whether particular attributes
     * and child nodes are copied, moved or left alone, is defined by the {@code policies} map
     * @param from Element to serve as the source of migration
     * @param to Element to serve as the target of migration
     * @param policies Map containing attribute names (must start with {@code @}), child node names (must start with
     *                 {@code ./}) and the action appropriate to each of them, whether to copy element, move, or leave
     *                 intact. Wildcard symbol ({@code *}) is supported to specify common policy for multiple elements
     */
    private static void transferProperties(Target from, Target to, Map<String, PropertyTransferPolicy> policies) {
        // Process attributes
        List<String> removableAttributes = new ArrayList<>();
        for (String attribute : from.getAttributes().keySet()) {
            PropertyTransferPolicy policy = getPolicyForProperty(
                policies,
                DialogConstants.ATTRIBUTE_PREFIX + attribute);
            if (policy != PropertyTransferPolicy.SKIP) {
                to.attribute(attribute, from.getAttributes().get(attribute));
            }
            if (policy == PropertyTransferPolicy.MOVE) {
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
            if (policy == PropertyTransferPolicy.SKIP) {
                continue;
            }
            to.getChildren().add(child);
            if (policy == PropertyTransferPolicy.MOVE) {
                removableChildren.add(child);
            }
        }
        removableChildren.forEach(child -> from.getChildren().remove(child));
    }

    /**
     * Called to pick up an appropriate {@link PropertyTransferPolicy}
     * from the set of provided policies
     * @param policies {@code Map<String, XmlTransferPolicy>} describing available policies
     * @return The selected policy, or a default policy if no appropriate option found
     */
    private static PropertyTransferPolicy getPolicyForProperty(Map<String, PropertyTransferPolicy> policies, String propertyToken) {
        return policies.entrySet().stream()
            .filter(entry -> entry.getKey().endsWith(DialogConstants.WILDCARD)
                ? propertyToken.startsWith(StringUtils.stripEnd(entry.getKey(), DialogConstants.WILDCARD))
                : propertyToken.equals(entry.getKey()))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(PropertyTransferPolicy.SKIP);
    }


    private enum PropertyTransferPolicy {
        SKIP,
        COPY,
        MOVE
    }
}
