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

import com.exadel.aem.toolkit.api.annotations.meta.AnnotationRendering;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;

/**
 * Used to set up a specific
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/accordion/index.html">
 * Accordion panel</a> item in an accordion-shaped Granite UI dialog or within an {@code Accordion} widget
 */
@Target(value = ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@AnnotationRendering(properties = "all")
public @interface AccordionPanel {

    /**
     * Title of current Accordion panel
     * @return String value, required
     */
    String title();

    /**
     * True to open the item initially; false otherwise
     * @return True or false
     */
    @PropertyRendering(ignoreValues = "false")
    boolean active() default false;

    /**
     * True to disable the item; false otherwise
     * @return True or false
     */
    @PropertyRendering(ignoreValues = "false")
    boolean disabled() default false;
}
