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
package com.exadel.aem.toolkit.plugin.handlers.widget;

import java.util.List;
import java.util.function.BiConsumer;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.widgets.MultiField;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.exceptions.InvalidFieldContainerException;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.util.DialogConstants;
import com.exadel.aem.toolkit.plugin.util.PluginContainerUtility;

/**
 * Handler used to prepare data for {@link MultiField} widget rendering
 */
public class MultiFieldHandler implements BiConsumer<Source, Target> {

    private static final String EMPTY_MULTIFIELD_EXCEPTION_MESSAGE = "No valid fields found in multifield class ";

    /**
     * Implements the {@code BiConsumer<Source, Target} pattern to process settings specified by {@link MultiField}
     * and provide data for widget rendering
     * @param source Member that defines a {@code MultiField}
     * @param target Data structure used for rendering
     */
    @Override
    public void accept(Source source, Target target) {
        // Modify attributes of the target for multifield mode
        String name = target.getAttributes().get(DialogConstants.PN_NAME);
        target.getAttributes().remove(DialogConstants.PN_NAME);

        // Get the filtered members collection for the current container; early return if collection is empty
        List<Source> members = PluginContainerUtility.getContainerEntries(source, true);
        if (members.isEmpty()) {
            PluginRuntime.context().getExceptionHandler().handle(new InvalidFieldContainerException(
                    EMPTY_MULTIFIELD_EXCEPTION_MESSAGE + source.getValueType().getName()
            ));
            return;
        }

        // Process separately the multiple-source and the single-source modes of multifield
        if (members.size() > 1){
            process(members, target, name);
        } else {
            process(members.get(0), target);
        }
    }

    /**
     * Places multiple widget sources to the container of the {@code Target} multifield
     * @param sources The collection of {@link Source} instances to become multifield children
     * @param target Current {@link Target} instance
     * @param name The {@code name} attribute fot the target multifield
     */
    private void process(List<Source> sources, Target target, String name) {
        target.attribute(DialogConstants.PN_COMPOSITE, true);
        Target multifieldContainerElement = target.getOrCreate(DialogConstants.NN_FIELD)
                .attribute(DialogConstants.PN_NAME, name)
                .attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.CONTAINER);

        // In case there are multiple sources in multifield container, their "name" values must not be preceded
        // with "./" which is by default
        // see https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/multifield/index.html#examples
        // That is why we must alter the default name prefix for the ongoing set of sources
        PluginContainerUtility.appendToContainer(multifieldContainerElement, sources);
    }

    /**
     * Places a single widget source under the {@code Target} multifield
     * @param source The {@link Source} instance to render multifield content from
     * @param target Current {@link Target} instance
     */
    private void process(Source source, Target target) {
        DialogWidget widget = DialogWidgets.fromSource(source);
        if (widget == null) {
            return;
        }
        widget.appendTo(source, target, DialogConstants.NN_FIELD);
    }
}
