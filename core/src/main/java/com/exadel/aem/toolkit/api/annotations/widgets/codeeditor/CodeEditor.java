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
package com.exadel.aem.toolkit.api.annotations.widgets.codeeditor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestriction;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestrictions;

/**
 * Used to set up a syntax-highlighting code editor withing a Touch UI dialog or page. Default implementation is based
 * on the open-source <a href="https://ace.c9.io/">Ace Editor</a>
 */

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ResourceType(ResourceTypes.CODE_EDITOR)
public @interface CodeEditor {

    /**
     * Specifies a particular link to the editor's {@code js} file in an external repository or CDN. Can be used to
     * require a specific version or a custom build
     * @return Optional string value
     */
    @ValueRestriction(ValueRestrictions.NOT_BLANK_OR_DEFAULT)
    String source() default "";

    /**
     * Defines the code highlighting completion mode of the editor (the "language" or markup format it works with). Must
     * match one of the built-in modes enumerated in the <a href="https://github.com/ajaxorg/ace-builds/tree/master/src-noconflict">project's
     * repository</a> (unless a custom mode is supplied). By default, the {@code json} mode is used
     * @return Optional string value
     */
    @ValueRestriction(ValueRestrictions.NOT_BLANK_OR_DEFAULT)
    String mode() default "";

    /**
     * Defines the visual and code highlighting theme of the editor. Must match one of the built-in themes enumerated in
     * the <a href="https://github.com/ajaxorg/ace-builds/tree/master/src-noconflict">project's repository</a> (unless a
     * custom theme is supplied). By default, the {@code crimson_editor} theme is used
     * @return Optional string value
     */
    @ValueRestriction(ValueRestrictions.NOT_BLANK_OR_DEFAULT)
    String theme() default "";

    /**
     * Declares options that will be passed to the {@code CodeEditor} upon initialization. Every option is a key-value
     * pair. The option keys originate from the <a href="https://ajaxorg.github.io/ace-api-docs/index.html">editor's
     * API</a>
     * @return Optional array of {@link CodeEditorOption} objects
     */
    CodeEditorOption[] options() default {};

    /**
     * When set, specifies the string marker prepended to the data stored in JCR for the current field. E.g., if the
     * visible value of the field is {@code "Hello World"} and the {@code dataPrefix = "text:"} is specified, to the JCR
     * goes {@code "text:Hello World"}. This can be used to distinguish between snippets written in different languages,
     * or else to prevent AEM from falsely processing the field value as some Granite content (e.g. when storing JSON
     * strings in a multifield)
     * @return Optional string value
     */
    String dataPrefix() default "";
}
