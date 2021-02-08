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
package com.exadel.aem.toolkit.plugin.util.validation;

import com.exadel.aem.toolkit.api.annotations.meta.Validator;
import com.exadel.aem.toolkit.api.annotations.widgets.datepicker.DateTimeValue;
import com.exadel.aem.toolkit.plugin.adapters.DateTimeSetting;

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
        if (!isApplicableTo(obj)) {
            return false;
        }
        return new DateTimeSetting((DateTimeValue) obj).isValid();
    }

    /**
     * Returns whether this object is of {@code DateTimeValue} type
     * @param obj Tested value
     * @return True or false
     */
    @Override
    public boolean isApplicableTo(Object obj) {
        return obj instanceof DateTimeValue;
    }

    @Override
    public String getWarningMessage() {
        return MSG_DATETIME_EXPECTED;
    }
}
