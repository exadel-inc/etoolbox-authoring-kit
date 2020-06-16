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

import com.exadel.aem.toolkit.api.annotations.widgets.Password;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.api.handlers.MemberWrapper;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;
import org.w3c.dom.Element;

import java.util.function.BiConsumer;

/**
 * {@link Handler} implementation used to create markup responsible for {@code Password} widget functionality
 * within the {@code cq:dialog} XML node
 */
class PasswordHandler implements Handler, BiConsumer<Element, MemberWrapper> {
    /**
     * Processes the user-defined data and writes it to XML entity
     * @param element Current XML element
     * @param memberWrapper Current {@code MemberWrapper} instance
     */
    @Override
    public void accept(Element element, MemberWrapper memberWrapper) {
        Password password = PluginReflectionUtility.getMemberAnnotation(memberWrapper.getMember(), Password.class);
        if (password == null) {
            return;
        }
        if(!password.retype().isEmpty()){
            element.setAttribute(DialogConstants.PN_RETYPE,
                    memberWrapper.getValue(DialogConstants.PN_PREFIX) + getXmlUtil().getValidFieldName(password.retype()) + memberWrapper.getValue(DialogConstants.PN_POSTFIX));
        }
    }
}
