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
import com.exadel.aem.toolkit.api.annotations.meta.StringTransformation;
import com.exadel.aem.toolkit.api.annotations.widgets.common.NodeFilter;

/**
 * Used to set up
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/pathfield/index.html">
 * PathField</a> component in Granite UI
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.PATHFIELD)
@AnnotationRendering(properties = "all")
public @interface PathField {

    /**
     * Maps to the {@code multiple} attribute of this Granite UI component's node. Used to set whether the user is able
     * to make multiple selections
     * @return True or false
     */
    @PropertyRendering(ignoreValues = "false")
    boolean multiple() default false;

    /**
     * Maps to the {@code forceSelection} attribute of this Granite UI component's node. If set to true, forces the user
     * to select only from the list of given options. Otherwise the user can pass an arbitrary value
     * @return True or false
     */
    @PropertyRendering(ignoreValues = "false")
    boolean forceSelection() default false;

    /**
     * Maps to the {@code droppable} attribute of this Granite UI component's node. Indicates if assets can be dropped
     * on the {@code PathField}
     * @return True or false
     */
    @PropertyRendering(ignoreValues = "false")
    boolean droppable() default false;

    /**
     * Maps to the {@code deleteHint} attribute of this Granite UI component's node. If set to true, generates the <a
     * href="https://sling.apache.org/documentation/bundles/manipulating-content-the-slingpostservlet-servlets-post.html#delete">
     * {@code @Delete}</a> attribute for the HTTP request
     * @return True or false
     */
    @PropertyRendering(ignoreValues = "true")
    boolean deleteHint() default true;

    /**
     * When set to a non-blank string, maps to the {@code emptyText} attribute of this Granite UI component's node. Used
     * to define the text hint for an empty PathField
     * @return String value
     */
    String emptyText() default "";

    /**
     * Defines the URI template that returns the suggestion markup. The template supports the following variables:<br>
     * {@code query} - the query entered by the user;<br>
     * {@code offset} - the offset of the pagination;<br>
     * {@code limit} - the limit of the pagination
     * @return String value
     */
    String suggestionSrc() default "";

    /**
     * Defines the URI template that returns the picker markup. The template supports the {@code value} variable which
     * refers to the value of the first item
     * @return String value
     */
    String pickerSrc() default "";

    /**
     * Maps to the {@code root} attribute of this Granite UI component's node. Used to define the root node from which
     * {@code PathField} navigation starts
     * @return String value representing valid JCR path
     */
    String rootPath() default "/";

    /**
     * Specifies the filter applied to the suggestion and picker
     * @return One of {@code NodeFilter} values
     * @see NodeFilter
     */
    @PropertyRendering(transform = StringTransformation.CAMELCASE, ignoreValues = "hierarchyNotFile")
    NodeFilter filter() default NodeFilter.HIERARCHY_NOT_FILE;
}
