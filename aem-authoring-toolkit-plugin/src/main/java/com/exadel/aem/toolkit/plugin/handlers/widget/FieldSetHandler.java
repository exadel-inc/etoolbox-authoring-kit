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
package com.exadel.aem.toolkit.plugin.handlers.widget;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.exceptions.InvalidFieldContainerException;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.util.PluginNamingUtility;
import com.exadel.aem.toolkit.plugin.util.PluginXmlContainerUtility;

/**
 * {@link WidgetSetHandler} implementation used to create markup responsible for Granite {@code FieldSet} widget functionality
 * within the {@code cq:dialog} node
 */
class FieldSetHandler implements WidgetSetHandler {
    private static final String EMPTY_FIELDSET_EXCEPTION_MESSAGE = "No valid fields found in fieldset class ";

    /**
     * Processes the user-defined data and writes it to {@link Target}
     * @param source Current {@link Source} instance
     * @param target Current {@link Target} instance
     */
    @Override
    public void accept(Source source, Target target) {
        // Define the working @FieldSet annotation instance and the fieldset type
        FieldSet fieldSet = source.adaptTo(FieldSet.class);
        Class<?> fieldSetType = source.getContainerClass();

        List<Source> sources = getContainerSource(source, fieldSetType);

        if(sources.isEmpty()) {
            PluginRuntime.context().getExceptionHandler().handle(new InvalidFieldContainerException(
                    EMPTY_FIELDSET_EXCEPTION_MESSAGE + fieldSetType.getName()
            ));
            return;
        }

        if (StringUtils.isNotBlank(fieldSet.namePrefix())) {
            target.prefix(PluginNamingUtility.getValidFieldName(fieldSet.namePrefix()))
                .postfix(fieldSet.namePostfix());
        }
        // append the valid sources to the container
        PluginXmlContainerUtility.append(sources, target);
    }
}
