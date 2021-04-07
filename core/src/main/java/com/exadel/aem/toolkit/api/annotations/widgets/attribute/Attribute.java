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
package com.exadel.aem.toolkit.api.annotations.widgets.attribute;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.AnnotationRendering;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;

/**
 * Used to set up <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/docs/server/commonattrs.html">
 * global HTML attributes</a> that are added to a rendered HTML tag
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@AnnotationRendering(prefix = "granite:")
public @interface Attribute {

    /**
     * The HTML {@code id} attribute
     * @return Optional string value
     */
    String id() default "";

    /**
     * The HTML {@code rel} attribute
     * @return Optional string value
     */
    String rel() default "";

    /**
     * The HTML {@code class} attribute. With this property, you can assign an arbitrary CSS class, or a set of classes,
     * space-separated, to the widget's container.
     * Mind that you can also use a number of <a href="https://opensource.adobe.com/coral-spectrum/dist/documentation/manual/styles.html#css-utility-classes">
     *     pre-defined utility classes</a>
     * @return Optional string value
     */
    @PropertyRendering(name = "class")
    String className() default "";

    /**
     * The HTML {@code title} attribute
     * @return Optional string value
     */
    String title() default "";

    /**
     * The HTML {@code hidden} attribute
     * @return True or false
     */
    boolean hidden() default false;

    /**
     * Optional collection of extra attributes represented as name-value pairs
     * @return {@code @Data} annotation value, or an array of {@code @Data}
     */
    Data[] data() default {};
}
