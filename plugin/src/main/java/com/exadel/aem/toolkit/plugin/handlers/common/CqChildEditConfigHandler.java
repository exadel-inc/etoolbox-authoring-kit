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
package com.exadel.aem.toolkit.plugin.handlers.common;

import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableMap;

import com.exadel.aem.toolkit.api.annotations.editconfig.ChildEditConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.handlers.HandlerChains;
import com.exadel.aem.toolkit.plugin.sources.Sources;
import com.exadel.aem.toolkit.plugin.utils.AnnotationUtil;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;

/**
 * Implements {@code BiConsumer} to populate a {@link Target} instance with properties originating from a {@link Source}
 * object that define the {@code cq:childEditConfig} settings node of an AEM component
 */
public class CqChildEditConfigHandler implements BiConsumer<Source, Target> {
    private static final String METHOD_DROP_TARGETS = "dropTargets";
    private static final String METHOD_LISTENERS = "listeners";

    /**
     * Processes data that can be extracted from the given {@code Source} and stores it into the provided {@code Target}
     * @param source {@code Source} object used for data retrieval
     * @param target Resulting {@code Target} object
     */
    @Override
    public void accept(Source source, Target target) {
        ChildEditConfig childEditConfig = source.adaptTo(ChildEditConfig.class);
        target
            .attribute(DialogConstants.PN_PRIMARY_TYPE, DialogConstants.NT_EDIT_CONFIG)
            .attributes(childEditConfig, AnnotationUtil.getPropertyMappingFilter(childEditConfig));
        // Herewith we create a "proxied" @EditConfig object out of the provided @ChildEditConfig
        // with "dropTargets" and "listeners" methods of @EditConfig populated with  @ChildEditConfig values
        EditConfig derivedEditConfig = AnnotationUtil.createInstance(EditConfig.class, ImmutableMap.of(
            METHOD_DROP_TARGETS, childEditConfig.dropTargets(),
            METHOD_LISTENERS, childEditConfig.listeners()
        ));
        HandlerChains.forChildEditConfig().accept(Sources.fromAnnotation(derivedEditConfig), target);
    }
}
