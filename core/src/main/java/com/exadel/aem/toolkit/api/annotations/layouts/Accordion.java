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

import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.meta.StringTransformation;

/**
 * Used to define the accordion-shaped layout for a Granite UI dialog and/or to set up
 * an <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/accordion/index.html">
 * Accordion</a> widget
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.ACCORDION)
public @interface Accordion {

    /**
     * Enumerates the accordion panels to be rendered within this container
     * @return One or more {@code AccordionPanel} annotations
     * @see AccordionPanel
     */
    AccordionPanel[] value();

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
    @PropertyRendering(transform = StringTransformation.LOWERCASE)
    AccordionVariant variant() default AccordionVariant.DEFAULT;

    /**
     * Put vertical margin to the root element
     * @return True or False
     */
    boolean margin() default false;
}
