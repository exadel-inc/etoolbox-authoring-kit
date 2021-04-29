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
package com.exadel.aem.toolkit.api.annotations.layouts;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.main.ClassMember;

/**
 * Used to specify the placement of a Granite UI component. The placement can be defined relative to a sibling component
 * (see {@link Place#before()} and {@link Place#after}). In a multi-tab or multi-panel Granite UI dialog, one can
 * specify in which section (such as a tab or accordion panel) this component is placed
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Place {

    /**
     * If specified, contains the string that is equal to a {@link Tab} or an {@link AccordionPanel} title
     * in order to place the current widget in the appropriate container.
     * Skip this value if you don't need any specific placement
     * @return String value (optional)
     */
    String value() default "";

    /**
     * If specified, refers to a class member (a method or field) this member must be placed <b>before</b> when situated
     * in the same container
     * @return {@link ClassMember} value (optional)
     */
    ClassMember before() default @ClassMember;

    /**
     * If specified, refers to a class member (a method or field) this member must be placed <u>after</u> when situated
     * in the same container
     * @return {@link ClassMember} value (optional)
     */
    ClassMember after() default @ClassMember;
}
