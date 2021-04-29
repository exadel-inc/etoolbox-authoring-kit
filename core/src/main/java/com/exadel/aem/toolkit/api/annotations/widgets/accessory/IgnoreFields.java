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
import com.exadel.aem.toolkit.api.annotations.main.ClassMember;

/**
 * Used to specify fields that are ignored (skipped) when preparing data for Granite component rendering.
 * Used for the case when the current dialog class or fieldset extends another class and would expose
 * one or more {@code DialogField}s from a superclass that is not actually needed
 * @see ClassField
 * @deprecated This is deprecated and will be removed in a version after 2.0.2. Please use {@link Ignore} instead
 */
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Deprecated
@SuppressWarnings("squid:S1133")
public @interface IgnoreFields {

    /**
     * Enumerates class members to be skipped when rendering a dialog or dialog part.
     * Each member is specified by a reference to a {@code Class} and the name of a field or method
     * @return One or more {@code ClassMember} annotations
     * @see ClassMember
     */
    ClassField[] value();
}
