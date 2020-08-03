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

import com.exadel.aem.toolkit.api.annotations.main.ClassField;

/**
 * Used to specify field(-s) from the current class or any of the superclasses this field will replace when rendered
 * to a Coral UI widget. The field(-s) specified by this annotation will be ignored; and the current field will take place
 * of the first of the specified field (will be assigned the same ranking).
 * This annotation is useful for "altering" a field from a superclass, e.g. when we have
 * a {@code @DialogField @TextField private String text;} field in a superclass and need to have the virtually same field
 * in a descendant class, but a property of {@code @DialogField} or {@code @TextField} should be changed
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Replace {
    /**
     * Enumerates the fields to be replaced by the current field in the rendered XML for the dialog.
     * Each field is specified by a reference to a {@code Class} and a field name
     * @see ClassField
     * @return One or more {@code ClassField} annotations
     */
    ClassField value();
}
