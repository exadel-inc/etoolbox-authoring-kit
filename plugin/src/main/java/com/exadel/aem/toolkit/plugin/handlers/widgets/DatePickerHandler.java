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
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;
import com.exadel.aem.toolkit.plugin.validators.Validation;

/**
 * Implements {@code BiConsumer} to populate a {@link Target} instance with properties originating from a {@link Source}
 * object that define the Granite UI {@code DatePicker} widget look and behavior
 */
@Handles(DatePicker.class)
public class DatePickerHandler implements Handler {
    private static final String INVALID_FORMAT_EXCEPTION_TEMPLATE = "Invalid %s '%s' for @DatePicker field '%s'";
    private static final String INVALID_VALUE_EXCEPTION_TEMPLATE = "Property '%s' of @DatePicker does not correspond to specified valueFormat";

    /**
     * Processes data that can be extracted from the given {@code Source} and stores it into the provided {@code Target}
     * @param source {@code Source} object used for data retrieval
     * @param target Resulting {@code Target} object
     */
    @Override
    public void accept(Source source, Target target) {
        DatePicker datePickerAttribute = source.adaptTo(DatePicker.class);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;

        // check specified typeHint, report if invalid
        if (datePickerAttribute.typeHint() == TypeHint.STRING) {
            target.attribute(DialogConstants.PN_TYPE_HINT, datePickerAttribute.typeHint().toString());
        } else if (datePickerAttribute.typeHint() != TypeHint.NONE) {
            PluginRuntime.context().getExceptionHandler().handle(new ValidationException(
                    INVALID_FORMAT_EXCEPTION_TEMPLATE,
                    "typeHint",
                    datePickerAttribute.typeHint(),
                    target.getName()));
            return;
        }
        // for a String-storing source, check and process specified valueFormat, report if invalid
        if (datePickerAttribute.typeHint() == TypeHint.STRING
            && !StringUtils.isEmpty(datePickerAttribute.valueFormat())) {
            try {
                // Java DateTimeFormatter interprets D as 'day of year', unlike Coral engine,
                // so a replacement made here to make sure 'DD' as in 'YYYY-MM-DD' is not passed to formatter.
                // Another replacement is for treating timezone literals that can be surrounded by arbitrary symbols
                // but need to be surrounded with apostrophes in Java 1.8+
                String patchedValueFormat = datePickerAttribute.valueFormat()
                        .replaceAll("\\bD{1,2}\\b", "dd")
                        .replaceAll("\\W*([TZ])\\W*", "'$1'");
                dateTimeFormatter = DateTimeFormatter.ofPattern(patchedValueFormat);
            } catch (IllegalArgumentException e) {
                PluginRuntime.context().getExceptionHandler().handle(new ValidationException(
                        INVALID_FORMAT_EXCEPTION_TEMPLATE,
                        "valueFormat",
                        datePickerAttribute.valueFormat(),
                        target.getName()));
                return;
            }
        }
        // store values with specified or default formatting
        storeDateValue(datePickerAttribute.minDate(), target, DialogConstants.PN_MIN_DATE, dateTimeFormatter);
        storeDateValue(datePickerAttribute.maxDate(), target, DialogConstants.PN_MAX_DATE, dateTimeFormatter);
    }

    /**
     * Writes formatted {@link DateTimeValue} attribute to node
     * @param value     The {@code DateTimeValue} to store
     * @param target    {@link Target} to store data in
     * @param attribute Name of date-preserving attribute
     * @param formatter {@link DateTimeFormatter} instance
     */
    private void storeDateValue(
            DateTimeValue value,
            Target target,
            String attribute,
            DateTimeFormatter formatter
    ) {
        if (!Validation.forMethod(DatePicker.class, attribute).test(value)) {
            return;
        }
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
