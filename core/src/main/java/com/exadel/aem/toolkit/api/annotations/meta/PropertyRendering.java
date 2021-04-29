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
package com.exadel.aem.toolkit.api.annotations.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;
import com.exadel.aem.toolkit.api.markers._Default;

/**
 * Defines settings for rendering a specific value of an annotation to a Granite/XML attribute. These settings comprise,
 * e.g., the name of the attribute, lexical form of the attribute's value, or whether to render the attribute with
 * a particular value or not
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PropertyRendering {

    /**
     * When set, maps to the {@code name} attribute of a dialog field node
     * @return String value
     */
    String name() default "";

    /**
     * When set, used to specify one or more scopes this annotation property is rendered in,
     * i.e. whether this is rendered to {@code cq:Component} (component root), {@code cq:dialog}, {@code cq:editorConfig},
     * or any other appropriate JCR node.
     * This setting applies only to values that technically can be rendered to multiple JCR nodes,
     * such as {@link com.exadel.aem.toolkit.api.annotations.main.Dialog} annotation properties
     * @return {@code Scope} value
     * @see Scopes
     */
    String[] scope() default Scopes.DEFAULT;

    /**
     * When set to true, allows overriding prefix set for this field name
     * (in. e.g. a {@link FieldSet} annotation)
     * @return True or false
     */
    boolean ignorePrefix() default false;

    /**
     * When set, indicates that if a user-provided value matches one of the defined strings, the attribute will not
     * be rendered (used to omit meaningless defaults from the resulting XML markup)
     * @return String value, or an array of strings
     */
    String[] ignoreValues() default {};

    /**
     * When set to true, indicates that a user-defined value will be necessarily rendered into an attribute
     * even if it maps to an empty or a blank string
     * @return True or false
     */
    boolean allowBlank() default false;

    /**
     * Defines whether the string value is stored as-is, or else is rendered in uppercase, lowercase, or came-lase. Most
     * of the time this setting is used for transforming {@code Enum} values
     * @return One of {@link StringTransformation} variants
     */
    StringTransformation transform() default StringTransformation.NONE;

    /**
     * When set, defines what value type will be assumed for the underlying property. It must be one of the JCR compliant
     * types, such as {@code String}, {@code long}, {@code double}, etc. This value is used to modify a default type hint
     * or remove a type hint for the "XML-stringified" representation of a value, e.g. when a boolean value must be
     * rendered as {@code "true"} and not as {@code "{Boolean}true"} which is by default
     * @return {@code Class} reference. One of the JCR-compliant classes must be used
     */
    Class<?> valueType() default _Default.class;
}
