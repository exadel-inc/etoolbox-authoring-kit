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

/**
 * Marks that the properties of a specific annotation are expected to be automatically mapped to the attributes of a Granite UI
 * entity. This setting extends to all the properties of annotation unless a narrower list is specified via {@link MapProperties#value()}.
 * <p>In addition, this annotation can be used to specify the scope the properties are mapped in e.g. {@link Scopes#COMPONENT}
 * certifies that the properties are valid for {@code .content.xml} and not for a {@code cq:dialog} node. Annotation
 * handlers can use this value to adjust their behavior.</p>
 * <p>Moreover, a specific prefix can be specified that will be prepended to all the automatically mapped properties</p>
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MapProperties {

    /**
     * Defines mapping rules for the properties (methods) of the current annotation. Default rule is that all the property
     * values of an appropriate type (string, number, enum) are mapped. The specific rules are:
     * <p>1) If one or more properties names are specified "as they are", only these properties are mapped.</p>
     * <p>2) Otherwise, if one or more names are specified with the prepended negation sign ("{@code !}"), all the properties
     * <i>except</i> the specified ones are mapped.</p>
     * <p>3) If there are names with and without "{@code !}" sign, rule (1) is in effect
     * @return Array of strings, or an empty array
     */
    String[] value() default {};

    /**
     * When set, specifies one or more scopes this annotation can be mapped to,
     * i.e. whether this is rendered to {@code cq:Component} (component root), {@code cq:dialog}, {@code cq:editorConfig},
     * or any other appropriate JCR node. Default value is equal to "all applicable scopes".
     * <p>Note: this value has no effect for the properties that are managed by a specific built-in or custom handler.
     * Also, it can be overridden by a {@link PropertyRendering#scope()} setting if specified at the property level</p>
     * @return One or more {@code Scope} values
     * @see Scopes
     */
    String[] scope() default Scopes.DEFAULT;

    /**
     * When initialized to a non-blank value, allows setting name prefix for all the relevant fields of current annotation
     * @return String value
     */
    String prefix() default "";
}
