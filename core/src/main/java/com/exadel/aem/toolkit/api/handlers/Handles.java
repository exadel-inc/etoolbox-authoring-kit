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
package com.exadel.aem.toolkit.api.handlers;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.Scopes;
import com.exadel.aem.toolkit.api.markers._Default;

/**
 * Used to specify one or more ToolKit annotations processed by a handler class, and also to marshal
 * invocation order if multiple handlers are attached to the same annotation
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Handles {

    /**
     * Enumerates the ToolKit annotations this handler processes
     * @return A {@code Class} reference, or an array of classes
     */
    Class<? extends Annotation>[] value();

    /**
     * When set, specifies one or more scopes in which this handler is effective, i.e. whether it will be triggered
     * for {@code cq:Component} (component root), {@code cq:dialog}, {@code cq:editorConfig}, or any other appropriate
     * JCR node. Default value is equal to "all applicable scopes".
     * <p>The latter, however, differs in meaning for widget annotation handlers (where it is {@code cq:dialog} and
     * {@code cq:design_dialog} together) and class-wide annotation handlers. In the latter case, the default scope
     * matches the scope of the built-in annotations present</p>
     * @return One or more {@code Scope} values
     * @see Scopes
     */
    String[] scope() default Scopes.DEFAULT;

    /**
     * When specified, denotes a handler that must follow the current handler in the execution chain.
     * This property is used to align handlers when execution sequence matters
     * @return {@code Class} of the sibling handler
     */
    Class<?> before() default _Default.class;

    /**
     * When specified, denotes a handler that must precede the current handler in the execution chain.
     * This property is used to align handlers when execution sequence matters
     * @return {@code Class} of the sibling handler
     */
    Class<?> after() default _Default.class;
}
