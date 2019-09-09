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
package com.exadel.aem.toolkit.core.handlers.editconfig;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
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

public class InplaceEditingHandler implements Handler, BiConsumer<Element, EditConfig> {
    @Override
    public void accept(Element root, EditConfig editConfig) {
        if (ArrayUtils.isEmpty(editConfig.inplaceEditing())
                || (editConfig.inplaceEditing().length == 1 && EditorType.EMPTY.equals(editConfig.inplaceEditing()[0].type()))) {
            return;
        }
        Element inplaceEditingNode = getXmlUtil().createNodeElement(DialogConstants.NN_INPLACE_EDITING, DialogConstants.NT_INPLACE_EDITING_CONFIG);
        getXmlUtil().setAttribute(inplaceEditingNode, DialogConstants.PN_ACTIVE, true);
        root.appendChild(inplaceEditingNode);

        if (editConfig.inplaceEditing().length > 1) {
            inplaceEditingNode.setAttribute(DialogConstants.PN_EDITOR_TYPE, EditorType.HYBRID.toLowerCase());

            Element childEditorsNode = getChildEditorsNode(editConfig);
            inplaceEditingNode.appendChild(childEditorsNode);

            Element configNode = getConfigNode(editConfig);
            inplaceEditingNode.appendChild(configNode);
        } else {
            inplaceEditingNode.setAttribute(DialogConstants.PN_EDITOR_TYPE, editConfig.inplaceEditing()[0].type().toLowerCase());
            Element configNode = getXmlUtil().createNodeElement(DialogConstants.NN_CONFIG);
            populateConfigNode(configNode, editConfig.inplaceEditing()[0]);
            inplaceEditingNode.appendChild(configNode);
        }
    }

    private Element getChildEditorsNode(EditConfig editConfig) {
        Element childEditorsNode = getXmlUtil().createNodeElement(DialogConstants.NN_CHILD_EDITORS);
        Arrays.stream(editConfig.inplaceEditing())
                .map(this::getSingleChildEditorNode)
                .forEach(childEditorsNode::appendChild);
        return childEditorsNode;
    }

    private Element getSingleChildEditorNode(InplaceEditingConfig config) {
        Map<String, String> properties = new ImmutableMap.Builder<String, String>()
                .put(DialogConstants.PN_TITLE, StringUtils.isNotBlank(config.title()) ? config.title() : getConfigName(config))
                .put(DialogConstants.PN_TYPE, config.type().toLowerCase()).build();
        return getXmlUtil().createNodeElement(getConfigName(config), DialogConstants.NT_CHILD_EDITORS_CONFIG, properties);
    }

    private Element getConfigNode(EditConfig editConfig) {
        Element config = getXmlUtil().createNodeElement(DialogConstants.NN_CONFIG);
        for (InplaceEditingConfig childConfig : editConfig.inplaceEditing()) {
            Element childConfigNode = getXmlUtil().createNodeElement(getConfigName(childConfig), DialogConstants.NT_INPLACE_EDITING_CONFIG);
            populateConfigNode(childConfigNode, childConfig);
            config.appendChild(childConfigNode);
        }
        return config;
    }

    private void populateConfigNode(Element element, InplaceEditingConfig inplaceEditingConfig) {
        String propertyName = DialogConstants.RELATIVE_PATH_PREFIX + getXmlUtil().getValidName(inplaceEditingConfig.propertyName());
        String textPropertyName = inplaceEditingConfig.textPropertyName().isEmpty()
                ? propertyName
                : DialogConstants.RELATIVE_PATH_PREFIX + getXmlUtil().getValidName(inplaceEditingConfig.textPropertyName());
        element.setAttribute(DialogConstants.PN_EDIT_ELEMENT_QUERY, inplaceEditingConfig.editElementQuery());
        element.setAttribute(DialogConstants.PN_PROPERTY_NAME, propertyName);
        element.setAttribute(DialogConstants.PN_TEXT_PROPERTY_NAME, textPropertyName);
        populateRteConfig(element, inplaceEditingConfig);
    }

    private void populateRteConfig(Element element, InplaceEditingConfig inplaceEditingConfig) {
        Field referencedRteField = getReferencedRteField(inplaceEditingConfig);
        if (referencedRteField != null && referencedRteField.getAnnotation(RichTextEditor.class) != null) {
            BiConsumer<Element, Field> rteHandler = new RichTextEditorHandler(false);
            new InheritanceHandler(rteHandler).andThen(rteHandler).accept(element, referencedRteField);
            getXmlUtil().mapProperties(element,
                    referencedRteField.getAnnotation(RichTextEditor.class),
                    Collections.singletonList(DialogConstants.PN_USE_FIXED_INLINE_TOOLBAR));
        }
        new RichTextEditorHandler(false).accept(element, inplaceEditingConfig.richTextConfig());
    }

    private static Field getReferencedRteField(InplaceEditingConfig inplaceEditingConfig) {
        if (inplaceEditingConfig.richText().value().equals(Object.class)
                && StringUtils.isBlank(inplaceEditingConfig.richText().field())) {
            // richText attribute not specified, which is a valid case
            return null;
        }
        try {
            return inplaceEditingConfig.richText().value().getDeclaredField(inplaceEditingConfig.richText().field());
        } catch (NoSuchFieldException e) {
            PluginRuntime.context().getExceptionHandler().handle(new ReflectionException(
                    inplaceEditingConfig.richText().value(),
                    inplaceEditingConfig.richText().field()
            ));
        }
        return null;
    }
    private static String getConfigName(InplaceEditingConfig config) {
        if (StringUtils.isNotBlank(config.name())) {
            return config.name();
        }
        return config.propertyName();
    }
}
