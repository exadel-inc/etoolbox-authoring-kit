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

import java.time.DateTimeException;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.widgets.common.TypeHint;
import com.exadel.aem.toolkit.api.annotations.widgets.datepicker.DatePicker;
import com.exadel.aem.toolkit.api.annotations.widgets.datepicker.DateTimeValue;
import com.exadel.aem.toolkit.api.handlers.Handler;
import com.exadel.aem.toolkit.api.handlers.Handles;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.adapters.DateTimeSetting;
import com.exadel.aem.toolkit.plugin.exceptions.ValidationException;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.metadata.Metadata;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;
import com.exadel.aem.toolkit.plugin.validators.Validation;

/**
 * Implements {@code BiConsumer} to populate a {@link Target} instance with properties originating from a {@link Source}
 * object that define the Granite UI {@code DatePicker} widget look and behavior
 */
@Handles(DatePicker.class)
public class DatePickerHandler implements Handler {
    private static final String INVALID_FORMAT_EXCEPTION_TEMPLATE = "Invalid %s '%s' for @DatePicker field '%s'";
    private static final String INVALID_VALUE_EXCEPTION_TEMPLATE = "Property '%s' of @DatePicker does not correspond "
        + "to the specified value format";

    /**
     * Processes data that can be extracted from the given {@code Source} and stores it into the provided {@code Target}
     * @param source {@code Source} object used for data retrieval
     * @param target Resulting {@code Target} object
     */
    @Override
    public void accept(Source source, Target target) {
        DatePicker datePicker = source.adaptTo(DatePicker.class);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;

        // Check the specified {@code typeHint}, report if invalid
        if (datePicker.typeHint() == TypeHint.STRING) {
            target.attribute(DialogConstants.PN_TYPE_HINT, datePicker.typeHint().toString());
        } else if (datePicker.typeHint() != TypeHint.NONE) {
            PluginRuntime.context().getExceptionHandler().handle(new ValidationException(
                    INVALID_FORMAT_EXCEPTION_TEMPLATE,
                    "typeHint",
                    datePicker.typeHint(),
                    target.getName()));
            return;
        }
        // For a String-storing source, check specified {@code valueFormat}, report if invalid
        if (datePicker.typeHint() == TypeHint.STRING
            && !StringUtils.isEmpty(datePicker.valueFormat())) {
            try {
                // Java DateTimeFormatter interprets D as "day of the year", unlike the Granite engine,
                // so a replacement is made here to make sure "DD" as in "YYYY-MM-DD" is not passed to the formatter.
                // Another replacement is for treating timezone literals that can be surrounded by arbitrary symbols
                // but need to be surrounded with apostrophes in Java 1.8+
                String patchedValueFormat = datePicker.valueFormat()
                        .replaceAll("\\bD{1,2}\\b", "dd")
                        .replaceAll("\\W*([TZ])\\W*", "'$1'");
                dateTimeFormatter = DateTimeFormatter.ofPattern(patchedValueFormat);
            } catch (IllegalArgumentException e) {
                PluginRuntime.context().getExceptionHandler().handle(new ValidationException(
                        INVALID_FORMAT_EXCEPTION_TEMPLATE,
                        "valueFormat",
                        datePicker.valueFormat(),
                        target.getName()));
                return;
            }
        }
        // Validate inputs because otherwise can cause a formatting exception
        Validation.forProperty(Metadata.from(datePicker).getProperty(DialogConstants.PN_MIN_DATE))
            .test(datePicker.minDate());
        Validation.forProperty(Metadata.from(datePicker).getProperty(DialogConstants.PN_MAX_DATE))
            .test(datePicker.maxDate());
        // Store values with specified or default formatting
        storeDateValue(datePicker.minDate(), target, DialogConstants.PN_MIN_DATE, dateTimeFormatter);
        storeDateValue(datePicker.maxDate(), target, DialogConstants.PN_MAX_DATE, dateTimeFormatter);
    }

    /**
     * Writes formatted {@link DateTimeValue} attribute to node
     * @param value     The {@code DateTimeValue} to store
     * @param target    {@link Target} to store data in
     * @param attribute Name of the date-preserving attribute
     * @param formatter {@link DateTimeFormatter} instance
     */
    private void storeDateValue(
            DateTimeValue value,
            Target target,
            String attribute,
            DateTimeFormatter formatter
    ) {
        DateTimeSetting dateTimeSetting = new DateTimeSetting(value);
        if (dateTimeSetting.isEmpty()) {
            return;
        }
        try {
            target.attribute(attribute, formatter.format(Objects.requireNonNull(dateTimeSetting.getTemporal())));
        } catch (DateTimeException | NullPointerException e) {
            PluginRuntime.context().getExceptionHandler().handle(new ValidationException(
                    INVALID_VALUE_EXCEPTION_TEMPLATE,
                    attribute));
        }
    }
}
