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

import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;
import com.exadel.aem.toolkit.core.exceptions.InvalidSettingException;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.api.handlers.MemberWrapper;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import java.util.List;

/**
 * {@link Handler} implementation used to create markup responsible for Granite {@code FieldSet} widget functionality
 * within the {@code cq:dialog} XML node
 */
class FieldSetHandler implements WidgetSetHandler {
    private static final String EMPTY_FIELDSET_EXCEPTION_MESSAGE = "No valid fields found in fieldset class ";

    /**
     * Processes the user-defined data and writes it to XML entity
     * @param element Current XML element
     * @param memberWrapper Current {@code Field} instance
     */
    @Override
    public void accept(Element element, MemberWrapper memberWrapper) {
        // Define the working @FieldSet annotation instance and the fieldset type
        FieldSet fieldSet = PluginReflectionUtility.getMemberAnnotation(memberWrapper.getMember(), FieldSet.class);
        Class<?> fieldSetType = PluginReflectionUtility.getMemberType(memberWrapper.getMember());

        assert fieldSet != null;
        assert fieldSetType != null;

        // Get the filtered fields collection for the current container; early return if collection is empty
        List<MemberWrapper> members = getContainerMembers(element, memberWrapper.getMember(), fieldSetType);
        if(members.isEmpty()) {
            PluginRuntime.context().getExceptionHandler().handle(new InvalidSettingException(
                    EMPTY_FIELDSET_EXCEPTION_MESSAGE + fieldSetType.getName()
            ));
            return;
        }

        members.forEach(populated -> populatePrefixPostfix(populated, memberWrapper, fieldSet));

        // append the valid fields to the container
        Handler.appendToContainer(element, members);
    }

    private void populatePrefixPostfix(MemberWrapper populated, MemberWrapper current, FieldSet fieldSet) {
        if (StringUtils.isNotBlank(fieldSet.namePrefix())){
            populated.addValue(DialogConstants.PN_PREFIX, current.getValue(DialogConstants.PN_PREFIX).toString() +
                    populated.getValue(DialogConstants.PN_PREFIX).toString().substring(2) + getXmlUtil().getValidFieldName(fieldSet.namePrefix()));
        }
        if (StringUtils.isNotBlank(fieldSet.namePostfix())) {
            populated.addValue(DialogConstants.PN_POSTFIX, fieldSet.namePostfix() + populated.getValue(DialogConstants.PN_POSTFIX) +
                    current.getValue(DialogConstants.PN_POSTFIX).toString());
        }
    }
}