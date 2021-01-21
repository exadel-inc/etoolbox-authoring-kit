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
import java.util.function.BiConsumer;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.exceptions.InvalidFieldContainerException;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.util.PluginNamingUtility;
import com.exadel.aem.toolkit.plugin.util.PluginXmlContainerUtility;

/**
 * Handler used to prepare data for {@link FieldSet} widget rendering
 */
class FieldSetHandler implements BiConsumer<Source, Target> {

    private static final String EMPTY_FIELDSET_EXCEPTION_MESSAGE = "No valid fields found in fieldset class ";

    /**
     * Implements the {@code BiConsumer<Source, Target} pattern to process settings specified by {@link FieldSet}
     * and provide data for widget rendering
     * @param source Member that defines a {@code FieldSet}
     * @param target Data structure used for rendering
     */
    @Override
    public void accept(Source source, Target target) {
        // Define the working @FieldSet annotation instance and the fieldset type
        FieldSet fieldSet = source.adaptTo(FieldSet.class);
        Class<?> fieldSetType = source.getContainerClass();

        List<Source> fieldSetEntries = PluginXmlContainerUtility.getContainerEntries(source, true);

        if (fieldSetEntries.isEmpty()) {
            PluginRuntime.context().getExceptionHandler().handle(
                new InvalidFieldContainerException(EMPTY_FIELDSET_EXCEPTION_MESSAGE + fieldSetType.getName())
            );
            return;
        }

        if (StringUtils.isNotBlank(fieldSet.namePrefix())) {
            target
                .prefix(PluginNamingUtility.getValidFieldName(fieldSet.namePrefix()))
                .postfix(fieldSet.namePostfix());
        }
        // Append the valid sources to the container
        PluginXmlContainerUtility.appendToContainer(target, fieldSetEntries);
    }
}
