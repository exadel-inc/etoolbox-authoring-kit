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
package com.exadel.aem.toolkit.api.annotations.widgets.autocompletecoral3;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.meta.AnnotationRendering;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.widgets.common.OptionProvider;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.AUTOCOMPLETE_CORAL_3)
@AnnotationRendering(properties = "all")
public @interface Autocomplete {

    /**
     * Used to specify the collection of {@link AutocompleteOption}s within this Autocomplete
     * @return Single {@code AutocompleteOption} annotation, or an array of AutocompleteOptions
     */
    AutocompleteOption[] options() default {};

    /**
     * Used to specify the source for options handled by the ToolKit's {@code OptionProvider} mechanism
     * @return {@link OptionProvider} instance, or an empty {@code OptionProvider} if not needed
     */
    OptionProvider optionProvider() default @OptionProvider;

    /**
     * When set to a non-blank string, maps to the {@code placeholder} attribute of this Granite UI component's node.
     * Used to define the text hint for an empty Autocomplete
     * @return String value
     */
    String placeholder() default StringUtils.EMPTY;

    /**
     * When set to true, maps to the {@code match} attribute with 'startswith' value of this Granite UI component's node.
     * @return true or false
     */
    boolean matchStartsWith() default false;

    /**
     * When set to a non-blank string, maps to the {@code icon} attribute of this Granite UI component's node.
     * Used to define the component's icon
     * @return String value
     */
    String icon() default StringUtils.EMPTY;

    /**
     * When set to true, maps to the {@code disabled} attribute of this Granite UI component's node.
     * Defines that the current option is shown in the disabled state
     * @return True or false
     */
    boolean disabled() default false;

    /**
     * When set to true, maps to the {@code invalid} attribute of this Granite UI component's node.
     * @return True or false
     */
    boolean invalid() default false;

    /**
     * When set to true, maps to the {@code loading} attribute of this Granite UI component's node.
     * @return True or false
     */
    boolean loading() default false;

    /**
     * Indicates if the user is able to select multiple options
     * @return True or false
     */
    boolean multiple() default false;
}
