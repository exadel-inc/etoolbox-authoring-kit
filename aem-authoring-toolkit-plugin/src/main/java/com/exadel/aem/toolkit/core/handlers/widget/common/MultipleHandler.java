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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.w3c.dom.Element;
import com.google.common.collect.ImmutableMap;

import com.exadel.aem.toolkit.api.annotations.main.JcrConstants;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.Multiple;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.exadel.aem.toolkit.core.util.XmlTransferPolicy;

/**
 * Handler for creating ad-hoc {@code Multifield}s for {@link Multiple}-marked dialog fields
 */
public class MultipleHandler implements Handler, BiConsumer<Element, Field> {
    private static final String PREFIX_GRANITE = "granite:*";
    private static final String POSTFIX_NESTED = "_nested";

    @Override
    public void accept(Element element, Field field) {
        if (!field.isAnnotationPresent(Multiple.class)) {
            return;
        }

        Element fieldElement;
        boolean isComposite = false;

        if (isFieldSet(element)) {
            fieldElement = getFieldSetWrapper(element);
            isComposite = true;

        } else if (isMultifield(element)) {
            fieldElement = getNestedMutifieldWrapper(element);
            isComposite = true;

        } else {
            fieldElement = getSimpleWrapper(element);
        }

        // Facilitate the newly created element to work as a multifield
        if (isComposite) {
            getXmlUtil().setAttribute(fieldElement, JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, ResourceTypes.CONTAINER);
            getXmlUtil().setAttribute(element, DialogConstants.PN_COMPOSITE, true);
        }
        element.appendChild(fieldElement);
        element.removeAttribute(DialogConstants.PN_NAME);
        element.setAttribute(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, ResourceTypes.MULTIFIELD);

        // Since this element has just "emerged" as a multifield, we need to check if there are multifield bound
        // (custom) handlers and run such one more time
        PluginRuntime.context().getReflectionUtility().getCustomDialogWidgetHandlers(Collections.singletonList(MultiField.class))
                .forEach(handler -> handler.accept(element, field));
    }

    /**
     * Gets whether the currently rendered XML element is a Granite {@code Multifield} element
     * @param element {@code Element} to analyze
     * @return True or false
     */
    private boolean isMultifield(Element element) {
        return StringUtils.equals(element.getAttribute(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY), ResourceTypes.MULTIFIELD)
                && getXmlUtil().getChildElement(element, DialogConstants.NN_FIELD) != null;
    }

    /**
     * Gets whether the currently rendered XML element is a {@code FieldSet} kind of elements
     * @param element {@code Element} to analyze
     * @return True or false
     */
    private boolean isFieldSet(Element element) {
        Element itemsElement = getXmlUtil().getChildElement(element, DialogConstants.NN_ITEMS);
        return itemsElement != null && itemsElement.hasChildNodes();
    }

    /**
     * Creates a {@code field} node encapsulating source element's properties to be used within a synthetic multifield
     * @param source Previously rendered {@code Element} being converted to a synthetic multifield
     * @return {@code Element} representing the {@code field} node
     */
    private Element getSimpleWrapper(Element source) {
        Element result = getXmlUtil().createNodeElement(DialogConstants.NN_FIELD);
        // Move content to the new wrapper
        getXmlUtil().transfer(source, result, getStandardTransferPolicies());
        return result;
    }

    /**
     * Creates a {@code field} node wrapping a set of fields that will be subsequently used within a synthetic multifield
     * @param source Previously rendered {@code Element} being converted to a synthetic multifield
     * @return {@code Element} representing the {@code field} node
     */
    private Element getFieldSetWrapper(Element source) {
        Element result = getXmlUtil().createNodeElement(DialogConstants.NN_FIELD);
        // Append the existing "items" node to the "field" node
        result.appendChild(getXmlUtil().getChildElement(source, DialogConstants.NN_ITEMS));
        // Move only "name" property to the "field" node
        getXmlUtil().transfer(
                source,
                result,
                ImmutableMap.of(DialogConstants.ATTRIBUTE_PREFIX + DialogConstants.PN_NAME, XmlTransferPolicy.MOVE));
        return result;
    }

    /**
     * Creates a {@code field} node wrapping an existing {@code multifield} that will be used within a synthetic multifield
     * @param source Previously rendered {@code Element} being converted to a synthetic multifield
     * @return {@code Element} representing the {@code field} node
     */
    private Element getNestedMutifieldWrapper(Element source) {
        Element result = getXmlUtil().createNodeElement(DialogConstants.NN_FIELD);
        // Create nested "items > multifield > field > items" node structure
        Element nestedItems = getXmlUtil().createNodeElement(DialogConstants.NN_ITEMS);
        Element nestedMultifield = getXmlUtil().createNodeElement(source.getTagName() + POSTFIX_NESTED);
        nestedItems.appendChild(nestedMultifield);
        result.appendChild(nestedItems);

        // Move existing multifield attributes to the nested multifield
        Map<String, XmlTransferPolicy> multifieldPolicies = invert(getStandardTransferPolicies());
        multifieldPolicies.put(DialogConstants.ATTRIBUTE_PREFIX + JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, XmlTransferPolicy.COPY);
        getXmlUtil().transfer(source, nestedMultifield, multifieldPolicies);

        // Set the "name" attribute of the "field" node of the current multifield
        // At the same time, alter the "name" attribute of the nested multifield to not get mixed with the name of the current one
        Element nestedMultifieldField = getXmlUtil().getChildElement(nestedMultifield, DialogConstants.NN_FIELD);
        String nestedMultifieldFieldName = StringUtils.defaultString(nestedMultifieldField.getAttribute(DialogConstants.PN_NAME));
        getXmlUtil().setAttribute(result, DialogConstants.PN_NAME, nestedMultifieldFieldName);
        getXmlUtil().setAttribute(nestedMultifieldField, DialogConstants.PN_NAME, nestedMultifieldFieldName + POSTFIX_NESTED);

        return result;
    }

    /**
     * Generates the stereotype set of transfer policies to convert the current element into a multifield
     * @return {@code Map<String, XmlTransferPolicy>} instance
     */
    private static Map<String, XmlTransferPolicy> getStandardTransferPolicies() {
        Map<String, XmlTransferPolicy> transferPolicies = new LinkedHashMap<>();

        // All "standard" @DialogField props will belong to the Multifield node unless specified below, that is, stay where they are
        // while "widget-specific" properties will move to the "inner" node
        Arrays.stream(DialogField.class.getDeclaredMethods())
                .forEach(method -> {
                    String propertyName = method.getAnnotation(PropertyRendering.class) != null
                            ? StringUtils.defaultIfEmpty(method.getAnnotation(PropertyRendering.class).name(), method.getName())
                            : method.getName();
                    transferPolicies.put(DialogConstants.ATTRIBUTE_PREFIX + propertyName, XmlTransferPolicy.SKIP);
                });
        // Some attribute values are expected to be copied
        transferPolicies.put(DialogConstants.ATTRIBUTE_PREFIX + JcrConstants.PN_PRIMARY_TYPE, XmlTransferPolicy.COPY);
        transferPolicies.put(DialogConstants.ATTRIBUTE_PREFIX + DialogConstants.PN_DISABLED, XmlTransferPolicy.COPY);
        transferPolicies.put(DialogConstants.ATTRIBUTE_PREFIX + DialogConstants.PN_REQUIRED, XmlTransferPolicy.COPY);
        transferPolicies.put(DialogConstants.ATTRIBUTE_PREFIX + DialogConstants.PN_RENDER_HIDDEN, XmlTransferPolicy.COPY);
        transferPolicies.put(DialogConstants.ATTRIBUTE_PREFIX + JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, XmlTransferPolicy.MOVE);
        // This one is specially for multifield nodes copying
        transferPolicies.put(DialogConstants.ATTRIBUTE_PREFIX + DialogConstants.PN_COMPOSITE, XmlTransferPolicy.COPY);
        // Need to override policy for "name" as has been stored in a loop above
        transferPolicies.put(DialogConstants.ATTRIBUTE_PREFIX + DialogConstants.PN_NAME, XmlTransferPolicy.MOVE);
        // Need to leave "granite:"-prefixed props (as they are probably set via @Attribute) at the multifield level
        transferPolicies.put(DialogConstants.ATTRIBUTE_PREFIX + PREFIX_GRANITE, XmlTransferPolicy.MOVE);
        // Rest of element attributes will move to the inner field
        transferPolicies.put(DialogConstants.ATTRIBUTE_PREFIX + DialogConstants.WILDCARD, XmlTransferPolicy.MOVE);
        // While all child nodes will stay as the property of multifield
        transferPolicies.put(DialogConstants.RELATIVE_PATH_PREFIX + DialogConstants.WILDCARD, XmlTransferPolicy.SKIP);
        return transferPolicies;
    }

    /**
     * Inverts the provided map of node-to-{@link XmlTransferPolicy} values so that an element to move becomes the one
     * to skip, a skippable element becomes the one to move, while the copyable elements stay the same
     * @param transferPolicies {@code Map<String, XmlTransferPolicy>} representing currently defined policies
     * @return {@code Map<String, XmlTransferPolicy>} representing inverted policies
     */
    private static Map<String, XmlTransferPolicy> invert(Map<String, XmlTransferPolicy> transferPolicies) {
        Map<String, XmlTransferPolicy> result = new LinkedHashMap<>();
        UnaryOperator<XmlTransferPolicy> invertPolicy = policy -> policy == XmlTransferPolicy.SKIP ? XmlTransferPolicy.MOVE : XmlTransferPolicy.SKIP;
        transferPolicies.forEach((key, policy) -> {
            XmlTransferPolicy changedPolicy = (policy == XmlTransferPolicy.COPY) ? policy : invertPolicy.apply(policy);
            result.put(key, changedPolicy);
        });
        return result;
    }
}
