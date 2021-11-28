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
package com.exadel.aem.toolkit.plugin.handlers.widgets;

import java.util.List;
import java.util.function.Function;

import com.exadel.aem.toolkit.api.annotations.layouts.FixedColumns;
import com.exadel.aem.toolkit.api.handlers.Handler;
import com.exadel.aem.toolkit.api.handlers.Handles;
import com.exadel.aem.toolkit.api.handlers.MemberSource;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.handlers.containers.ContainerHandler;

/**
 * Implements {@code BiConsumer} to populate a {@link Target} instance with properties originating from a {@link Source}
 * object that define the Granite UI {@code Accordion} dialog widget look and behavior
 */
@Handles(FixedColumns.class)
public class FixedColumnsHandler extends ContainerHandler implements Handler {

    /**
     * Processes data that can be extracted from the given {@code Source} and stores it into the provided {@code Target}
     * @param source {@code Source} object used for data retrieval
     * @param target Resulting {@code Target} object
     */
    @Override
    public void accept(Source source, Target target) {
        if (source.adaptTo(Class.class) != null) {
            // This handler is not used with class-based source objects
            return;
        }
        populateMultiSectionContainer(source, target);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Function<MemberSource, List<Class<?>>> getRenderedClassesProvider() {
        return ANNOTATED_MEMBER_TYPE_AND_REPORTING_CLASS;
    }
}
