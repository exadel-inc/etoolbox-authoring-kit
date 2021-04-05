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

import com.exadel.aem.toolkit.api.annotations.meta.AnnotationRendering;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestriction;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestrictions;

/**
 * Used to set up an entry in {@link RichTextEditor#styles()} array. Represents a matching between an HTML style to mark
 * selected text with and its description in the <a href="https://helpx.adobe.com/experience-manager/6-5/sites/administering/using/configure-rich-text-editor-plug-ins.html#main-pars_title_11">
 *     styles</a> dropdown menu
 * @see RichTextEditor
 */
@Retention(RetentionPolicy.RUNTIME)
@ValueRestriction(ValueRestrictions.ALL_NOT_BLANK)
@AnnotationRendering(properties = "all")
public @interface Style {

    /**
     * Represents the CSS class added to the selected text
     * @return String value, non-blank
     */
    String cssName();

    /**
     * Represents the label shown in the dropdown for this style
     * @return String value, non-blank
     */
    String text();
}
