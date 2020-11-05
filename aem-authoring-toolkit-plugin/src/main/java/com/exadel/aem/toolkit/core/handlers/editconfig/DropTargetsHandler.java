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
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.exadel.aem.toolkit.api.handlers.Target;

import com.exadel.aem.toolkit.api.annotations.editconfig.DropTargetConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.util.DialogConstants;

/**
 * {@link Handler} implementation for storing {@link DropTargetConfig} arguments to {@code cq:editConfig} XML node
 */
public class DropTargetsHandler implements Handler, BiConsumer<Target, EditConfig> {
    /**
     * Processes the user-defined data and writes it to XML entity
     * @param target XML element
     * @param editConfig {@code EditConfig} annotation instance
     */
    @Override
    public void accept(Target target, EditConfig editConfig) {
        if(editConfig.dropTargets().length == 0){
            return;
        }
        Target dropTargetsElement = target.child(DialogConstants.NN_DROP_TARGETS);
        for (int i = 0; i < editConfig.dropTargets().length; i++) {
            DropTargetConfig dropTargetConfig = editConfig.dropTargets()[i];
            dropTargetsElement.child(dropTargetConfig.nodeName())
                    .attribute(DialogConstants.PN_PRIMARY_TYPE, DialogConstants.NT_DROP_TARGET_CONFIG)
                    .mapProperties(dropTargetConfig)
                    .attribute(DialogConstants.PN_ACCEPT, Arrays.stream(dropTargetConfig.accept()).collect(Collectors.toList()))
                    .attribute(DialogConstants.PN_GROUPS, Arrays.stream(dropTargetConfig.groups()).collect(Collectors.toList()));
        }
    }
}
