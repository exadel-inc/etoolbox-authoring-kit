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

import com.exadel.aem.toolkit.api.annotations.layouts.AccordionPanel;
import com.exadel.aem.toolkit.api.annotations.main.ClassMember;

/**
 * Used to specify class members and/or sections that could have been rendered into this dialog or dialog part but should
 * be instead ignored (skipped). This is commonly used when the current dialog class or fieldset extends another class and
 * would expose one or more {@code DialogField}s or {@code Tab}s / {@code AccordionPanel}s from superclass that are not
 * actually needed
 */
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Ignore {

    /**
     * Enumerates class members to be skipped when rendering a dialog or dialog part.
     * Each member is specified by a reference to a {@code Class} and the name of a field or method
     * @return One or more {@code ClassMember} annotations
     * @see ClassMember
     */
    ClassMember[] members() default {};

    /**
     * Enumerates container sections, such as Tabs or Accordion panels, to be skipped when rendering a dialog
     * or dialog part. Each section is specified by its title
     * @return One or more String values
     * @see com.exadel.aem.toolkit.api.annotations.container.Tab
     * @see AccordionPanel
     */
    String[] sections() default {};
}
