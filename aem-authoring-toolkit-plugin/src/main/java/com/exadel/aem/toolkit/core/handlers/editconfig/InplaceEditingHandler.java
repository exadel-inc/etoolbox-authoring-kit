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
package com.exadel.aem.toolkit.core.handlers.editconfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.BiConsumer;

import com.exadel.aem.toolkit.api.handlers.SourceFacade;
import com.exadel.aem.toolkit.api.handlers.TargetFacade;
import com.exadel.aem.toolkit.core.SourceFacadeImpl;
import com.exadel.aem.toolkit.core.TargetFacadeFacadeImpl;
import com.exadel.aem.toolkit.core.util.NamingUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.ImmutableMap;

import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditorType;
import com.exadel.aem.toolkit.api.annotations.editconfig.InplaceEditingConfig;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RichTextEditor;
import com.exadel.aem.toolkit.core.exceptions.ReflectionException;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.handlers.widget.common.InheritanceHandler;
import com.exadel.aem.toolkit.core.handlers.widget.rte.RichTextEditorHandler;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.DialogConstants;

/**
 * {@link Handler} implementation for storing {@link InplaceEditingConfig} arguments to {@code cq:editConfig} XML node
 */
public class InplaceEditingHandler implements Handler, BiConsumer<TargetFacade, EditConfig> {
    /**
     * Processes the user-defined data and writes it to XML entity
     * @param root XML element
     * @param editConfig {@code EditConfig} annotation instance
     */
    @Override
    public void accept(TargetFacade root, EditConfig editConfig) {
        if (ArrayUtils.isEmpty(editConfig.inplaceEditing())
                || (editConfig.inplaceEditing().length == 1 && EditorType.EMPTY.equals(editConfig.inplaceEditing()[0].type()))) {
            return;
        }
        TargetFacade inplaceEditingNode = new TargetFacadeFacadeImpl(DialogConstants.NN_INPLACE_EDITING)
                .setAttribute(DialogConstants.PN_PRIMARY_TYPE, DialogConstants.NT_INPLACE_EDITING_CONFIG)
                .setAttribute(DialogConstants.PN_ACTIVE, true);
        root.appendChild(inplaceEditingNode);

        if (editConfig.inplaceEditing().length > 1) {
            inplaceEditingNode.setAttribute(DialogConstants.PN_EDITOR_TYPE, EditorType.HYBRID.toLowerCase());

            TargetFacade childEditorsNode = getChildEditorsNode(editConfig);
            inplaceEditingNode.appendChild(childEditorsNode);

            TargetFacade configNode = getConfigNode(editConfig);
            inplaceEditingNode.appendChild(configNode);
        } else {
            inplaceEditingNode.setAttribute(DialogConstants.PN_EDITOR_TYPE, editConfig.inplaceEditing()[0].type().toLowerCase());
            TargetFacade configNode = new TargetFacadeFacadeImpl(DialogConstants.NN_CONFIG);
            populateConfigNode(configNode, editConfig.inplaceEditing()[0]);
            inplaceEditingNode.appendChild(configNode);
        }
    }

    /**
     * Generates and returns new {@code cq:childEditors} XML node containing one or more {@code cq:ChildEditorConfig} subnodes
     * @param config {@link EditConfig} annotation instance
     * @return XML {@code Element}
     */
    private TargetFacade getChildEditorsNode(EditConfig config) {
        TargetFacade childEditorsNode = new TargetFacadeFacadeImpl(DialogConstants.NN_CHILD_EDITORS);
        Arrays.stream(config.inplaceEditing())
                .map(this::getSingleChildEditorNode)
                .forEach(childEditorsNode::appendChild);
        return childEditorsNode;
    }

    /**
     * Generates and returns new {@code cq:ChildEditorConfig} XML node
     * @param config {@link InplaceEditingConfig} annotation instance
     * @return XML {@code Element}
     */
    private TargetFacade getSingleChildEditorNode(InplaceEditingConfig config) {
        Map<String, String> properties = new ImmutableMap.Builder<String, String>()
                .put(DialogConstants.PN_TITLE, StringUtils.isNotBlank(config.title()) ? config.title() : getConfigName(config))
                .put(DialogConstants.PN_TYPE, config.type().toLowerCase()).build();
        return new TargetFacadeFacadeImpl(getConfigName(config))
            .setAttribute(DialogConstants.PN_PRIMARY_TYPE, DialogConstants.NT_CHILD_EDITORS_CONFIG)
            .setAttributes(properties);
    }

    /**
     * Generates and returns new {@code config} XML node  containing one or more {@code cq:InplaceEditingConfig} subnodes
     * for use with {@code cq:inplaceEditing} markup
     * @param config {@link EditConfig} annotation instance
     * @return XML {@code Element}
     */
    private TargetFacade getConfigNode(EditConfig config) {
        TargetFacade configNode = new TargetFacadeFacadeImpl(DialogConstants.NN_CONFIG);
        for (InplaceEditingConfig childConfig : config.inplaceEditing()) {
            TargetFacade childConfigNode = new TargetFacadeFacadeImpl(getConfigName(childConfig))
                    .setAttribute(DialogConstants.PN_PRIMARY_TYPE, DialogConstants.NT_INPLACE_EDITING_CONFIG);
            populateConfigNode(childConfigNode, childConfig);
            configNode.appendChild(childConfigNode);
        }
        return configNode;
    }

    /**
     * Sets necessary attributes to {@code cq:InplaceEditingConfig} XML node
     * @param targetFacade {@code Element} representing config node
     * @param config {@link InplaceEditingConfig} annotation instance
     */
    private void populateConfigNode(TargetFacade targetFacade, InplaceEditingConfig config) {
        String propertyName = getValidPropertyName(config.propertyName());
        String textPropertyName = config.textPropertyName().isEmpty()
                ? propertyName
                : getValidPropertyName(config.textPropertyName());
        targetFacade.setAttribute(DialogConstants.PN_EDIT_ELEMENT_QUERY, config.editElementQuery());
        targetFacade.setAttribute(DialogConstants.PN_PROPERTY_NAME, propertyName);
        targetFacade.setAttribute(DialogConstants.PN_TEXT_PROPERTY_NAME, textPropertyName);
        populateRteConfig(targetFacade, config);
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
     * Plants necessary attributes and subnodes related to in-place rich text editor to {@code cq:InplaceEditingConfig} XML node
     * @param targetFacade {@code Element} representing config node
     * @param config {@link InplaceEditingConfig} annotation instance
     */
    private void populateRteConfig(TargetFacade targetFacade, InplaceEditingConfig config) {
        SourceFacade referencedRteField = getReferencedRteField(config);
        if (referencedRteField != null && referencedRteField.adaptTo(RichTextEditor.class) != null) {
            BiConsumer<SourceFacade, TargetFacade> rteHandler = new RichTextEditorHandler(false);
            new InheritanceHandler(rteHandler).andThen(rteHandler).accept(referencedRteField, targetFacade);
            targetFacade.mapProperties(referencedRteField.adaptTo(RichTextEditor.class),
                    Collections.singletonList(DialogConstants.PN_USE_FIXED_INLINE_TOOLBAR));
        }
        //new RichTextEditorHandler(false).accept(config.richTextConfig(), targetFacade);
    }

    /**
     * Retrieves a component class field referenced by this {@link InplaceEditingConfig}
     * via {@link com.exadel.aem.toolkit.api.annotations.widgets.Extends}-typed attribute
     * @param config {@link InplaceEditingConfig} annotation instance
     * @return {@code Field} instance
     */
    private static SourceFacade getReferencedRteField(InplaceEditingConfig config) {
        if (config.richText().value().equals(Object.class)
                && StringUtils.isBlank(config.richText().field())) {
            // richText attribute not specified, which is a valid case
            return null;
        }
        try {
            return new SourceFacadeImpl(config.richText().value().getDeclaredField(config.richText().field()));
        } catch (NoSuchFieldException e) {
            PluginRuntime.context().getExceptionHandler().handle(new ReflectionException(
                    config.richText().value(),
                    config.richText().field()
            ));
        }
        return null;
    }

    /**
     * Gets non-blank string that would stand for the name of the current config
     * @param config {@link InplaceEditingConfig} annotation instance
     * @return String value
     */
    private static String getConfigName(InplaceEditingConfig config) {
        if (StringUtils.isNotBlank(config.name())) {
            return config.name();
        }
        return config.propertyName();
    }
}
