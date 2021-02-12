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
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.util.DialogConstants;
import com.exadel.aem.toolkit.plugin.util.PluginAnnotationUtility;

/**
 * {@code BiConsumer<Target, EditConfig>} implementation for storing {@link DropTargetConfig} arguments to {@code cq:editConfig} node
 */
public class DropTargetsHandler implements BiConsumer<EditConfig, Target> {
    /**
     * Processes the user-defined data and writes it to {@code Target}
     * @param editConfig {@code EditConfig} annotation instance
     * @param target Current {@link Target} instance
     */
    @Override
    public void accept(EditConfig editConfig, Target target) {
        if(editConfig.dropTargets().length == 0){
            return;
        }
        Target dropTargetsElement = target.getOrCreateTarget(DialogConstants.NN_DROP_TARGETS);
        for (int i = 0; i < editConfig.dropTargets().length; i++) {
            DropTargetConfig dropTargetConfig = editConfig.dropTargets()[i];
            dropTargetsElement.getOrCreateTarget(dropTargetConfig.nodeName())
                    .attribute(DialogConstants.PN_PRIMARY_TYPE, DialogConstants.NT_DROP_TARGET_CONFIG)
                    .attributes(dropTargetConfig, PluginAnnotationUtility.getPropertyMappingFilter(dropTargetConfig))
                    .attribute(DialogConstants.PN_ACCEPT, Arrays.stream(dropTargetConfig.accept()).collect(Collectors.toList()).toString())
                    .attribute(DialogConstants.PN_GROUPS, Arrays.stream(dropTargetConfig.groups()).collect(Collectors.toList()).toString());
        }
    }
}
