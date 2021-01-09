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
package com.exadel.aem.toolkit.api.annotations.widgets.autocomplete;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.IgnorePropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;

/**
 * Used to set up Coral 2
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/autocomplete/index.html">
 * Autocomplete element</a> in TouchUI dialog
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.AUTOCOMPLETE)
@PropertyMapping
@SuppressWarnings("unused")
public @interface Autocomplete {
    /**
     * Maps to the 'forceSelection' attribute of this TouchUI dialog component's node.
     * If set to true, forces the user to select only from the available choices
     * @return True or false
     */
    boolean forceSelection() default false;
    /**
     * Maps to the 'mode' attribute of this TouchUI dialog component's node.
     * Used to specify string matching mode for autocomplete proposals
     * @return True or false
     */
    String mode() default "contains";
    /**
     * Maps to the 'multiple' attribute of this TouchUI dialog component's node.
     * Used to set whether the user is able to make multiple selections
     * @return True or false
     */
    boolean multiple() default false;
    /**
     * When set, the {@code datasource} node is appended to the JCR buildup of this component
     * and populated with values of provided {@link AutocompleteDatasource} annotation
     * @return {@code @AutocompleteDatasource} instance
     */
    @IgnorePropertyMapping
    AutocompleteDatasource datasource() default @AutocompleteDatasource();
    /**
     * When set, the {@code values} node is appended to the JCR buildup of this component
     * and populated with values of provided {@link AutocompleteTag} annotation
     * @return {@code @AutocompleteTag} instance
     */
    @IgnorePropertyMapping
    AutocompleteTag values() default @AutocompleteTag();
    /**
     * When set, the {@code options} node is appended to the JCR buildup of this component
     * and populated with values of provided {@link AutocompleteList} annotation
     * @return {@code @AutocompleteList} instance
     */
    @IgnorePropertyMapping
    AutocompleteList options() default @AutocompleteList();
}
