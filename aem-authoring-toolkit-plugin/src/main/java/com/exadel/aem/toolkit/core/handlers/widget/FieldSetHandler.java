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

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;
import com.exadel.aem.toolkit.api.annotations.widgets.IgnoreField;
import com.exadel.aem.toolkit.core.exceptions.InvalidSettingException;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;

/**
 * {@link Handler} implementation used to create markup responsible for Granite {@code FieldSet} widget functionality
 * within the {@code cq:dialog} XML node
 */
class FieldSetHandler implements WidgetSetHandler {
    private static final String EMPTY_FIELDSET_EXCEPTION_MESSAGE = "No valid fields found in fieldset class ";

    /**
     * Processes the user-defined data and writes it to XML entity
     * @param element Current XML element
     * @param field Current {@code Field} instance
     */
    @Override
    @SuppressWarnings({"deprecation", "squid:S1874"})
    // the processing of deprecated "IgnoreField" annotation remains for compatibility reasons until v.2.0.0
    public void accept(Element element, Field field) {
        // Define the working @FieldSet annotation instance and the fieldset type
        FieldSet fieldSet = field.getDeclaredAnnotation(FieldSet.class);
        Class<?> fieldSetType = field.getType();

        // Get the filtered fields collection for the current container; early return if collection is empty
        List<Field> fields = getContainerFields(element, field, fieldSetType);

        // COMPATIBILITY: retrieve and process list of fields marked with a legacy "IgnoreField" annotation
        // to be removed after v.2.0.0
        List<Field> legacyIgnoredFields = PluginReflectionUtility.getAllFields(
                fieldSetType,
                Collections.singletonList(f -> f.isAnnotationPresent(IgnoreField.class)));
        if (!legacyIgnoredFields.isEmpty()) {
            fields = fields.stream()
                    .filter(f -> legacyIgnoredFields.stream()
                            .anyMatch(ignoredField -> !f.getName().equals(ignoredField.getName())))
                    .collect(Collectors.toList());
        }
        // end of compatibility block

        if(fields.isEmpty()) {
            PluginRuntime.context().getExceptionHandler().handle(new InvalidSettingException(
                    EMPTY_FIELDSET_EXCEPTION_MESSAGE + fieldSetType.getName()
            ));
            return;
        }

        // Cache existing name prefix and set updated name prefix with the parameter of the FieldSet annotation as needed
        String previousNamePrefix = getXmlUtil().getNamePrefix();
        if (StringUtils.isNotBlank(fieldSet.namePrefix())) {
            getXmlUtil().setNamePrefix(previousNamePrefix + getXmlUtil().getValidFieldName(fieldSet.namePrefix()));
        }

        // append the valid fields to the container
        Handler.appendToContainer(element, fields);

        // Restore the name prefix
        getXmlUtil().setNamePrefix(previousNamePrefix);
    }
}