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
 * Used to set up <a href="https://helpx.adobe.com/experience-manager/6-5/sites/administering/using/configure-rich-text-editor-plug-ins.html#pastemodes">
 * HTML paste rules</a> for a RichTextEditor control
 * @see RichTextEditor
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface HtmlPasteRules {

    /**
     * Defines whether bold formatting is preserved in the pasted text
     * @return True or false
     */
    boolean allowBold() default true;

    /**
     * Defines whether italic formatting is preserved in the pasted text
     * @return True or false
     */
    boolean allowItalic() default true;

    /**
     * Defines whether underline formatting is preserved in the pasted text
     * @return True or false
     */
    boolean allowUnderline() default true;

    /**
     * Defines whether anchors are preserved in the pasted text
     * @return True or false
     */
    boolean allowAnchors() default true;

    /**
     * Defines whether images are preserved in the pasted text
     * @return True or false
     */
    boolean allowImages() default true;

    /**
     * Defines whether and how tables are preserved in the pasted text
     * @see AllowElement
     * @return One of {@code AllowElement} values
     */
    AllowElement allowTables() default AllowElement.ALLOW;

    /**
     * Defines whether lists are preserved in the pasted text
     * @see AllowElement
     * @return One of {@code AllowElement} values
     */
    AllowElement allowLists() default AllowElement.ALLOW;

    /**
     * Specifies an optional set of block tags that are preserved in the pasted text
     * @return String value, or an array of strings
     */
    String[] allowedBlockTags() default {};

    /**
     * Specifies an optional replacement for block tags that are *not* preserved in the pasted text
     * @return String value
     */
    String fallbackBlockTag() default "";
}
