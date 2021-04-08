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
package com.exadel.aem.toolkit.api.annotations.widgets;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.AnnotationRendering;
import com.exadel.aem.toolkit.api.annotations.widgets.property.Property;

/**
 * Allows to define a {@code datasource} node for a {@link com.exadel.aem.toolkit.api.annotations.widgets.select.Select}
 * or a {@link com.exadel.aem.toolkit.api.annotations.widgets.radio.RadioGroup} annotation
 * Use this to specify either an EToolbox List / ACS Commons list source or a custom datasource for selection
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@AnnotationRendering(properties = "all")
public @interface DataSource {

    /**
     * When set to a non-blank string, stores as the {@code sling:resourceType} attribute of a {@code datasource node}
     * of the current widget
     * @return String value
     */
    String resourceType() default "";

    /**
     * When set to a non-blank string, stores as the {@code path} attribute of a {@code datasource node}
     * of the current widget
     * @return Valid JCR path, or an empty string
     */
    String path() default "";

    /**
     * Used to specify collection of {@link Property}s within this {@code DataSource}
     * @return Single {@code @Property} annotation, or an array of properties
     */
    Property[] properties() default {};
}
