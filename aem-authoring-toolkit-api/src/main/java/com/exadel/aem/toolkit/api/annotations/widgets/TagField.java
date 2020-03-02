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

import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;

/**
 * Used to set up TagField element in TouchUI dialog
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.TAG)
@PropertyMapping
@SuppressWarnings("unused")
public @interface TagField {
    /**
     *  When set to a non-blank string, maps to the 'emptyText' attribute of this TouchUI dialog component's node.
     *  Used to define text hint for an empty TagField
     *  @return String value
     */
    String emptyText() default "";

    /**
     * Maps to the 'multiple' attribute of this TouchUI dialog component's node.
     * Used to set whether the user is able to make multiple selections
     * @return True or false
     */
    boolean multiple() default false;

    /**
     * Maps to the 'forceSelection' attribute of this TouchUI dialog component's node.
     * If set to true, forces the user to select only from the available choices
     * @return True or false
     */
    boolean forceSelection() default false;

    /**
     * Maps to the 'autocreateTag' attribute of this TouchUI dialog component's node.
     * When set to true, create the user defined tag during form submission
     * @return True or false
     */
    boolean autocreateTag() default true;

    /**
     * Maps to the 'deleteHint' attribute of this TouchUI dialog component's node.
     * If set to true, generate the `SlingPostServlet @Delete
     * <http://sling.apache.org/documentation/bundles/manipulating-content-the-slingpostservlet-servlets-post.html#delete>`
     * @return True or false
     */
    boolean deleteHint () default true;

    /**
     * Maps to the 'root' attribute of this TouchUI dialog component's node.
     * Used to define the root path of the tags
     * @return String value representing valid JCR path
     */
    String rootPath() default "/";
}
