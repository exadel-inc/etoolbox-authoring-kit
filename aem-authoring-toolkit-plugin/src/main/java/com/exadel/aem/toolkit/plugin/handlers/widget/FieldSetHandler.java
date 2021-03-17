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
import com.exadel.aem.toolkit.plugin.exceptions.InvalidLayoutException;
import com.exadel.aem.toolkit.plugin.handlers.layouts.common.WidgetContainerHandler;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;

/**
 * Handler used to prepare data for {@link FieldSet} widget rendering
 */
class FieldSetHandler extends WidgetContainerHandler {

    private static final String EMPTY_FIELDSET_EXCEPTION_MESSAGE = "No valid fields found in fieldset class ";

    /**
     * Implements the {@code BiConsumer<Source, Target} pattern to process settings specified by {@link FieldSet}
     * and provide data for widget rendering
     * @param source Member that defines a {@code FieldSet}
     * @param target Data structure used for rendering
     */
    @Override
    public void accept(Source source, Target target) {
        FieldSet fieldSet = source.adaptTo(FieldSet.class);
        Class<?> fieldSetType = source.getValueType();

        List<Source> fieldSetEntries = getEntriesForContainer(source, true);

        if (fieldSetEntries.isEmpty()) {
            PluginRuntime.context().getExceptionHandler().handle(
                new InvalidLayoutException(EMPTY_FIELDSET_EXCEPTION_MESSAGE + fieldSetType.getName())
            );
            return;
        }

        if (StringUtils.isNotBlank(fieldSet.namePrefix())) {
            target.namePrefix(fieldSet.namePrefix());
        }
        if (StringUtils.isNotBlank(fieldSet.namePostfix())) {
            target.namePostfix(fieldSet.namePostfix());
        }
        populateContainer(fieldSetEntries, target);
    }
}
