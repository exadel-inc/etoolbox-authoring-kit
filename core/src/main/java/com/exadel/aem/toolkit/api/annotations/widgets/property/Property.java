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
package com.exadel.aem.toolkit.api.annotations.widgets.property;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a name-value pair to be rendered as a Granite UI component's attribute and its value
 * @see Properties
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Properties.class)
public @interface Property {

    /**
     * Indicates the attribute name
     * Relative path can be defined in such a way that the substring before the ultimate `/`
     * represents the path, and the substring after the ultimate `/` represents the property name.
     * @return String value, non-blank
     */
    String name();

    /**
     * Indicates the attribute value
     * @return String value
     */
    String value();
}
