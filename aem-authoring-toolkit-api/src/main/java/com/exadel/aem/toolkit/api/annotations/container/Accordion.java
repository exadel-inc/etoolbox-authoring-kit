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
package com.exadel.aem.toolkit.api.annotations.container;

import com.exadel.aem.toolkit.api.annotations.meta.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to set up
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/accordion/index.html">
 * accordion element</a> in TouchUI dialog
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.ACCORDION)
@PropertyMapping
@SuppressWarnings("unused")
public @interface Accordion {

    /**
     * Maps to the 'jcr:title' attribute of a {@code cq:dialog/content/items/tabs/items/<this_accordion>} node
     *
     * @return String value, required
     */
    String title();

    /**
     * Whether multiple items can be opened at the same time.
     *
     * @return True or False
     */
    boolean multiple() default false;

    /**
     * The variant of the accordion.
     * The default look and feel.Quiet variant with minimal borders.
     * Large variant, typically used inside a navigation rail since it does not have borders on the sides.
     *
     * @return One of {@code AccordionVariant} values
     * @see AccordionVariant
     */
    @EnumValue(transformation = StringTransformation.LOWERCASE)
    AccordionVariant variant() default AccordionVariant.DEFAULT;

    /**
     * Put vertical margin to the root element.
     *
     * @return True or False
     */
    boolean margin() default false;
}
