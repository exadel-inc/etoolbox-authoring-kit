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
package com.exadel.aem.toolkit.plugin.adapters;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.widgets.datepicker.DatePicker;
import com.exadel.aem.toolkit.api.annotations.widgets.datepicker.DateTimeValue;

/**
 * Adapts a {@link DateTimeValue} object in order to validate the authored data and prepare it for rendering
 */
public class DateTimeSetting {
    private final DateTimeValue dateTimeValue;
    private Temporal dateTimeInstance;

    /**
     * Default constructor
     * @param dateTimeValue Reference to a {@link DatePicker} object
     */
    public DateTimeSetting(DateTimeValue dateTimeValue) {
        this.dateTimeValue = dateTimeValue;
    }

    /**
     * Gets whether this {@code DateTimeValue} instance is valid for rendering
     * @return True or false
     */
    public boolean isValid() {
        return dateTimeValue != null
            && (isEmpty() || getTemporal() != null);
    }

    /**
     * Gets whether the provided {@link DateTimeValue} is an empty (non-initialized) instance
     * @return True or false
     */
    public boolean isEmpty() {
        return StringUtils.isEmpty(dateTimeValue.timezone())
            && dateTimeValue.minute() == 0
            && dateTimeValue.hour() == 0
            && dateTimeValue.day() == 0
            && dateTimeValue.month() == 0
            && dateTimeValue.year() == 0;
    }

    /**
     * Creates (if needed) and retrieves a Java {@code Temporal} object which derives from the current
     * {@code DateTimeValue} annotation
     * @return Canonical Temporal instance, or null
     */
    public Temporal getTemporal() {
        if (dateTimeInstance != null) {
            return dateTimeInstance;
        }
        try {
            if (StringUtils.isNotBlank(dateTimeValue.timezone())) { // the following induces exception if any of DateTime parameters is invalid
                return ZonedDateTime.of(
                    dateTimeValue.year(),
                    dateTimeValue.month(),
                    dateTimeValue.day(),
                    dateTimeValue.hour(),
                    dateTimeValue.minute(),
                    0,
                    0,
                    ZoneId.of(dateTimeValue.timezone()));
            }
            dateTimeInstance = LocalDateTime.of(
                dateTimeValue.year(),
                dateTimeValue.month(),
                dateTimeValue.day(),
                dateTimeValue.hour(),
                dateTimeValue.minute());
        } catch (DateTimeException e) {
            dateTimeInstance = null;
        }
        return dateTimeInstance;
    }
}
