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
package com.exadel.aem.toolkit.api.annotations.widgets.select;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.IgnorePropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;

/**
 * Used to set up
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/select/index.html">
 * Select element</a> in TouchUI dialog
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.SELECT)
@PropertyMapping
@SuppressWarnings("unused")
public @interface Select {
    /**
     * Used to specify collection of {@link Option}s within this Select
     * @return Single {@code Option} annotation, or an array of Options
     */
    @IgnorePropertyMapping
    Option[] options() default {};
    /**
     * When set to a non-blank string, a {@code datasource} node is appended to the JCR buildup of this component
     * pointing to a ACS Commons list
     * @return Valid JCR path, or an empty string
     */
    @IgnorePropertyMapping
    String acsListPath() default "";
    /**
     * When set to a non-blank string, allows to override {@code sling:resourceType} attribute of a {@code datasource node}
     * pointing to a ACS Commons list
     * @return String value
     */
    @IgnorePropertyMapping
    String acsListResourceType() default "";
    /**
     * When set to a non-blank string, maps to the 'emptyText' attribute of this TouchUI dialog component's node.
     * @return String value
     */
    String emptyText() default "";
    /**
     * When this option is to true, and also {@link Select#acsListPath()} is specified, renders the {@code addNone} attribute
     * to the {@code datasource} node of this TouchUI dialog component's node so that "none" option is added to the
     * list of selectable options.
     * This option has no effect unless valid {@code acsListPath} is set.
     * @return True or false
     */
    @IgnorePropertyMapping
    boolean addNoneOption() default false;
}
