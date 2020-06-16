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
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.api.handlers.MemberWrapper;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.w3c.dom.Element;

import java.lang.reflect.Member;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * {@link Handler} implementation used to create markup responsible for Granite UI {@code Checkbox} widget functionality
 * within the {@code cq:dialog} XML node
 */
class CheckboxHandler implements Handler, BiConsumer<Element, MemberWrapper> {
    private static final String POSTFIX_FOR_ROOT_CHECKBOX = "Checkbox";

    /**
     * Processes the user-defined data and writes it to XML entity
     * @param element Current XML element
     * @param memberWrapper Current {@code MemberWrapper} instance
     */
    @Override
    public void accept(Element element, MemberWrapper memberWrapper) {
        Checkbox checkbox = PluginReflectionUtility.getMemberAnnotation(memberWrapper.getMember(), Checkbox.class);
        assert checkbox != null;
        if (checkbox.sublist()[0] == Object.class) {
            getXmlUtil().mapProperties(element, checkbox);
            setTextAttribute(element, memberWrapper.getMember());
        } else {
            element.setAttribute(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, ResourceTypes.NESTED_CHECKBOX_LIST);
            Element itemsElement = getXmlUtil().createNodeElement(DialogConstants.NN_ITEMS);
            element.appendChild(itemsElement);

            Element checkboxElement = getXmlUtil().createNodeElement(memberWrapper.getMember().getName() + POSTFIX_FOR_ROOT_CHECKBOX, ResourceTypes.CHECKBOX);
            getXmlUtil().mapProperties(checkboxElement, checkbox);
            itemsElement.appendChild(checkboxElement);

            appendNestedCheckBoxList(checkboxElement, memberWrapper.getMember());
        }
    }

    /**
     * Creates and appends markup correspondent to a Granite UI nested {@code Checkbox} structure
     * @param element {@code Element} instance representing current XML node
     * @param member Current {@code Member} of a component class
     */
    private void appendNestedCheckBoxList(Element element, Member member) {
        Element sublist = getXmlUtil().createNodeElement(DialogConstants.NN_SUBLIST, ResourceTypes.NESTED_CHECKBOX_LIST);
        Checkbox checkbox = PluginReflectionUtility.getMemberAnnotation(member, Checkbox.class);
        assert checkbox != null;
        getXmlUtil().setAttribute(sublist, DialogConstants.PN_DISCONNECTED, checkbox.disconnectedSublist());
        element.appendChild(sublist);

        Element itemsElement = getXmlUtil().createNodeElement(DialogConstants.NN_ITEMS);
        sublist.appendChild(itemsElement);

        appendCheckbox(itemsElement, member);
    }

    /**
     * Creates and appends single Granite UI {@code Checkbox} markup to the current XML node
     * @param element {@code Element} instance representing current XML node
     * @param member Current {@code Member} of a component class
     */
    private void appendCheckbox(Element element, Member member) {
        Checkbox checkbox = PluginReflectionUtility.getMemberAnnotation(member, Checkbox.class);
        if (checkbox == null) {
            return;
        }
        for (Class<?> sublistClass : checkbox.sublist()) {
            List<Member> members = PluginReflectionUtility.getAllMembers(sublistClass);
            members = members.stream().filter(f -> PluginReflectionUtility.getMemberAnnotation(f, Checkbox.class) != null).collect(Collectors.toList());

            for (Member inner : members) {
                Element checkboxElement = getXmlUtil().createNodeElement(inner.getName(), ResourceTypes.CHECKBOX);
                Checkbox innerCheckbox = PluginReflectionUtility.getMemberAnnotation(inner, Checkbox.class);
                if (innerCheckbox == null) {
                    continue;
                }
                getXmlUtil().mapProperties(checkboxElement, innerCheckbox);
                setTextAttribute(checkboxElement, inner);

                element.appendChild(checkboxElement);

                if (innerCheckbox.sublist()[0] != Object.class) {
                    appendNestedCheckBoxList(checkboxElement, inner);
                }
            }
        }
    }

    /**
     * Decides which property of the current field to use as the {@code text} attribute of checkbox node and populates it
     * @param element {@code Element} instance representing current XML node
     * @param member Current {@code Member} of a component class
     */
    private void setTextAttribute(Element element, Member member) {
        Checkbox checkbox = PluginReflectionUtility.getMemberAnnotation(member, Checkbox.class);
        DialogField dialogField = PluginReflectionUtility.getMemberAnnotation(member, DialogField.class);
        assert checkbox != null;
        if (checkbox.text().isEmpty() && dialogField != null) {
            element.setAttribute(DialogConstants.PN_TEXT, dialogField.label());
        } else if (checkbox.text().isEmpty()) {
            element.setAttribute(DialogConstants.PN_TEXT, member.getName());
        }
    }
}
