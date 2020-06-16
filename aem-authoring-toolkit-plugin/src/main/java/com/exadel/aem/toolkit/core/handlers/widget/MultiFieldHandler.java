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

import java.util.List;

import com.exadel.aem.toolkit.api.handlers.MemberWrapper;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.w3c.dom.Element;
import com.google.common.collect.ImmutableMap;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.widgets.MultiField;
import com.exadel.aem.toolkit.core.exceptions.InvalidSettingException;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.DialogConstants;

/**
 * {@link Handler} implementation used to create markup responsible for Granite UI {@code Multifield} widget functionality
 * within the {@code cq:dialog} XML node
 */
public class MultiFieldHandler implements WidgetSetHandler {
    private static final String EMPTY_MULTIFIELD_EXCEPTION_MESSAGE = "No valid fields found in multifield class ";

    /**
     * Processes the user-defined data and writes it to XML entity
     * @param element Current XML element
     * @param memberWrapper Current {@code MemberWrapper} instance
     */
    @Override
    public void accept(Element element, MemberWrapper memberWrapper) {
        // Define the working @Multifield annotation instance and the multifield type
        MultiField multiField = PluginReflectionUtility.getMemberAnnotation(memberWrapper.getMember(), MultiField.class);
        if (multiField == null) {
            return;
        }
        Class<?> multifieldType = multiField.field();

        // Modify the element's attributes for multifield mode
        String name = element.getAttribute(DialogConstants.PN_NAME);
        element.removeAttribute(DialogConstants.PN_NAME);

        // Get the filtered fields collection for the current container; early return if collection is empty
        List<MemberWrapper> containerMembers = getContainerMembers(element, memberWrapper.getMember(), multifieldType);
        if (containerMembers.isEmpty()) {
            PluginRuntime.context().getExceptionHandler().handle(new InvalidSettingException(
                    EMPTY_MULTIFIELD_EXCEPTION_MESSAGE + multifieldType.getName()
            ));
            return;
        }

        // Render separately the multiple-field and the single-field modes of multifield
        if (containerMembers.size() > 1) {
            render(element, name, containerMembers);
        } else {
            render(element, containerMembers.get(0));
        }
    }

    /**
     * Renders multiple widgets as XML nodes within the current multifield container
     * @param element Current XML element
     * @param name The element's {@code name} attribute
     * @param memberWrappers The collection of {@code MemberWrapper} instances to render TouchUI widgets from
     */
    private void render(Element element, String name, List<MemberWrapper> memberWrappers) {
        getXmlUtil().setAttribute(element, DialogConstants.PN_COMPOSITE, true);
        Element multifieldContainerElement = PluginRuntime.context().getXmlUtility().createNodeElement(
                DialogConstants.NN_FIELD,
                ImmutableMap.of(
                        DialogConstants.PN_NAME, name,
                        JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, ResourceTypes.CONTAINER
                ));
        element.appendChild(multifieldContainerElement);
        memberWrappers.forEach(member -> {
            String prefix = (String) member.getValue(DialogConstants.PN_PREFIX);
            if (prefix.startsWith(DialogConstants.RELATIVE_PATH_PREFIX)) {
                member.addValue(DialogConstants.PN_PREFIX, prefix.substring(2));
            }
        });
        Handler.appendToContainer(multifieldContainerElement, memberWrappers);
    }

    /**
     * Renders a single widget within the current multifield container
     * @param element Current XML element
     * @param memberWrapper The {@code MemberWrapper} instance to render TouchUI widget from
     */
    private void render(Element element, MemberWrapper memberWrapper) {
        DialogWidget widget = DialogWidgets.fromMember(memberWrapper.getMember());
        if (widget == null) {
            return;
        }
        widget.appendTo(element, memberWrapper, DialogConstants.NN_FIELD);
    }
}
