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

package com.exadel.aem.toolkit.api.annotations.widgets.accessory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.main.ClassMember;

/**
 * Used to specify fields that are ignored while rendering XML markup for the current dialog. Typically used
 * for the case when current dialog class extends another class  exposing one or more {@code DialogField}s that are
 * not needed
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreFields {
    /**
     * For the child classes, enumerates the fields to be skipped from rendering XML for the current dialog.
     * Each field is specified by a reference to a {@code Class} and a file name
     * @see ClassMember
     * @return One or more {@code ClassField} annotations
     */
    ClassMember[] value();
}
