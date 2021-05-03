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
package com.exadel.aem.toolkit.api.annotations.widgets.rte;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used to set up <a href="https://helpx.adobe.com/experience-manager/6-5/sites/administering/using/configure-rich-text-editor-plug-ins.html#linkstyles">
 * HTML link rules</a> for a {@code RichTextEditor} control
 * @see RichTextEditor
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface HtmlLinkRules {

    /**
     * If set to a non-blank string, represents the CSS style for links to internal resources
     * @return String value
     */
    String cssInternal() default "";

    /**
     * If set to a non-blank string, represents the CSS style for links to external resources
     * @return String value
     */
    String cssExternal() default "";

    /**
     * It set, defines the set of valid protocols for links, such as {@code http://}, {@code file://}, etc.
     * @return A string representing a protocol as shown above, or an array of strings
     */
    String[] protocols() default {};

    /**
     * It set, defines the default protocol for links
     * @return A string representing a protocol
     */
    String defaultProtocol() default "";

    /**
     * Defines the {@code target} attribute for a link to an internal resource
     * @see LinkTarget
     * @return One of {@code LinkTarget} values
     */
    LinkTarget targetInternal() default LinkTarget.AUTO;

    /**
     * Defines the {@code target} attribute for a link to an external resource
     * @see LinkTarget
     * @return One of {@code LinkTarget} values
     */
    LinkTarget targetExternal() default LinkTarget.AUTO;
}
