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
import com.exadel.aem.toolkit.api.handlers.SourceFacade;
import com.exadel.aem.toolkit.core.exceptions.InvalidFieldContainerException;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.exadel.aem.toolkit.core.util.PluginXmlContainerUtility;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * {@link Handler} implementation used to create markup responsible for Granite {@code FieldSet} widget functionality
 * within the {@code cq:dialog} XML node
 */
class FieldSetHandler implements WidgetSetHandler {
    private static final String EMPTY_FIELDSET_EXCEPTION_MESSAGE = "No valid fields found in fieldset class ";

    /**
     * Processes the user-defined data and writes it to XML entity
     * @param sourceFacade Current {@code SourceFacade} instance
     * @param element Current XML element
     */
    @Override
    @SuppressWarnings({"deprecation", "squid:S1874"})
    public void accept(SourceFacade sourceFacade, Element element) {
        // Define the working @FieldSet annotation instance and the fieldset type
        FieldSet fieldSet = sourceFacade.adaptTo(FieldSet.class);
        Class<?> fieldSetType = getFieldSetType(sourceFacade.getSource());

        List<SourceFacade> sourceFacades = getContainerSourceFacades(element, sourceFacade, fieldSetType);

        if(sourceFacades.isEmpty()) {
            PluginRuntime.context().getExceptionHandler().handle(new InvalidFieldContainerException(
                    EMPTY_FIELDSET_EXCEPTION_MESSAGE + fieldSetType.getName()
            ));
            return;
        }

        sourceFacades.forEach(populated -> populatePrefixPostfix(populated, sourceFacade, fieldSet));

        // append the valid sourceFacades to the container
        PluginXmlContainerUtility.append(element, sourceFacades);
    }

    private void populatePrefixPostfix(SourceFacade populated, SourceFacade current, FieldSet fieldSet) {
        if (StringUtils.isNotBlank(fieldSet.namePrefix())){
            populated.addToValueMap(DialogConstants.PN_PREFIX, current.fromValueMap(DialogConstants.PN_PREFIX).toString() +
                    populated.fromValueMap(DialogConstants.PN_PREFIX).toString().substring(2) + getXmlUtil().getValidFieldName(fieldSet.namePrefix()));
        }
        if (StringUtils.isNotBlank(fieldSet.namePostfix())) {
            populated.addToValueMap(DialogConstants.PN_POSTFIX, fieldSet.namePostfix() + populated.fromValueMap(DialogConstants.PN_POSTFIX) +
                    current.fromValueMap(DialogConstants.PN_POSTFIX).toString());
        }
    }

    private Class<?> getFieldSetType(Object member) {
        return member instanceof Field
                ? ((Field) member).getType()
                : ((Method) member).getReturnType();
    }
}