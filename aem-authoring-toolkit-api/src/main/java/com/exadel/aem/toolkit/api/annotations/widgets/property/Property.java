/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Represent a name-value pair to be rendered as a TouchUI Dialog element attribute and its value
 * @see Properties
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Property {
    /**
     * Indicates the attribute name
     * @return String value, non-blank
     */
    String name();
    /**
     * Indicates the attribute value
     * @return String value
     */
    String value();
}
