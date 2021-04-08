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
package com.exadel.aem.toolkit.api.annotations.widgets.autocomplete;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.AnnotationRendering;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;

/**
 * Used to set up Coral 2
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/autocomplete/index.html">
 * Autocomplete</a> component in Granite UI
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.AUTOCOMPLETE)
@AnnotationRendering(properties = "all")
public @interface Autocomplete {

    /**
     * Maps to the {@code forceSelection} attribute of this Granite UI component's node.
     * If set to true, forces the user to select only from the available options without the possibility of an arbitrary
     * input
     * @return True or false
     */
    boolean forceSelection() default false;

    /**
     * Maps to the {@code mode} attribute of this Granite UI component's node.
     * Used to specify string matching mode for autocomplete proposals
     * @return String value (non-null)
     */
    String mode() default "contains";

    /**
     * Maps to the {@code multiple} attribute of this Granite UI component's node.
     * Used to set whether the user is able to make multiple selections
     * @return True or false
     */
    boolean multiple() default false;

    /**
     * When set, the {@code datasource} node is appended to the JCR buildup of this component
     * and populated with values of provided {@link AutocompleteDatasource} annotation
     * @return {@code @AutocompleteDatasource} instance
     */
    AutocompleteDatasource datasource() default @AutocompleteDatasource();

    /**
     * When set, the {@code values} node is appended to the JCR representation of this component
     * and populated with the values of the provided {@link AutocompleteTag} annotation
     * @return {@code @AutocompleteTag} instance
     */
    AutocompleteTag values() default @AutocompleteTag();

    /**
     * When set, the {@code options} node is appended to the JCR representation of this component
     * and populated with the values of provided {@link AutocompleteList} annotation
     * @return {@code @AutocompleteList} instance
     */
    AutocompleteList options() default @AutocompleteList();
}
