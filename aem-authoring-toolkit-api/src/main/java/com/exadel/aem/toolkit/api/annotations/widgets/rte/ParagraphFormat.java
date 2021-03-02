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
package com.exadel.aem.toolkit.api.annotations.widgets.rte;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestriction;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestrictions;

/**
 * Used to set up an entry in {@link RichTextEditor#formats()} array. Represents matching between HTML tag to mark selected text
 * with and its description in the <a href="https://helpx.adobe.com/experience-manager/6-5/sites/administering/using/configure-rich-text-editor-plug-ins.html#main-pars_title_12">
 *     "paraformats"</a> dropdown menu
 * @see RichTextEditor
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ValueRestriction(ValueRestrictions.ALL_NOT_BLANK)
@PropertyMapping
public @interface ParagraphFormat {
    /**
     * Represents the paragraph or header tag, such as p, h1, h2, h3 etc.
     * @return String value, non-blank
     */
    String tag();
    /**
     * Represents the label shown in the dropdown for this tag
     * @return String value, non-blank
     */
    String description();
}
