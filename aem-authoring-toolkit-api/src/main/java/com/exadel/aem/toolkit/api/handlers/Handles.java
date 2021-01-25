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
package com.exadel.aem.toolkit.api.handlers;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.markers._Default;

/**
 * Used to specify one or more AEM Authoring Toolkit annotations processed by a handler class
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Handles {

    /**
     * Enumerates AEM Authoring Toolkit annotations this handler processes
     * @return An {@code Annotation} or an array of annotations
     */
    Class<? extends Annotation>[] value();

    /**
     * When specified, denotes a handler that must follow the current handler in the execution chain.
     * This property is used to align handlers when execution sequence matters
     * @return {@code Class} of the sibling handler
     */
    Class<?> before() default _Default.class;

    /**
     * When specified, denotes a handler that must precede the current handler in the execution chain.
     * This property is used to align handlers when execution sequrence matters
     * @return {@code Class} of the sibling handler
     */
    Class<?> after() default _Default.class;
}
