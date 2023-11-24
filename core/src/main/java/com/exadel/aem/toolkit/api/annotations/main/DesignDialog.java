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
package com.exadel.aem.toolkit.api.annotations.main;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.AnnotationRendering;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.Scopes;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestriction;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestrictions;

/**
 * Used to store generic properties of an AEM component's Design Dialog according to the
 * <a href="https://docs.adobe.com/content/help/en/experience-manager-65/developing/components/components-basics.html#design-dialogs"> Adobe specification</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@AnnotationRendering(
    scope = Scopes.CQ_DESIGN_DIALOG,
    properties = "!forceIgnoreFreshness"
)
public @interface DesignDialog {

    /**
     * Maps to the {@code jcr:title} attribute of component's {@code cq:design_dialog} node
     * @return String value, non-blank
     */
    @PropertyRendering(name = "jcr:title")
    @ValueRestriction(ValueRestrictions.NOT_BLANK_OR_DEFAULT)
    String title() default "";

    /**
     * Renders as the `height` attribute of component's {@code cq:design_dialog} node
     * @return Double-typed number
     */
    @PropertyRendering(ignoreValues = "0.0")
    @ValueRestriction(ValueRestrictions.POSITIVE)
    double height() default 0;

    /**
     * Renders as the `width` attribute of component's {@code cq:design_dialog} node
     * @return Double-typed number
     */
    @PropertyRendering(ignoreValues = "0.0")
    @ValueRestriction(ValueRestrictions.POSITIVE)
    double width() default 0;

    /**
     * When set to true, forces the entire dialog to ignore freshness of the Granite UI form beside the dialog. This will
     * allow any component residing within the dialog to display its default value (if specified) regardless of whether
     * the underlying resource is being created (a "fresh" one) or just being edited
     * @return True or false
     */
    boolean forceIgnoreFreshness() default false;
}
