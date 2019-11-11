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
package com.exadel.aem.toolkit.core.util.validation;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.widgets.datepicker.DateTimeValue;
import com.exadel.aem.toolkit.api.annotations.meta.Validator;

/**
 * {@link Validator} implementation for testing that provided DateTimeValue is valid
 */
public class DateTimeValidator implements Validator {
    private static final String MSG_DATETIME_EXPECTED = "valid date/time value expected";

    /**
     * Tests that the provided DateTime is valid
     * @param obj {@code DateTimeValue} instance
     * @return True or false
     */
    @Override
    public boolean test(Object obj) {
        return getDateTimeInstance(obj) != null;
    }

    /**
     * Returns whether this object is of {@code DateTimeValue} type
     * @param obj Tested value
     * @return True or false
     */
    @Override
    public boolean isApplicableTo(Object obj) {
        return ClassUtils.isAssignable(obj.getClass(), DateTimeValue.class);
    }

    /**
     * Filters out {@code DateTimeValue} with insufficient and/or erroneous data
     * @param obj {@code DateTimeValue} annotation instance
     * @return Filtered value
     */
    @Override
    public Object getFilteredValue(Object obj) {
        return getDateTimeInstance(obj);
    }

    @Override
    public String getWarningMessage() {
        return MSG_DATETIME_EXPECTED;
    }

    /**
     * Utility method to create Java {@code Temporal} from {@code DateTimeValue} annotation
     * @param obj {@code DateTimeValue} annotation instance
     * @return Canonical Temporal instance, or null
     */
    private static Temporal getDateTimeInstance(Object obj) {
        DateTimeValue dt = (DateTimeValue)obj;
        try {
            if (StringUtils.isNotBlank(dt.timezone())) { // the following induces exception if any of DateTime parameters is invalid
                return ZonedDateTime.of(dt.year(), dt.month(), dt.day(), dt.hour(), dt.minute(), 0, 0, ZoneId.of(dt.timezone()));
            }
            return LocalDateTime.of(dt.year(), dt.month(), dt.day(), dt.hour(), dt.minute());
        } catch (DateTimeException e) {
            return null;
        }
    }

}
