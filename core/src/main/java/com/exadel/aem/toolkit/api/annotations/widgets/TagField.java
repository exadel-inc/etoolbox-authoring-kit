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
 * Used to set up TagField element in Granite UI
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.TAG)
@AnnotationRendering(properties = "all")
public @interface TagField {

    /**
     *  When set to a non-blank string, maps to the {@code emptyText} attribute of this Granite UI component's node.
     *  Used to define the text hint for an empty TagField
     *  @return String value
     */
    String emptyText() default "";

    /**
     * Maps to the {@code multiple} attribute of this Granite UI component's node.
     * Used to set whether the user is able to make multiple selections
     * @return True or false
     */
    boolean multiple() default false;

    /**
     * Maps to the {@code forceSelection} attribute of this Granite UI component's node.
     * If set to true, forces the user to select only from the available choices
     * @return True or false
     */
    boolean forceSelection() default false;

    /**
     * Maps to the {@code autocreateTag} attribute of this Granite UI component's node.
     * When set to true, the user-defined tag is created during form submission
     * @return True or false
     */
    boolean autocreateTag() default true;

    /**
     * Maps to the {@code deleteHint} attribute of this Granite UI component's node.
     * If set to true, generate the <a href="http://sling.apache.org/documentation/bundles/manipulating-content-the-slingpostservlet-servlets-post.html#delete">
     * {@code @Delete}</a> attribute for the http request
     * @return True or false
     */
    boolean deleteHint() default true;

    /**
     * Maps to the {@code root} attribute of this Granite UI component's node.
     * Used to define the root path of the tags
     * @return String value representing valid JCR path
     */
    String rootPath() default "/";
}
