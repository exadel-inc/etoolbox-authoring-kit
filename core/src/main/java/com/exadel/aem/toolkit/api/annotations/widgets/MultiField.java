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
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.markers._Default;

/**
 * Used to set up
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/multifield/index.html">
 * Multifield</a> component in Granite UI
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.MULTIFIELD)
@AnnotationRendering(properties = {"deleteHint", "typeHint"})
public @interface MultiField {

    /**
     * Used to specify a class that provides fields for this MultiField
     * @return Reference to a class
     * @deprecated Please use {@link MultiField#value()} instead
     */
    @Deprecated
    @SuppressWarnings("squid:S1133")
    Class<?> field() default _Default.class;

    /**
     * Used to specify a class that provides fields for this Multifield
     * @return Reference to a class
     */
    Class<?> value() default _Default.class;

    /**
     * Maps to the {@code deleteHint} attribute of this Granite UI component's node.
     * If set to true, generates the <a href="https://sling.apache.org/documentation/bundles/manipulating-content-the-slingpostservlet-servlets-post.html#delete">
     * {@code @Delete}</a> attribute for the HTTP request
     * @return True or false
     */
    @PropertyRendering(ignoreValues = "true")
    boolean deleteHint() default true;

    /**
     * Maps to the {@code typeHint} attribute of this Granite UI component's node.
     * Used to specify the value of <a href="https://sling.apache.org/documentation/bundles/manipulating-content-the-slingpostservlet-servlets-post.html#typehint">
     * {@code @TypeHint}</a> for the HTTP request
     * @return String value
     */
    String typeHint() default "";

    /**
     * If set to true, forces multiple-source mode for multifield despite of actual amount of fields
     * @return True or false
     */
    @PropertyRendering(
        name = "composite",
        ignoreValues = "false"
    )
    boolean forceComposite() default false;
}
