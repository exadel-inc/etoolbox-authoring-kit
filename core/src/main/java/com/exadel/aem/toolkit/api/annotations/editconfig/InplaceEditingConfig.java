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
package com.exadel.aem.toolkit.api.annotations.editconfig;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.exadel.aem.toolkit.api.annotations.meta.AnnotationRendering;
import com.exadel.aem.toolkit.api.annotations.widgets.Extends;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RichTextEditor;

/**
 * Defines a child in-place editor for this component. Upon processing this annotation,
 * a {@code cq:editConfig/cq:inplaceEditing/cq:childEditors/[editorName]} node within a component's configuration is created
 * See <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/components-basics.html#main-pars_title_9_pnfkfl_refd_">
 *     AEM Components documentation</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@AnnotationRendering(properties = "all")
public @interface InplaceEditingConfig {

    /**
     * Maps to the {@code type} attribute of current {@code cq:editConfig/cq:inplaceEditing/cq:childEditors/<editorName>} node
     * @see EditorType
     * @return String value
     */
    String type() default EditorType.EMPTY;

    /**
     * Maps to the {@code editElementQuery} attribute of current {@code cq:editConfig/cq:inplaceEditing/cq:childEditors/<editorName>} node
     * @return String value
     */
    String editElementQuery() default "";

    /**
     * Used to define tag name of current in-place editor config. If not set, {@link InplaceEditingConfig#propertyName()}
     * is used
     * @return String value
     */
    String name() default "";

    /**
     * If more than one in-place editors are configured for this component, used to differentiate between them as items
     * in a Granite UI dropdown
     * @return String value
     */
    String title() default "";

    /**
     * Maps to the {@code propertyName} attribute of the current {@code cq:editConfig/cq:inplaceEditing/cq:childEditors/<editorName>} node
     * @return String value, non-blank
     */
    String propertyName();

    /**
     * Maps to the {@code textPropertyName} attribute of the current {@code cq:editConfig/cq:inplaceEditing/cq:childEditors/<editorName>} node
     * @return String value
     */
    String textPropertyName() default "";

    /**
     * Specifies optional inheritance of a {@link RichTextEditor} configuration set up elsewhere in the project
     * @see RichTextEditor
     * @return {@code @Extends} annotation value
     */
    Extends richText() default @Extends;

    /**
     * Specifies optional {@link RichTextEditor} configuration for this particular in-place editor
     * @see RichTextEditor
     * @return {@code @RichTextEditor} annotation value
     */
    RichTextEditor richTextConfig() default @RichTextEditor;
}
