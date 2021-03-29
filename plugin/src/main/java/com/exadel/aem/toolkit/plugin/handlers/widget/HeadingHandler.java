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
 * {@code BiConsumer<Source, Target>} implementation used to create markup for Granite UI {@code Heading} widget
 * for a {@code Dialog} or a {@code DesignDialog}
 */
@Handles(Heading.class)
public class HeadingHandler implements Handler {
    private static final String EXCEPTION_MESSAGE = "A non blank string must be specified for this Heading's text";

    /**
     * Processes the user-defined data and writes it to {@link Target}
     * @param source Current {@link Source} instance
     * @param target Current {@link Target} instance
     */
    @SuppressWarnings("deprecation") // Use of Heading#text will be removed after version 2.0.1
    @Override
    public void accept(Source source, Target target) {
        Heading heading = source.adaptTo(Heading.class);
        if (StringUtils.isAllBlank(heading.value(), heading.text())) {
            PluginRuntime.context().getExceptionHandler().handle(new ValidationException(EXCEPTION_MESSAGE));
        }
        target.attribute(CoreConstants.PN_TEXT, StringUtils.defaultIfBlank(heading.value(), heading.text()));
    }
}
