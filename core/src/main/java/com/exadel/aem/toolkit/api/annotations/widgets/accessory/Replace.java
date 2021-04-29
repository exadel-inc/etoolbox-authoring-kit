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
 * Used to specify a member of the current class or any of the superclasses that will be skipped and replaced
 * by the current member, and the widget attached to it.
 * <p>This annotation is useful for "altering" a member from a superclass, e.g. when we have
 * a {@code @DialogField @TextField private String text;} field in a superclass and need to have the virtually same field
 * in a descendant class, but a property of {@code @DialogField} or {@code @TextField} should be changed.</p>
 * <p>The replacing member will occupy the same place in the widgets stack as the member being replaced unless it specifies
 * another particular position (by e.g. {@code ranking} property)</p>
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Replace {

    /**
     * Specifies the member to be replaced with the current one
     * @see ClassMember
     * @return Valid {@link ClassMember} structure
     */
    ClassMember value();
}
