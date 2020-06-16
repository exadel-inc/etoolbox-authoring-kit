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

import com.exadel.aem.toolkit.api.annotations.widgets.radio.RadioButton;
import com.exadel.aem.toolkit.api.annotations.widgets.radio.RadioGroup;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.api.handlers.MemberWrapper;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;
import org.apache.commons.lang3.ArrayUtils;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.function.BiConsumer;

/**
 * {@link Handler} implementation used to create markup responsible for {@code RadioGroup} widget functionality
 * within the {@code cq:dialog} XML node
 */
class RadioGroupHandler implements Handler, BiConsumer<Element, MemberWrapper> {
    /**
     * Processes the user-defined data and writes it to XML entity
     * @param element Current XML element
     * @param memberWrapper Current {@code MemberWrapper} instance
     */
    @Override
    @SuppressWarnings({"deprecation", "squid:S1874"}) // .acsListPath() and .acsListResourceType() method calls left for backward compatibility
    public void accept(Element element, MemberWrapper memberWrapper) {
        RadioGroup radioGroup = PluginReflectionUtility.getMemberAnnotation(memberWrapper.getMember(), RadioGroup.class);
        if (radioGroup == null) {
            return;
        }
        if (ArrayUtils.isNotEmpty(radioGroup.buttons())) {
            Element items = (Element) element.appendChild(getXmlUtil().createNodeElement(DialogConstants.NN_ITEMS));
            Arrays.stream(radioGroup.buttons()).forEach(button -> renderButton(button, items));
        }
        getXmlUtil().appendDataSource(element, radioGroup.datasource(), radioGroup.acsListPath(), radioGroup.acsListResourceType());
    }

    private void renderButton(RadioButton buttonInstance, Element parentElement) {
        Element xmlItem = getXmlUtil().createNodeElement(getXmlUtil().getUniqueName(buttonInstance.value(),
                DialogConstants.NN_ITEM,
                parentElement));
        parentElement.appendChild(xmlItem);
        getXmlUtil().mapProperties(xmlItem, buttonInstance);
    }
}
