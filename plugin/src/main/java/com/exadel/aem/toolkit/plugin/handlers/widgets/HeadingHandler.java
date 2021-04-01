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

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.widgets.Heading;
import com.exadel.aem.toolkit.api.handlers.Handler;
import com.exadel.aem.toolkit.api.handlers.Handles;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.exceptions.ValidationException;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;

/**
 * Implements {@code BiConsumer} to populate a {@link Target} instance with properties originating from a {@link Source}
 * object that define the Granite UI {@code Heading} widget look and behavior
 */
@Handles(Heading.class)
public class HeadingHandler implements Handler {
    private static final String EXCEPTION_MESSAGE = "A non-blank string must be specified for this Heading's text";

    /**
     * Processes data that can be extracted from the given {@code Source} and stores it into the provided {@code Target}
     * @param source {@code Source} object used for data retrieval
     * @param target Resulting {@code Target} object
     */
    @Override
    @SuppressWarnings("deprecation") // Use of Heading#text will be removed after version 2.0.2
    public void accept(Source source, Target target) {
        Heading heading = source.adaptTo(Heading.class);
        if (StringUtils.isAllBlank(heading.value(), heading.text())) {
            PluginRuntime.context().getExceptionHandler().handle(new ValidationException(EXCEPTION_MESSAGE));
        }
        target.attribute(CoreConstants.PN_TEXT, StringUtils.defaultIfBlank(heading.value(), heading.text()));
    }
}
