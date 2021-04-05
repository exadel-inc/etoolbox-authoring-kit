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
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;

/**
 * Used to set up
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/password/index.html">
 * Password</a> component in Granite UI
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.PASSFIELD)
@AnnotationRendering(properties = "all")
public @interface Password {

    /**
     * When set to a non-blank string, maps to the {@code emptyText} attribute of this Granite UI component's node.
     * Used to define the text hint for an empty Password field
     * @return String value
     */
    String emptyText() default "";

    /**
     * When set to a non-blank string, maps to the {@code autocomplete} attribute of this Granite UI component's node.
     * Used to indicate if the value can be automatically completed by the browser
     * @return String value
     */
    String autocomplete() default "off";

    /**
     * When set to true, maps to the {@code autofocus} attribute of this Granite UI component's node.
     * Used to specify that this component will have focus after page load/refresh
     * @return True or false
     */
    boolean autofocus() default false;

    /**
     * When set to a non-blank string, maps to the {@code retype} attribute of this Granite UI component's node.
     * Used to specify the name of the other password field that is used to verify the current field
     * @return String value representing a valid component
     */
    String retype() default "";
}
