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

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.editconfig.DropTargetConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.util.DialogConstants;

public class DropTargetsHandler implements Handler, BiConsumer<Element, EditConfig> {
    @Override
    public void accept(Element root, EditConfig editConfig) {
        if(editConfig.dropTargets().length == 0){
            return;
        }
        Element dropTargetsElement = getXmlUtil().createNodeElement(DialogConstants.NN_DROP_TARGETS);
        root.appendChild(dropTargetsElement);
        for (int i = 0; i < editConfig.dropTargets().length; i++) {
            DropTargetConfig dropTargetConfig = editConfig.dropTargets()[i];
            Element currentConfig = getXmlUtil().createNodeElement(dropTargetConfig.nodeName());
            dropTargetsElement.appendChild(currentConfig);
            getXmlUtil().mapProperties(currentConfig, dropTargetConfig);
            List<String> accept = Arrays.stream(dropTargetConfig.accept()).collect(Collectors.toList());
            getXmlUtil().setAttribute(currentConfig, DialogConstants.PN_ACCEPT, accept);
            List<String> groups = Arrays.stream(dropTargetConfig.groups()).collect(Collectors.toList());
            getXmlUtil().setAttribute(currentConfig, DialogConstants.PN_GROUPS, groups);
        }
    }
}
