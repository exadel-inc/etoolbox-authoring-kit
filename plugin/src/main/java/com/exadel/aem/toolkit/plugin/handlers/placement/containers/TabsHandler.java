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

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.layouts.Tabs;
import com.exadel.aem.toolkit.api.handlers.Handler;
import com.exadel.aem.toolkit.api.handlers.Handles;
import com.exadel.aem.toolkit.api.handlers.MemberSource;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;

/**
 * Implements {@code BiConsumer} to populate a {@link Target} instance with properties originating from a {@link Source}
 * object that define the Granite UI {@code Tabs} dialog widget look and behavior
 */
@Handles(Tabs.class)
public class TabsHandler extends ContainerHandler implements Handler {
    private static final Predicate<Method> WIDGET_PROPERTIES_FILTER = method ->
        !StringUtils.equalsAny(
            method.getName(),
            CoreConstants.PN_TYPE,
            DialogConstants.PN_PADDING);

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
        target.attributes(source.adaptTo(Tabs.class), WIDGET_PROPERTIES_FILTER); // We do not use the auto-mapping facility
        // because @Tabs can be used class-level and should not mess with "true" auto-mapped class annotations
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
