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
package com.exadel.aem.toolkit.api.annotations.widgets.datepicker;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to set up {@link DatePicker#minDate()} and {@link DatePicker#maxDate()} values in an intuitive format
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface DateTimeValue {

    /**
     * Specifies the day in a month
     * @return Integer value between {@code 1} and {@code 31}
     */
    int day() default 0;

    /**
     * Specifies the number of a month
     * @return Integer value between {@code 1} and {@code 12}
     */
    int month() default 0;

    /**
     * Specifies the year
     * @return Arbitrary integer number
     */
    int year() default 0;

    /**
     * Specifies the hour
     * @return Integer value between {@code 0} and {@code 23}
     */
    int hour() default 0;

    /**
     * Specifies the minute
     * @return Integer value between {@code 0} and {@code 59}
     */
    int minute() default 0;

    /**
     * Specifies the timezone
     * @return Optional string value
     */
    String timezone() default "";
}
