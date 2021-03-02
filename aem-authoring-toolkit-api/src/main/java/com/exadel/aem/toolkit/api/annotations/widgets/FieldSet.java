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
package com.exadel.aem.toolkit.api.annotations.widgets;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.IgnorePropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.markers._Default;

/**
 * Used to set up
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/fieldset/index.html">
 * FieldSet element</a> in TouchUI dialog
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.FIELDSET)
@PropertyMapping
public @interface FieldSet {

    /**
     * When set to a non-blank string, maps to the 'title' attribute of the current TouchUI dialog component.
     * Used to define title displayed above the FieldSet in TouchUI dialog
     * @return String value
     */
    @PropertyRendering(name = "jcr:title")
    String title() default "";

    /**
     * Used to define string prefix for names of all fields in the FieldSet
     * @return String value
     */
    @IgnorePropertyMapping
    String namePrefix() default "";

    /**
     * Used to define string postfix for names of all fields in the FieldSet
     * @return String value
     */
    @IgnorePropertyMapping
    String namePostfix() default "";

    /**
     * When set, defines the source for the fields used to compose the current {@code FieldSet}. If skipped,
     * the type of underlying {@code Field} is considered
     * @return Valid class referenced, or the {@link _Default} pseudo-reference
     */
    Class<?> source() default _Default.class;
}
