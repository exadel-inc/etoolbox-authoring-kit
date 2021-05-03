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

/**
 * Defines settings for rendering data declared by an annotation in a Granite UI entity. They extend to all the properties
 * of the underlying annotation unless a narrower list is specified via {@link AnnotationRendering#properties()}.
 * <p>In addition, this annotation can be used to specify the scope the properties are rendered within.
 * E.g. {@link Scopes#COMPONENT} certifies that the properties are valid for {@code .content.xml} and not for a
 * {@code cq:dialog} node. Annotation handlers can use this value to adjust their behavior.</p>
 * <p>A specific prefix can be specified that will be prepended to all the automatically mapped properties</p>
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AnnotationRendering {

    /**
     * Defines the rules for the automapping of the current annotation's properties (methods). The default rule is that
     * all the property values of an appropriate type (string, number, enum) are mapped. The more specific rules are:
     * <p>1) If "{@code *}" or "{@code all}" are specified, all the appropriate properties are mapped.</p>
     * <p>2) If one or more property names are specified "as they are", and the properties list does not end with "{@code *}"
     * or "{@code all}", only the directly named properties are mapped.</p>
     * <p>3) If one or more names are specified with the prepended negation sign ("{@code !}"), they are not mapped
     * while the rest are mapped.</p>
     * <p>4) If there are names with and without "{@code !}" sign, rule (2) is in effect.
     * <p>5) If "{@code none}" is specified, there is no automapping
     * @return Array of strings, or an empty array
     */
    String[] properties() default "*";

    /**
     * When set, specifies one or more scopes this annotation can be mapped to,
     * i.e. whether this is rendered to {@code cq:Component} (component root), {@code cq:dialog}, {@code cq:editorConfig},
     * or any other appropriate JCR node. The default value is equal to "all applicable scopes".
     * <p>Note: this value has no effect for the properties that are rendered by a specific handler.
     * Also, it can be overridden by a {@link PropertyRendering#scope()} setting if specified at the property level</p>
     * @return One or more {@code Scope} values
     * @see Scopes
     */
    String[] scope() default Scopes.DEFAULT;

    /**
     * When initialized to a non-blank value, allows setting name prefix for all the relevant fields of the current
     * annotation
     * @return String value
     */
    String prefix() default "";
}
