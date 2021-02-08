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
package com.exadel.aem.toolkit.api.annotations.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;

/**
 * Defines settings for rendering a specific value of an annotation to an XML attribute, such as, the name of the attribute,
 * and whether to render attribute with a particular value or not (typically, blank strings and values which are default
 * according to Adobe specifications don't need to be explicitly set and hence rendered)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PropertyRendering {

    /**
     * When set, maps to the 'name' attribute of a field node
     * @return String value
     */
    String name() default "";

    /**
     * When set to true, allows overriding prefix set for this field name
     * (in. e.g. a {@link FieldSet} annotation)
     * @return True or false
     */
    boolean ignorePrefix() default false;

    /**
     * When set, indicates that if a user-provided value matches one of the defined strings, the attribute will not
     * be rendered (used to omit meaningless defaults from XML markup)
     * @return String value, or an array of strings
     */
    String[] ignoreValues() default {};

    /**
     * When set to true, indicates that a user-defined value will be necessarily rendered into an XML attribute
     * even if it maps to an empty, or a blank strong
     * @return True or false
     */
    boolean allowBlank() default false;
}
