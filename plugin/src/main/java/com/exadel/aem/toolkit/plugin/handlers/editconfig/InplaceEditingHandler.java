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
package com.exadel.aem.toolkit.plugin.handlers.editconfig;

import java.util.Arrays;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditorType;
import com.exadel.aem.toolkit.api.annotations.editconfig.InplaceEditingConfig;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RichTextEditor;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.api.markers._Default;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.exceptions.ReflectionException;
import com.exadel.aem.toolkit.plugin.handlers.widgets.common.InheritanceHandler;
import com.exadel.aem.toolkit.plugin.handlers.widgets.rte.RichTextEditorHandler;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.sources.Sources;
import com.exadel.aem.toolkit.plugin.utils.AnnotationUtil;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;
import com.exadel.aem.toolkit.plugin.utils.NamingUtil;

/**
 * Implements {@code BiConsumer} to populate a {@link Target} instance with properties originating from a
 * {@link Source} object that define the in-place editing config within a Granite UI {@code cq:editConfig} node
 */
public class InplaceEditingHandler implements BiConsumer<Source, Target> {

    /**
     * Processes data that can be extracted from the given {@code Source} and stores it into the provided {@code Target}
     * @param source {@code Source} object used for data retrieval
     * @param target Resulting {@code Target} object
     */
    @Override
    public void accept(Source source, Target target) {
        EditConfig editConfig = source.adaptTo(EditConfig.class);
        if (ArrayUtils.isEmpty(editConfig.inplaceEditing())
                || (editConfig.inplaceEditing().length == 1 && EditorType.EMPTY.equals(editConfig.inplaceEditing()[0].type()))) {
            return;
        }
        Target inplaceEditingNode = target.getOrCreateTarget(DialogConstants.NN_INPLACE_EDITING)
                .attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, DialogConstants.NT_INPLACE_EDITING_CONFIG)
                .attribute(DialogConstants.PN_ACTIVE, true);

        if (editConfig.inplaceEditing().length > 1) {
            inplaceEditingNode.attribute(DialogConstants.PN_EDITOR_TYPE, EditorType.HYBRID.toLowerCase());

            getChildEditorsNode(editConfig, inplaceEditingNode);

            getConfigNode(editConfig, inplaceEditingNode);
        } else {
            inplaceEditingNode.attribute(DialogConstants.PN_EDITOR_TYPE, editConfig.inplaceEditing()[0].type().toLowerCase());
            Target configNode = inplaceEditingNode.getOrCreateTarget(DialogConstants.NN_CONFIG);
            populateConfigNode(editConfig.inplaceEditing()[0], configNode);
        }
    }

    /**
     * Populate {@code cq:childEditors} node containing one or more {@code cq:ChildEditorConfig} subnodes
     * @param config {@link EditConfig} annotation instance
     * @param target Current {@link Target} instance
     */
    private void getChildEditorsNode(EditConfig config, Target target) {
        Target childEditorsNode = target.getOrCreateTarget(DialogConstants.NN_CHILD_EDITORS);
        Arrays.stream(config.inplaceEditing())
                .forEach(inplaceEditingConfig -> getSingleChildEditorNode(inplaceEditingConfig, childEditorsNode));
    }

    /**
     * Populate {@code cq:ChildEditorConfig} node
     * @param config {@link InplaceEditingConfig} annotation instance
     * @param target Current {@link Target} instance
     */
    private void getSingleChildEditorNode(InplaceEditingConfig config, Target target) {
        target.getOrCreateTarget(getConfigName(config))
            .attribute(DialogConstants.PN_PRIMARY_TYPE, DialogConstants.NT_CHILD_EDITORS_CONFIG)
            .attribute(DialogConstants.PN_TITLE, StringUtils.isNotBlank(config.title()) ? config.title() : getConfigName(config))
            .attribute(DialogConstants.PN_TYPE, config.type().toLowerCase());
    }

    /**
     * Generates a node containing one or more {@code cq:InplaceEditingConfig} sub-nodes
     * for use with {@code cq:inplaceEditing} markup
     * @param config {@link EditConfig} annotation instance
     * @param target Current {@link Target} instance
     */
    private void getConfigNode(EditConfig config, Target target) {
        Target configNode = target.getOrCreateTarget(DialogConstants.NN_CONFIG);
        for (InplaceEditingConfig childConfig : config.inplaceEditing()) {
            Target childConfigNode = configNode.getOrCreateTarget(getConfigName(childConfig))
                    .attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, DialogConstants.NT_INPLACE_EDITING_CONFIG);
            populateConfigNode(childConfig, childConfigNode);
        }
    }

    /**
     * Sets necessary attributes to {@code cq:InplaceEditingConfig} XML node
     * @param config {@link InplaceEditingConfig} annotation instance
     * @param target {@link Target} representing config node
     */
    private void populateConfigNode(InplaceEditingConfig config, Target target) {
        String propertyName = getValidPropertyName(config.propertyName());
        String textPropertyName = config.textPropertyName().isEmpty()
                ? propertyName
                : getValidPropertyName(config.textPropertyName());
        target.attribute(DialogConstants.PN_EDIT_ELEMENT_QUERY, config.editElementQuery())
                .attribute(DialogConstants.PN_PROPERTY_NAME, propertyName)
                .attribute(DialogConstants.PN_TEXT_PROPERTY_NAME, textPropertyName);
        populateRteConfig(config, target);
    }

    /**
     * Gets a standard-compliant value for a {@code propertyName} or {@code textPropertyName} attribute of {@link InplaceEditingConfig}
     * @param rawName Attribute value as passed by user
     * @return Converted standard-compliant name
     */
    private String getValidPropertyName(String rawName) {
        String propertyName = NamingUtil.getValidFieldName(rawName);
        if (propertyName.startsWith(DialogConstants.PARENT_PATH_PREFIX)) {
            return propertyName;
        }
        return DialogConstants.RELATIVE_PATH_PREFIX + propertyName;
    }

    /**
     * Plants necessary attributes and sub-nodes related to in-place rich text editor to {@code cq:InplaceEditingConfig}
     * XML node
     * @param config {@link InplaceEditingConfig} annotation instance
     * @param target {@link Target} representing config node
     */
    private void populateRteConfig(InplaceEditingConfig config, Target target) {
        Source referencedRteField = getReferencedRteField(config);
        if (referencedRteField != null && referencedRteField.adaptTo(RichTextEditor.class) != null) {
            BiConsumer<Source, Target> rteHandler = new RichTextEditorHandler(false);
            new InheritanceHandler(rteHandler).andThen(rteHandler).accept(referencedRteField, target);
            RichTextEditor rteAnnotation = referencedRteField.adaptTo(RichTextEditor.class);
            target.attributes(
                rteAnnotation,
                AnnotationUtil
                    .getPropertyMappingFilter(rteAnnotation)
                    .and(method -> !DialogConstants.PN_USE_FIXED_INLINE_TOOLBAR.equals(method.getName()))
            );
        }
        new RichTextEditorHandler(false).accept(config.richTextConfig(), target);
    }

    /**
     * Retrieves a component class field referenced by this {@link InplaceEditingConfig}
     * via {@link com.exadel.aem.toolkit.api.annotations.widgets.Extends}-typed attribute
     * @param config {@link InplaceEditingConfig} annotation instance
     * @return {@code Field} instance
     */
    private static Source getReferencedRteField(InplaceEditingConfig config) {
        if (config.richText().value().equals(_Default.class)
                && StringUtils.isBlank(config.richText().field())) {
            // richText attribute not specified, which is a valid case
            return null;
        }
        try {
            return Sources.fromMember(config.richText().value().getDeclaredField(config.richText().field()), config.richText().value());
        } catch (NoSuchFieldException e) {
            PluginRuntime.context().getExceptionHandler().handle(new ReflectionException(
                    config.richText().value(),
                    config.richText().field()
            ));
        }
        return null;
    }

    /**
     * Gets a non-blank string that would stand for the name of the current config
     * @param config {@link InplaceEditingConfig} annotation instance
     * @return String value
     */
    private static String getConfigName(InplaceEditingConfig config) {
        String result = StringUtils.isNotBlank(config.name())
            ? config.name()
            : config.propertyName();
        if (result.contains(CoreConstants.SEPARATOR_SLASH)) {
            return StringUtils.substringAfterLast(result, CoreConstants.SEPARATOR_SLASH).trim();
        }
        return result.trim();
    }
}
