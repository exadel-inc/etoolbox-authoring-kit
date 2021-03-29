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
package com.exadel.aem.toolkit.plugin.handlers;

import java.util.Map;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableMap;

import com.exadel.aem.toolkit.api.annotations.meta.Scopes;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.handlers.assets.dependson.DependsOnHandler;
import com.exadel.aem.toolkit.plugin.handlers.common.CasualAnnotationsHandler;
import com.exadel.aem.toolkit.plugin.handlers.common.ComponentHandler;
import com.exadel.aem.toolkit.plugin.handlers.common.CqChildEditConfigHandler;
import com.exadel.aem.toolkit.plugin.handlers.common.CqDialogHandler;
import com.exadel.aem.toolkit.plugin.handlers.common.CqEditConfigHandler;
import com.exadel.aem.toolkit.plugin.handlers.common.CqHtmlTagHandler;
import com.exadel.aem.toolkit.plugin.handlers.common.PropertyMappingHandler;
import com.exadel.aem.toolkit.plugin.handlers.editconfig.DropTargetsHandler;
import com.exadel.aem.toolkit.plugin.handlers.editconfig.FormParametersHandler;
import com.exadel.aem.toolkit.plugin.handlers.editconfig.InplaceEditingHandler;
import com.exadel.aem.toolkit.plugin.handlers.editconfig.ListenersHandler;
import com.exadel.aem.toolkit.plugin.handlers.widgets.common.AttributeAnnotationHandler;
import com.exadel.aem.toolkit.plugin.handlers.widgets.common.DialogFieldAnnotationHandler;
import com.exadel.aem.toolkit.plugin.handlers.widgets.common.InheritanceHandler;
import com.exadel.aem.toolkit.plugin.handlers.widgets.common.MultipleAnnotationHandler;
import com.exadel.aem.toolkit.plugin.handlers.widgets.common.PropertyAnnotationHandler;
import com.exadel.aem.toolkit.plugin.handlers.widgets.common.ResourceTypeHandler;

/**
 * Serves as the source for handler chains used to process user-specified data and prepare structures that are further
 * rendered into Granite entities
 * @see Source
 * @see Target
 */
public class HandlerChains {

    // Generic handlers
    private static final BiConsumer<Source, Target> CASUAL_ANNOTATIONS_HANDLER = new CasualAnnotationsHandler();
    private static final BiConsumer<Source, Target> PROPERTY_MAPPING_HANDLER = new PropertyMappingHandler();

    // UI-specific handlers
    private static final BiConsumer<Source, Target> CHILD_EDIT_CONFIG_HANDLER = new CqChildEditConfigHandler();
    private static final BiConsumer<Source, Target> COMPONENT_HANDLER = new ComponentHandler();
    private static final BiConsumer<Source, Target> DIALOG_HANDLER = new CqDialogHandler();
    private static final BiConsumer<Source, Target> EDIT_CONFIG_HANDLER = new CqEditConfigHandler();
    private static final BiConsumer<Source, Target> HTML_TAG_HANDLER = new CqHtmlTagHandler();
    private static final Map<String, BiConsumer<Source, Target>> UI_HANDLERS = ImmutableMap.<String, BiConsumer<Source, Target>>builder()
        .put(Scopes.COMPONENT, COMPONENT_HANDLER)
        .put(Scopes.CQ_DIALOG, DIALOG_HANDLER)
        .put(Scopes.CQ_DESIGN_DIALOG, DIALOG_HANDLER)
        .put(Scopes.CQ_EDIT_CONFIG, EDIT_CONFIG_HANDLER)
        .put(Scopes.CQ_CHILD_EDIT_CONFIG, CHILD_EDIT_CONFIG_HANDLER)
        .put(Scopes.CQ_HTML_TAG, HTML_TAG_HANDLER)
        .build();

    // Widget-specific handlers
    private static final BiConsumer<Source, Target> ATTRIBUTE_ANNOTATION_HANDLER = new AttributeAnnotationHandler();
    private static final BiConsumer<Source, Target> DEPENDS_ON_HANDLER = new DependsOnHandler();
    private static final BiConsumer<Source, Target> DIALOG_FIELD_HANDLER = new DialogFieldAnnotationHandler();
    private static final BiConsumer<Source, Target> MULTIPLE_HANDLER = new MultipleAnnotationHandler();
    private static final BiConsumer<Source, Target> PROPERTY_ANNOTATION_HANDLER = new PropertyAnnotationHandler();
    private static final BiConsumer<Source, Target> RESOURCE_TYPE_HANDLER = new ResourceTypeHandler();

    // Complete widget chain
    private static final BiConsumer<Source, Target> MEMBER_HANDLER_CHAIN =
        RESOURCE_TYPE_HANDLER
        .andThen(PROPERTY_MAPPING_HANDLER)
        .andThen(ATTRIBUTE_ANNOTATION_HANDLER)
        .andThen(DIALOG_FIELD_HANDLER)
        .andThen(CASUAL_ANNOTATIONS_HANDLER)
        .andThen(DEPENDS_ON_HANDLER)
        .andThen(PROPERTY_ANNOTATION_HANDLER)
        .andThen(MULTIPLE_HANDLER);
    private static final BiConsumer<Source, Target> MEMBER_INHERITANCE_HANDLER_CHAIN =
        new InheritanceHandler(MEMBER_HANDLER_CHAIN)
            .andThen(MEMBER_HANDLER_CHAIN);

    // EditConfig handlers
    private static final BiConsumer<Source, Target> EDIT_CONFIG_DROP_TARGETS_HANDLER = new DropTargetsHandler();
    private static final BiConsumer<Source, Target> EDIT_CONFIG_FORM_PARAMS_HANDLER = new FormParametersHandler();
    private static final BiConsumer<Source, Target> EDIT_CONFIG_INPLACE_HANDLER = new InplaceEditingHandler();
    private static final BiConsumer<Source, Target> EDIT_CONFIG_LISTENERS_HANDLER = new ListenersHandler();

    // Complete editConfig chains
    private static final BiConsumer<Source, Target> EDIT_CONFIG_HANDLER_CHAIN =
        PROPERTY_MAPPING_HANDLER
        .andThen(EDIT_CONFIG_DROP_TARGETS_HANDLER)
        .andThen(EDIT_CONFIG_FORM_PARAMS_HANDLER)
        .andThen(EDIT_CONFIG_INPLACE_HANDLER)
        .andThen(EDIT_CONFIG_LISTENERS_HANDLER);
    private static final BiConsumer<Source, Target> CHILD_EDIT_CONFIG_HANDLER_CHAIN =
        EDIT_CONFIG_DROP_TARGETS_HANDLER
        .andThen(EDIT_CONFIG_LISTENERS_HANDLER);

    private static final BiConsumer<Source, Target> NOOP_HANDLER = (source, target) -> {};

    /**
     * Default (instantiation-restricting) constructor
     */
    private HandlerChains() {
    }

    /**
     * Retrieves a handler conveyor for rendering a scope-specific UI, such as a dialog, a design dialog, or an
     * in-place editing config
     * @param scope Non-blank string representing the scope
     * @return {@code BiConsumer<Source, Target>} instance representing the conveyor
     */
    public static BiConsumer<Source, Target> forScope(String scope) {
        BiConsumer<Source, Target> uiHandler = UI_HANDLERS.getOrDefault(scope, NOOP_HANDLER);
        return PROPERTY_MAPPING_HANDLER
            .andThen(uiHandler)
            .andThen(CASUAL_ANNOTATIONS_HANDLER);
    }

    /**
     * Retrieves a handler conveyor for rendering a member-specific UI (usually a dialog field widget)
     * @return {@code BiConsumer<Source, Target>} instance representing the conveyor
     */
    public static BiConsumer<Source, Target> forMember() {
        return MEMBER_INHERITANCE_HANDLER_CHAIN;
    }

    /**
     * Retrieves a handler conveyor for rendering {@code editConfig}
     * @return {@code BiConsumer<Source, Target>} instance representing the conveyor
     */
    public static BiConsumer<Source, Target> forEditConfig() {
        return EDIT_CONFIG_HANDLER_CHAIN;
    }

    /**
     * Retrieves a handler conveyor for rendering {@code editConfig}
     * @return {@code BiConsumer<Source, Target>} instance representing the conveyor
     */
    public static BiConsumer<Source, Target> forChildEditConfig() {
        return CHILD_EDIT_CONFIG_HANDLER_CHAIN;
    }
}
