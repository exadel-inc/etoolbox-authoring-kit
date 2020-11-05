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

import java.util.function.BiConsumer;

import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;

import com.exadel.aem.toolkit.api.annotations.widgets.accessory.Multiple;
import com.exadel.aem.toolkit.core.handlers.Handler;

/**
 * Handler for creating ad-hoc {@code Multifield}s for {@link Multiple}-marked dialog fields
 */
public class MultipleHandler implements Handler, BiConsumer<Source, Target> {
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
    }/*

        // Modify the existing XML DOM targetFacade with new subnode(-s) and attributes
        Element fieldElement;
        boolean isComposite = false;

        if (isFieldSet(targetFacade)) {
            fieldElement = getFieldSetWrapper(targetFacade);
            isComposite = true;

        } else if (isMultifield(targetFacade)) {
            fieldElement = getNestedMultifieldWrapper(targetFacade, source);
            isComposite = true;

        } else {
            fieldElement = getSimpleWrapper(targetFacade, source);
        }

        // Facilitate the modified targetFacade to work as Multifield
        if (isComposite) {
            getXmlUtil().setAttribute(fieldElement, DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.CONTAINER);
            getXmlUtil().setAttribute(targetFacade, DialogConstants.PN_COMPOSITE, true);
        }
        targetFacade.appendChild(fieldElement);
        targetFacade.removeAttribute(DialogConstants.PN_NAME);
        targetFacade.setAttribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.MULTIFIELD);

        // Since this targetFacade has just "emerged" as a multifield, we need to check if there are multifield bound
        // (custom) handlers and run such one more time
        PluginRuntime.context().getReflectionUtility().getCustomDialogWidgetHandlers(Collections.singletonList(MultiField.class))
                .forEach(handler -> handler.accept(source, targetFacade));
    }

    *//**
     * Gets whether the currently rendered XML element is a Granite {@code Multifield} element
     * @param element XML element
     * @return True or false
     *//*
    private boolean isMultifield(TargetFacade element) {
        return StringUtils.equals(element.getAttribute(DialogConstants.PN_SLING_RESOURCE_TYPE, String.class), ResourceTypes.MULTIFIELD)
                && element.getChild(DialogConstants.NN_FIELD) != null;
    }

    *//**
     * Gets whether the currently rendered XML targetFacade is a {@code FieldSet} kind of elements
     * @param targetFacade XML targetFacade
     * @return True or false
     *//*
    private boolean isFieldSet(TargetFacade targetFacade) {
        Element itemsElement = getXmlUtil().getChildElement(targetFacade, DialogConstants.NN_ITEMS);
        return itemsElement != null && itemsElement.hasChildNodes();
    }

    *//**
     * Creates a {@code source} node encapsulating source element's properties to be used within a synthetic multifield
     * @param source Previously rendered {@code Element} being converted to a synthetic multifield
     * @param source Current {@code Field} instance
     * @return {@code Element} representing the {@code source} node
     *//*
    private Element getSimpleWrapper(Element source, SourceFacade source) {
        Element result = getXmlUtil().createNodeElement(DialogConstants.NN_FIELD);
        // Move content to the new wrapper
        getXmlUtil().transfer(source, result, getTransferPolicies(source));
        return result;
    }

    *//**
     * Creates a {@code SourceFacade} node wrapping a set of fields that will be subsequently used within a synthetic multifield
     * @param targetFacade Previously rendered {@code Element} being converted to a synthetic multifield
     * @return {@code Element} representing the {@code SourceFacade} node
     *//*
    private Element getFieldSetWrapper(TargetFacade targetFacade) {
        Element result = getXmlUtil().createNodeElement(DialogConstants.NN_FIELD);
        // Get the existing "items" node and remove leading "./"-s from "name" attributes of particular items
        Element existingItems = getXmlUtil().getChildElement(targetFacade, DialogConstants.NN_ITEMS);
        IntStream.range(0, existingItems.getChildNodes().getLength())
                .mapToObj(pos -> (Element) existingItems.getChildNodes().item(pos))
                .forEach(element -> {
                    String name = element.getAttribute(DialogConstants.PN_NAME);
                    if (StringUtils.isNotEmpty(name)) {
                        element.setAttribute(DialogConstants.PN_NAME, StringUtils.removeStart(name, DialogConstants.RELATIVE_PATH_PREFIX));
                    }
                });
        // Append the existing "items" node to the "field" node
        result.appendChild(existingItems);
        // Move "name" property of the targetFacade node to the "field" node
        getXmlUtil().transfer(
                targetFacade,
                result,
                ImmutableMap.of(DialogConstants.ATTRIBUTE_PREFIX + DialogConstants.PN_NAME, XmlTransferPolicy.MOVE));
        return result;
    }

    *//**
     * Creates a {@code source} node wrapping an existing {@code multifield} that will be used within a synthetic multifield
     * @param source Previously rendered {@code Element} being converted to a synthetic multifield
     * @param source Current {@code SourceFacade} instance
     * @return {@code Element} representing the {@code source} node
     *//*
    private Element getNestedMultifieldWrapper(Element source, SourceFacade source) {
        Element result = getXmlUtil().createNodeElement(DialogConstants.NN_FIELD);
        // Create nested "items > multifield > source > items" node structure
        Element nestedItems = getXmlUtil().createNodeElement(DialogConstants.NN_ITEMS);
        Element nestedMultifield = getXmlUtil().createNodeElement(source.getTagName() + POSTFIX_NESTED);
        nestedItems.appendChild(nestedMultifield);
        result.appendChild(nestedItems);

        // Move existing multifield attributes to the nested multifield
        Map<String, XmlTransferPolicy> standardPolicies = getTransferPolicies(source);
        Map<String, XmlTransferPolicy> multifieldPolicies = new LinkedHashMap<>();
        multifieldPolicies.put(DialogConstants.ATTRIBUTE_PREFIX + DialogConstants.PN_COMPOSITE, XmlTransferPolicy.COPY);
        multifieldPolicies.put(DialogConstants.RELATIVE_PATH_PREFIX + DialogConstants.NN_FIELD, XmlTransferPolicy.MOVE);
        standardPolicies.forEach(multifieldPolicies::put);
        multifieldPolicies.put(DialogConstants.ATTRIBUTE_PREFIX + DialogConstants.PN_SLING_RESOURCE_TYPE, XmlTransferPolicy.COPY);
        getXmlUtil().transfer(source, nestedMultifield, multifieldPolicies);

        // Set the "name" attribute of the "source" node of the current multifield
        // At the same time, alter the "name" attribute of the nested multifield to not get mixed with the name of the current one
        Element nestedMultifieldField = getXmlUtil().getChildElement(nestedMultifield, DialogConstants.NN_FIELD);
        String nestedMultifieldFieldName = StringUtils.defaultString(nestedMultifieldField.getAttribute(DialogConstants.PN_NAME));
        getXmlUtil().setAttribute(result, DialogConstants.PN_NAME, nestedMultifieldFieldName);
        getXmlUtil().setAttribute(nestedMultifieldField, DialogConstants.PN_NAME, nestedMultifieldFieldName + POSTFIX_NESTED);

        return result;
    }

    *//**
     * Generates set of node transfer policies to properly distribute XML element requisites between the wrapper level
     * and the nested element level while converting a singular element to a multifield
     * @param source Current {@code SourceFacade} instance
     * @return {@code Map<String, XmlTransferPolicy>} instance
     *//*
    private static Map<String, XmlTransferPolicy> getTransferPolicies(SourceFacade source) {
        Map<String, XmlTransferPolicy> transferPolicies = new LinkedHashMap<>();

        // All "standard" @DialogField props will belong to the Multifield node unless in the cases specified below,
        // while "widget-specific" properties will move to the "inner" node
        Arrays.stream(DialogField.class.getDeclaredMethods())
                .forEach(method -> {
                    String propertyName = method.getAnnotation(PropertyRendering.class) != null
                            ? StringUtils.defaultIfEmpty(method.getAnnotation(PropertyRendering.class).name(), method.getName())
                            : method.getName();
                    transferPolicies.put(DialogConstants.ATTRIBUTE_PREFIX + propertyName, XmlTransferPolicy.SKIP);
                });
        // Also, all the values set via @Property will belong to the Multifield node
        Arrays.stream(source.adaptTo(Property[].class))
                .forEach(property -> transferPolicies.put(DialogConstants.ATTRIBUTE_PREFIX + property.name(), XmlTransferPolicy.SKIP));
        // Need to override policy for "name" as has been stored in a loop above
        transferPolicies.put(DialogConstants.ATTRIBUTE_PREFIX + DialogConstants.PN_NAME, XmlTransferPolicy.MOVE);
        // Some attribute values are expected to be moved or copied though set to "skipped" above
        transferPolicies.put(DialogConstants.ATTRIBUTE_PREFIX + JcrConstants.PN_PRIMARY_TYPE, XmlTransferPolicy.COPY);
        transferPolicies.put(DialogConstants.ATTRIBUTE_PREFIX + DialogConstants.PN_DISABLED, XmlTransferPolicy.COPY);
        transferPolicies.put(DialogConstants.ATTRIBUTE_PREFIX + DialogConstants.PN_RENDER_HIDDEN, XmlTransferPolicy.COPY);
        transferPolicies.put(DialogConstants.ATTRIBUTE_PREFIX + DialogConstants.PN_REQUIRED, XmlTransferPolicy.MOVE);
        // Need to leave "granite:"-prefixed props (as they are probably set via @Attribute) at the multifield level
        transferPolicies.put(DialogConstants.ATTRIBUTE_PREFIX + PREFIX_GRANITE, XmlTransferPolicy.MOVE);
        // Rest of element attributes will move to the inner source
        transferPolicies.put(DialogConstants.ATTRIBUTE_PREFIX + DialogConstants.WILDCARD, XmlTransferPolicy.MOVE);
        // While all child nodes will stay as the property of multifield
        transferPolicies.put(DialogConstants.RELATIVE_PATH_PREFIX + DialogConstants.WILDCARD, XmlTransferPolicy.SKIP);
        return transferPolicies;
    }*/
}
