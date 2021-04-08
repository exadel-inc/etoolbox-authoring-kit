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
package com.exadel.aem.toolkit.test.widget;

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.common.TypeHint;
import com.exadel.aem.toolkit.api.annotations.widgets.datepicker.DatePicker;
import com.exadel.aem.toolkit.api.annotations.widgets.datepicker.DatePickerType;
import com.exadel.aem.toolkit.api.annotations.widgets.datepicker.DateTimeValue;

import static com.exadel.aem.toolkit.plugin.utils.TestConstants.DEFAULT_COMPONENT_NAME;

@AemComponent(
        path = DEFAULT_COMPONENT_NAME,
        title = "DatePicker Widget Dialog"
)
@Dialog
@SuppressWarnings("unused")
public class DatePickerWidget {
    @DialogField(name = "date")
    @DatePicker(
            type = DatePickerType.DATETIME,
            displayedFormat = "YYYY-MM-DD[T]HH:mm:ss.000",
            valueFormat = "DD.MM.YYYY HH:mm",
            minDate = @DateTimeValue(day = 1, month = 1, year = 2019),
            maxDate = @DateTimeValue(day = 30, month = 4, year = 2020, hour = 12, minute = 10, timezone = "UTC+3"),
            typeHint = TypeHint.STRING,
            displayTimezoneMessage = true,
            beforeSelector = "before",
            afterSelector = "after"
    )
    String currentDate;
}
