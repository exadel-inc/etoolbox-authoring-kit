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
package com.exadel.aem.toolkit.plugin.handlers.placement.containers;

import java.util.List;
import java.util.function.Function;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.widgets.MultiField;
import com.exadel.aem.toolkit.api.handlers.Handler;
import com.exadel.aem.toolkit.api.handlers.Handles;
import com.exadel.aem.toolkit.api.handlers.MemberSource;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.exceptions.InvalidLayoutException;
import com.exadel.aem.toolkit.plugin.handlers.HandlerChains;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;

/**
 * Implements {@code BiConsumer} to populate a {@link Target} instance with properties originating from a {@link Source}
 * object that define the Granite UI {@code MultiField} widget look and behavior
 */
@Handles(MultiField.class)
public class MultiFieldHandler extends ContainerHandler implements Handler {

    private static final String EMPTY_MULTIFIELD_EXCEPTION_MESSAGE = "No valid fields found in multifield class ";

    /**
     * Processes data that can be extracted from the given {@code Source} and stores it into the provided {@code
     * Target}
     * @param source {@code Source} object used for data retrieval
     * @param target Resulting {@code Target} object
     */
    @Override
    public void accept(Source source, Target target) {
        // Modify attributes of the target for multifield mode
        String name = target.getAttributes().get(DialogConstants.PN_NAME);
        target.getAttributes().remove(DialogConstants.PN_NAME);

        // Get the filtered members' collection for the current container; early return if the collection is empty
        List<Source> members = getAvailableForContainer(source, target);
        if (members.isEmpty()) {
            PluginRuntime.context().getExceptionHandler().handle(new InvalidLayoutException(
                EMPTY_MULTIFIELD_EXCEPTION_MESSAGE + source.adaptTo(MemberSource.class).getValueType().getName()
            ));
            return;
        }

        // Process separately the multiple-source and the single-source modes of multifield
        if (members.size() > 1 || source.adaptTo(MultiField.class).forceComposite()) {
            placeMultiple(source, members, target, name);
        } else {
            placeOne(members.get(0), target);
        }
    }

    /**
     * Places multiple widget sources to the container of the {@code Target} multifield
     * @param source            {@code Source} object used for data retrieval
     * @param multiFieldEntries The collection of {@link Source} instances to become multifield children
     * @param target            Current {@link Target} instance
     * @param name              The {@code name} attribute for the target multifield
     */
    private void placeMultiple(Source source, List<Source> multiFieldEntries, Target target, String name) {
        target.attribute(DialogConstants.PN_COMPOSITE, true);
        Target multifieldContainerElement = target.getOrCreateTarget(DialogConstants.NN_FIELD)
            .attribute(DialogConstants.PN_NAME, name)
            .attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.CONTAINER);
        populateSingleSectionContainer(source, multiFieldEntries, multifieldContainerElement);
    }

    /**
     * Places a single widget source under the {@code Target} multifield
     * @param source The {@link Source} instance to render multifield content from
     * @param target Current {@link Target} instance
     */
    private void placeOne(Source source, Target target) {
        HandlerChains.forMember().accept(source, target.getOrCreateTarget(DialogConstants.NN_FIELD));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Function<MemberSource, List<Class<?>>> getRenderedClassesProvider() {
        return ANNOTATED_MEMBER_TYPE;
    }
}
