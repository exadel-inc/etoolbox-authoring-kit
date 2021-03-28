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

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableMap;

import com.exadel.aem.toolkit.api.annotations.meta.Scopes;
import com.exadel.aem.toolkit.api.handlers.Handler;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.handlers.common.ComponentHandler;
import com.exadel.aem.toolkit.plugin.handlers.common.CqChildEditConfigHandler;
import com.exadel.aem.toolkit.plugin.handlers.common.CqDialogHandler;
import com.exadel.aem.toolkit.plugin.handlers.common.CqEditConfigHandler;
import com.exadel.aem.toolkit.plugin.handlers.common.CqHtmlTagHandler;
import com.exadel.aem.toolkit.plugin.handlers.common.CustomHandlingHandler;
import com.exadel.aem.toolkit.plugin.handlers.common.PropertyMappingHandler;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;

/**
 * Serves as the source for handler chains used to process user-specified data and prepare structures that are further
 * rendered into Granite entities
 * @see Source
 * @see Target
 */
public class HandlerChains {

    // Generic handlers
    private static final BiConsumer<Source, Target> PROPERTY_MAPPING_HANDLER = new PropertyMappingHandler();
    private static final BiConsumer<Source, Target> CUSTOM_HANDLING_HANDLER = new CustomHandlingHandler();

    // Scope-specific handlers
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

    private static final BiConsumer<Source, Target> NOOP_HANDLER = (source, target) -> {};

    /**
     * Default (instantiation-restricting) constructor
     */
    private HandlerChains() {
    }

    /**
     * Retrieves a handler conveyor for rendering a default scope-specific UI, such as a dialog, a design dialog, or an
     * in-place editing config
     * @param scope Non-blank string representing the scope
     * @return {@code BiConsumer<Source, Target>} instance representing the conveyor
     */
    public static BiConsumer<Source, Target> forScope(String scope) {
        BiConsumer<Source, Target> uiHandler = UI_HANDLERS.getOrDefault(scope, NOOP_HANDLER);
        return PROPERTY_MAPPING_HANDLER
            .andThen(uiHandler)
            .andThen(CUSTOM_HANDLING_HANDLER);
    }

    public static BiConsumer<Source, Target> forMember(Source source, String scope) {
        Handler widgetHandler = PluginRuntime.context().getReflection().getHandlers(scope, source.adaptTo(Annotation[].class))
            .stream()
            .reduce((first, second) -> (Handler) first.andThen(second))
            .orElse((Handler) NOOP_HANDLER);
        return PROPERTY_MAPPING_HANDLER
            .andThen(widgetHandler)
            .andThen(CUSTOM_HANDLING_HANDLER);
    }

}
