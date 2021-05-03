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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.AnnotationRendering;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.meta.StringTransformation;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestriction;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestrictions;

/**
 * Used to set up
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/administering/using/rich-text-editor.html">
 * RichTextEditor</a> component in Granite UI
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.RICHTEXT)
@AnnotationRendering(properties = "useFixedInlineToolbar")
public @interface RichTextEditor {

    /**
     * Maps to the {@code useFixedInlineToolbar} attribute of this Granite UI component's node
     * @return True or false
     */
    boolean useFixedInlineToolbar() default true;

    /**
     * Represents the set of "features" (i.e. plugins, corresponding toolbar icons, and relevant logic) for this RichTextEditor
     * when in "windowed" (non-fullscreen) mode.
     * Most common values are exposed by {@link RteFeatures}
     * @return String value, or an array of strings.
     */
    String[] features() default {};

    /**
     * Represents the set of "features" (i.e. plugins, corresponding toolbar icons, and relevant logic) for this RichTextEditor
     * when in fullscreen mode. If this value is not specified, the same set of features is used for either mode.
     * Most common values are exposed by {@link RteFeatures}
     * @return String value, or an array of strings.
     */
    String[] fullscreenFeatures() default {};

    /**
     * Represents the collection of {@link IconMapping}s to modify display of the toolbar
     * @return {@code @IconMapping} value, or an array of such values
     */
    IconMapping[] icons() default {};

    /**
     * Represents the collection of {@link ParagraphFormat}s to build up the "paraformat" dropdown
     * @return {@code @ParagraphFormat} value, or an array of such values
     */
    ParagraphFormat[] formats() default {};

    /**
     * Represents collection of {@link Characters} to populate the "Insert symbol" popup window
     * @return {@code @Characters} value, or an array of such values
     */
    Characters[] specialCharacters() default {};

    /**
     * Defines the default paste mode for this RichTextEditor
     * @return One of {@code PasteMode} values
     */
    @PropertyRendering(
        ignoreValues = "default",
        transform = StringTransformation.LOWERCASE
    )
    PasteMode defaultPasteMode() default PasteMode.DEFAULT;

    /**
     * Defines the rules applied to HTML links within this RichTextEditor
     * @see HtmlLinkRules
     * @return Valid {@code HtmlLinkRules} annotation
     */
    HtmlLinkRules htmlLinkRules() default @HtmlLinkRules;

    /**
     * Defines the rules that are in effect when pasting clipboard content to this RichTextEditor
     * @see HtmlPasteRules
     * @return Valid {@code HtmlPasteRules} annotation
     */
    HtmlPasteRules htmlPasteRules() default @HtmlPasteRules;

    /**
     * Defines the collection of external stylesheets (apart from the default ones) used for display of this
     * RichTextEditor's content
     * @return String value, or an array of strings
     */
    @ValueRestriction(ValueRestrictions.JCR_PATH)
    String[] externalStyleSheets() default {};

    /**
     * Represents the collection of {@link Style}s to build up the "styles" dropdown
     * @return Single {@code Style} value, or an array of such values
     */
    Style[] styles() default {};

    /**
     * Defines a maximal amount of operations managed by the "undo" plugin (max clicks of "undo" button) in this RichTextEditor
     * @return Long value, non-negative
     */
    @ValueRestriction(ValueRestrictions.NON_NEGATIVE)
    @PropertyRendering(ignoreValues = "50")
    long maxUndoSteps() default 50;

    /**
     * Defines the size of tab indent in this RichTextEditor
     * @return Long value, non-negative
     */
    @ValueRestriction(ValueRestrictions.NON_NEGATIVE)
    @PropertyRendering(ignoreValues = "4")
    long tabSize() default 4;

    /**
     * Defines the size of paragraph indent for e.g. list items in this RichTextEditor
     * @return Long value, non-negative
     */
    @ValueRestriction(ValueRestrictions.NON_NEGATIVE)
    @PropertyRendering(ignoreValues = "0")
    long indentSize() default 0;
}
