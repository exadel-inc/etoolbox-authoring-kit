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

import java.util.function.BiConsumer;

import com.exadel.aem.toolkit.api.handlers.Target;
import com.google.common.collect.ImmutableMap;

import com.exadel.aem.toolkit.api.annotations.editconfig.ChildEditConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;
import com.exadel.aem.toolkit.core.util.PluginObjectUtility;

/**
 * Contains methods to generate and trigger the chain of handlers to store {@code cq:editConfig}
 * and {@code cq:childEditConfig} markup
 */
public class EditConfigHandlingHelper {
    private static final String METHOD_DROP_TARGETS = "dropTargets";
    private static final String METHOD_LISTENERS = "listeners";

    private EditConfigHandlingHelper() {}

    /**
     * Builds in-place editing markup for the current component based on the set of component class fields
     * @param editConfig {@link EditConfig} instance
     * @param target {@link Target} representing {@code cq:editConfig} node
     */
    public static void append(EditConfig editConfig, Target target) {
        getEditConfigHandlerChain().accept(editConfig, target);
    }

    /**
     * Builds in-place editing markup for the <i>children</i> of the current component based on the set
     * of component class fields
     * @param childEditConfig {@link ChildEditConfig} instance
     * @param target {@link Target} representing {@code cq:editConfig} node
     */
    public static void append(ChildEditConfig childEditConfig, Target target) {
        // herewith create a "proxied" @EditConfig object out of the provided @ChildEditConfig
        // with "dropTargets" and "listeners" methods of @EditConfig populated with  @ChildEditConfig values
        EditConfig derivedEditConfig = PluginObjectUtility.create(EditConfig.class, ImmutableMap.of(
                METHOD_DROP_TARGETS, childEditConfig.dropTargets(),
                METHOD_LISTENERS, childEditConfig.listeners()
        ));
        getChildEditConfigHandlerChain().accept(derivedEditConfig, target);
    }

    /**
     * Generates the chain of handlers to store {@code cq:editConfig} markup
     * @return {@code BiConsumer<EditConfig, Target>} instance
     */
    private static BiConsumer<EditConfig, Target> getEditConfigHandlerChain() {
        return new PropertiesHandler()
                .andThen(new DropTargetsHandler())
                .andThen(new FormParametersHandler())
                .andThen(new InplaceEditingHandler())
                .andThen(new ListenersHandler());
    }

    /**
     * Generates the chain of handlers to store {@code cq:editConfig} markup
     * @return {@code BiConsumer<EditConfig, Target>} instance
     */
    private static BiConsumer<EditConfig, Target> getChildEditConfigHandlerChain() {
        return new DropTargetsHandler().andThen(new ListenersHandler());
    }
}
