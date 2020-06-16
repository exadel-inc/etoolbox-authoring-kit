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

import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.handlers.MemberWrapper;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import java.util.function.BiConsumer;

/**
 * Handler for storing {@link DialogField} properties to a Granite UI widget XML node
 */
public class DialogFieldHandler implements Handler, BiConsumer<Element, MemberWrapper> {
    /**
     * Processes the user-defined data and writes it to XML entity
     * @param element XML element
     * @param memberWrapper Current {@code MemberWrapper} instance
     */
    @Override
    public void accept(Element element, MemberWrapper memberWrapper) {
        DialogField dialogField = PluginReflectionUtility.getMemberAnnotation(memberWrapper.getMember(), DialogField.class);
        if (dialogField == null) {
            return;
        }
        String name = memberWrapper.getMember().getName();
        if (StringUtils.isNotBlank(dialogField.name())) {
            name = !DialogConstants.PATH_SEPARATOR.equals(dialogField.name()) && !DialogConstants.RELATIVE_PATH_PREFIX.equals(dialogField.name())
                    ? PluginRuntime.context().getXmlUtility().getValidFieldName(dialogField.name())
                    : DialogConstants.RELATIVE_PATH_PREFIX;
        }
        String prefix = (String) memberWrapper.getValue(DialogConstants.PN_PREFIX);
        if (StringUtils.isNotBlank(prefix)
                && !(prefix.equals(DialogConstants.RELATIVE_PATH_PREFIX) && name.equals(DialogConstants.RELATIVE_PATH_PREFIX))
                && !(prefix.equals(DialogConstants.RELATIVE_PATH_PREFIX) && name.startsWith(DialogConstants.PARENT_PATH_PREFIX))) {
            name = prefix + name;
        }
        name = name + memberWrapper.getValue(DialogConstants.PN_POSTFIX);
        element.setAttribute(DialogConstants.PN_NAME, name);
    }
}
