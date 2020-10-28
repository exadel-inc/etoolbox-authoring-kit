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
package com.exadel.aem.toolkit.core.handlers.widget;

import java.util.List;

import com.exadel.aem.toolkit.api.handlers.SourceFacade;
import com.exadel.aem.toolkit.api.handlers.TargetFacade;
import com.exadel.aem.toolkit.core.TargetFacadeFacadeImpl;
import com.google.common.collect.ImmutableMap;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.widgets.MultiField;
import com.exadel.aem.toolkit.core.exceptions.InvalidFieldContainerException;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.exadel.aem.toolkit.core.util.PluginXmlContainerUtility;

/**
 * {@link Handler} implementation used to create markup responsible for Granite UI {@code Multifield} widget functionality
 * within the {@code cq:dialog} XML node
 */
public class MultiFieldHandler implements WidgetSetHandler {
    private static final String EMPTY_MULTIFIELD_EXCEPTION_MESSAGE = "No valid fields found in multifield class ";

    /**
     * Processes the user-defined data and writes it to XML entity
     * @param sourceFacade Current {@code SourceFacade} instance
     * @param targetFacade Current {@code TargetFacade} targetFacade
     */
    @Override
    public void accept(SourceFacade sourceFacade, TargetFacade targetFacade) {
        // Define the working @Multifield annotation instance and the multifield type
        MultiField multiField = sourceFacade.adaptTo(MultiField.class);
        Class<?> multifieldType = multiField.field();

        // Modify the targetFacade's attributes for multifield mode
        String name = (String) targetFacade.deleteAttribute(DialogConstants.PN_NAME);

        // Get the filtered members collection for the current container; early return if collection is empty
        List<SourceFacade> members = getContainerSourceFacades(targetFacade, sourceFacade, multifieldType);
        if (members.isEmpty()) {
            PluginRuntime.context().getExceptionHandler().handle(new InvalidFieldContainerException(
                    EMPTY_MULTIFIELD_EXCEPTION_MESSAGE + multifieldType.getName()
            ));
            return;
        }

        // Render separately the multiple-sourceFacade and the single-sourceFacade modes of multifield
        if (members.size() > 1){
            render(members, targetFacade, name);
        } else {
            render(members.get(0), targetFacade);
        }
    }

    /**
     * Renders multiple widgets as XML nodes within the current multifield container
     * @param sourceFacades The collection of {@code SourceFacade} instances to render TouchUI widgets from
     * @param targetFacade Current XML targetFacade
     * @param name The targetFacade's {@code name} attribute
     */
    private void render(List<SourceFacade> sourceFacades, TargetFacade targetFacade, String name) {
        targetFacade.setAttribute(DialogConstants.PN_COMPOSITE, true);
        TargetFacade multifieldContainerElement = new TargetFacadeFacadeImpl(
                DialogConstants.NN_FIELD).setAttributes(ImmutableMap.of(
                        DialogConstants.PN_NAME, name,
                        DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.CONTAINER
                ));
        targetFacade.appendChild(multifieldContainerElement);

        // In case there are multiple sourceFacades in multifield container, their "name" values must not be preceded
        // with "./" which is by default
        // see https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/multifield/index.html#examples
        // That is why we must alter the default name prefix for the ongoing set of sourceFacades
        sourceFacades.forEach(sourceFacade -> {
            String prefix = (String) sourceFacade.fromValueMap(DialogConstants.PN_PREFIX);
            if (prefix.startsWith(DialogConstants.RELATIVE_PATH_PREFIX)) {
                sourceFacade.addToValueMap(DialogConstants.PN_PREFIX, prefix.substring(2));
            }
        });
        PluginXmlContainerUtility.append(multifieldContainerElement, sourceFacades);
    }

    /**
     * Renders a single widget within the current multifield container
     * @param sourceFacade The {@code SourceFacade} instance to render TouchUI widget from
     * @param targetFacade Current {@code TargetFacade} targetFacade
     */
    private void render(SourceFacade sourceFacade, TargetFacade targetFacade) {
        DialogWidget widget = DialogWidgets.fromSourceFacade(sourceFacade);
        if (widget == null) {
            return;
        }
        widget.appendTo(targetFacade, sourceFacade, DialogConstants.NN_FIELD);
    }
}
