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
package com.exadel.aem.toolkit.api.annotations.widgets.autocomplete3;

import com.exadel.aem.toolkit.api.annotations.meta.AnnotationRendering;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to define an option within the {@link Autocomplete#items()} set
 * See documentation on <a href="https://developer.adobe.com/experience-manager/reference-materials/6-5/coral-ui/coralui3/Coral.Autocomplete.html">
 * Autocomplete</a> component
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.AUTOCOMPLETE_EAK)
@AnnotationRendering(properties = "all")
public @interface AutocompleteItem {

    /**
     * Maps to the {@code text} attribute of this Coral UI component's node.
     * Used to define the text displayed beside the option box
     * @return String value
     */
    String text();

    /**
     * Maps to the {@code value} attribute of this Coral UI component's node.
     * Used to define the value to be stored when this option is checked
     * @return String value
     */
    @PropertyRendering(allowBlank = true)
    String value();

    /**
     * When set to true, maps to the {@code disabled} attribute of this Granite UI component's node.
     * Changing the state of the autocomplete item
     * @return True or false
     */
    @PropertyRendering(ignoreValues = "false")
    boolean disabled() default false;

    /**
     * When set to true, maps to the {@code hidden} attribute of this Granite UI component's node.
     * Changing the state of the autocomplete item
     * @return True or false
     */
    @PropertyRendering(ignoreValues = "false")
    boolean hidden() default false;

    /**
     * When set to true, maps to the {@code selected} attribute of this Granite UI component's node.
     * Defines that the current option is selected by default
     * @return True or false
     */
    @PropertyRendering(ignoreValues = "false")
    boolean selected() default false;
}
