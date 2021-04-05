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

import com.exadel.aem.toolkit.api.annotations.meta.ValueRestriction;

/**
 * Used to set up {@link DatePicker#minDate()} and {@link DatePicker#maxDate()} values in an intuitive format
 */
@Retention(RetentionPolicy.RUNTIME)
@ValueRestriction("com.exadel.aem.toolkit.plugin.validators.DateTimeValidator")
public @interface DateTimeValue {
    int day() default 0;
    int month() default 0;
    int year() default 0;
    int hour() default 0;
    int minute() default 0;
    String timezone() default "";
}
