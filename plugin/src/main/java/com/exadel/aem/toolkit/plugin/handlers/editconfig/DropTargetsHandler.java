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
import java.util.stream.Collectors;

import com.exadel.aem.toolkit.api.annotations.editconfig.DropTargetConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.utils.AnnotationUtil;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;

/**
 * Implements {@code BiConsumer} to populate a {@link Target} instance with properties originating from a
 * {@link Source} object that define the configuration of drop targets within a Granite UI {@code cq:editConfig}
 * or {@code cq:childEditConfig} node
 */
public class DropTargetsHandler implements BiConsumer<Source, Target> {

    /**
     * Processes data that can be extracted from the given {@code Source} and stores it into the provided {@code Target}
     * @param source {@code Source} object used for data retrieval
     * @param target Resulting {@code Target} object
     */
    @Override
    public void accept(Source source, Target target) {
        EditConfig editConfig = source.adaptTo(EditConfig.class);
        if(editConfig.dropTargets().length == 0){
            return;
        }
        Target dropTargetsElement = target.getOrCreateTarget(DialogConstants.NN_DROP_TARGETS);
        for (int i = 0; i < editConfig.dropTargets().length; i++) {
            DropTargetConfig dropTargetConfig = editConfig.dropTargets()[i];
            dropTargetsElement.getOrCreateTarget(dropTargetConfig.nodeName())
                    .attribute(DialogConstants.PN_PRIMARY_TYPE, DialogConstants.NT_DROP_TARGET_CONFIG)
                    .attributes(dropTargetConfig, AnnotationUtil.getPropertyMappingFilter(dropTargetConfig))
                    .attribute(DialogConstants.PN_ACCEPT, Arrays.stream(dropTargetConfig.accept()).collect(Collectors.toList()).toString())
                    .attribute(DialogConstants.PN_GROUPS, Arrays.stream(dropTargetConfig.groups()).collect(Collectors.toList()).toString());
        }
    }
}
