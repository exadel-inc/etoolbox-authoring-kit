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
package com.exadel.aem.toolkit.plugin.handlers.widgets.common;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.adapters.PluginsSetting;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;

/**
 * Implements {@code BiConsumer} to append to a {@link Target} instance the information about plugin(-s) assigned to a
 * widget annotation
 */
public class PluginsHandler implements BiConsumer<Source, Target> {

    /**
     * Processes data that can be extracted from the given {@code Source} and stores it into the provided
     * {@code Target}
     * @param source {@code Source} object used for data retrieval
     * @param target Resulting {@code Target} object
     */
    @Override
    public void accept(Source source, Target target) {
        PluginsSetting pluginsSetting = source.adaptTo(PluginsSetting.class);
        if (ArrayUtils.isEmpty(pluginsSetting.getValue())) {
            return;
        }
        String pluginsText = Arrays.stream(pluginsSetting.getValue())
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.joining(CoreConstants.SEPARATOR_COMMA));
        if (!pluginsText.isEmpty()) {
            target
                .getOrCreateTarget(DialogConstants.NN_GRANITE_DATA)
                .attribute(DialogConstants.PN_PLUGINS, pluginsText);
        }
    }
}
