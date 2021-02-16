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

import com.exadel.aem.toolkit.api.annotations.meta.EnumValue;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.meta.StringTransformation;

/**
 * Used to set up accordion container widget inside dialog
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.ACCORDION)
@PropertyMapping
@SuppressWarnings("unused")
public @interface AccordionWidget {

    /**
     * Name of current accordion widget
     * @return String value, required
     */
    String name();

    /**
     * For the accordion-shaped TouchUI dialog layout, enumerates the accordion panels to be rendered
     * @return One or more {@code AccordionPanel} annotations
     * @see AccordionPanel
     */
    AccordionPanel[] panels();

    /**
     * Sets whether multiple items can be opened at the same time
     * @return True or False
     */
    boolean multiple() default false;

    /**
     * Determines the styling of this Accordion
     * @return One of {@code AccordionVariant} values
     * @see AccordionVariant
     */
    @EnumValue(transformation = StringTransformation.LOWERCASE)
    AccordionVariant variant() default AccordionVariant.DEFAULT;

    /**
     * Put vertical margin to the root element
     * @return True or False
     */
    boolean margin() default false;
}
