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
package com.exadel.aem.toolkit.plugin.handlers.widgets;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;
import com.exadel.aem.toolkit.api.handlers.Handler;
import com.exadel.aem.toolkit.api.handlers.Handles;
import com.exadel.aem.toolkit.api.handlers.MemberSource;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.exceptions.InvalidLayoutException;
import com.exadel.aem.toolkit.plugin.handlers.layouts.common.WidgetContainerHandler;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;

/**
 * Implements {@code BiConsumer} to populate a {@link Target} instance with properties originating from a {@link Source}
 * object that define the Granite UI {@code FieldSet} widget look and behavior
 */
@Handles(FieldSet.class)
public class FieldSetHandler extends WidgetContainerHandler implements Handler {

    private static final String EMPTY_FIELDSET_EXCEPTION_MESSAGE = "No valid fields found in fieldset class ";

    /**
     * Processes data that can be extracted from the given {@code Source} and stores it into the provided {@code Target}
     * @param source {@code Source} object used for data retrieval
     * @param target Resulting {@code Target} object
     */
    @Override
    public void accept(Source source, Target target) {
        FieldSet fieldSet = source.adaptTo(FieldSet.class);
        Class<?> fieldSetType = source.adaptTo(MemberSource.class).getValueType();

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
