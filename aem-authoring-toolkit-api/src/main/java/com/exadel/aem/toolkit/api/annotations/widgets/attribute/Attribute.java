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
package com.exadel.aem.toolkit.api.annotations.widgets.attribute;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.IgnorePropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyName;

/**
 * Used to set up <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/docs/server/commonattrs.html">
 * global HTML attributes</a> added to rendered HTML tags
 * @see com.exadel.aem.toolkit.api.annotations.container.Tab
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@PropertyMapping(prefix = "granite:")
@SuppressWarnings("unused")
public @interface Attribute {
    /**
     * The HTML 'id' attribute
     * @return String value
     */
    String id() default "";
    /**
     * The HTML 'rel' attribute
     * @return String value
     */
    String rel() default "";
    /**
     * The HTML 'class' attribute
     * @return String value
     */
    @PropertyName("class")
    String clas() default "";
    /**
     * The HTML 'title' attribute
     * @return String value
     */
    String title() default "";
    /**
     * The HTML 'hidden' attribute
     * @return True or false
     */
    boolean hidden() default false;
    /**
     * Optional collection of extra attributes represented as name-value pairs
     * @return Single {@code @Data} annotation value, or an array of {@code @Data}
     */
    @IgnorePropertyMapping
    Data[] data() default {};
}
