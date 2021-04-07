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
package com.exadel.aem.toolkit.plugin.handlers.assets.dependson;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.ImmutableMap;

import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnActions;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnTab;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnTabConfig;
import com.exadel.aem.toolkit.api.annotations.meta.Scopes;
import com.exadel.aem.toolkit.api.handlers.Handler;
import com.exadel.aem.toolkit.api.handlers.Handles;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.exceptions.InvalidContainerException;
import com.exadel.aem.toolkit.plugin.exceptions.ValidationException;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;
import com.exadel.aem.toolkit.plugin.utils.NamingUtil;

/**
 * Implements {@code BiConsumer} to populate a {@link Target} instance with properties originating from a {@link Source}
 * object that define relations between Granite container components (Tabs) per {@code DependsOn} specification
 */
@Handles(
    value = {DependsOnTab.class, DependsOnTabConfig.class},
    scope = {Scopes.CQ_DIALOG, Scopes.CQ_DESIGN_DIALOG}
)
public class DependsOnTabHandler implements Handler {

    private static final String TAB_ITEMS_NODE_PATH = String.join(CoreConstants.SEPARATOR_SLASH,
        DialogConstants.NN_CONTENT,
        DialogConstants.NN_ITEMS,
        DialogConstants.NN_TABS,
        DialogConstants.NN_ITEMS);

    /**
     * Processes relevant data that can be extracted from the given {@code Source} and stores it into the provided {@code Target}
     * @param source {@code Source} object used for data retrieval
     * @param target Resulting {@code Target} object
     */
    @Override
    public void accept(Source source, Target target) {
        source.tryAdaptTo(DependsOnTab.class).ifPresent(dependsOnTab -> handleDependsOnTab(dependsOnTab, target));
        source.tryAdaptTo(DependsOnTabConfig.class).ifPresent(dependsOnTabConfig -> handleDependsOnTabConfig(dependsOnTabConfig, target));
    }

    /**
     * Called by {@link DependsOnTabHandler#accept(Source, Target)} to store particular {@code DependsOnTab} value
     * @param value  Current {@link DependsOnTab} value
     * @param target Resulting {@code Target} object
     */
    private void handleDependsOnTab(DependsOnTab value, Target target) {
        if (!target.exists(TAB_ITEMS_NODE_PATH)) {
            PluginRuntime.context().getExceptionHandler().handle(new InvalidContainerException());
            return;
        } else if (StringUtils.isAnyBlank(value.tabTitle(), value.query())) {
            PluginRuntime.context().getExceptionHandler().handle(new ValidationException(DependsOnHandler.EMPTY_VALUES_EXCEPTION_MESSAGE));
            return;
        }
        if (target.exists(TAB_ITEMS_NODE_PATH + "/" + NamingUtil.getValidNodeName(value.tabTitle()))) {
            Map<String, Object> dependsOnAttributes = ImmutableMap.of(
                DialogConstants.PN_DEPENDS_ON, value.query(),
                DialogConstants.PN_DEPENDS_ON_ACTION, DependsOnActions.TAB_VISIBILITY);
            target
                .getTarget(TAB_ITEMS_NODE_PATH  + CoreConstants.SEPARATOR_SLASH  + value.tabTitle())
                .getOrCreateTarget(DialogConstants.NN_GRANITE_DATA)
                .attributes(dependsOnAttributes);
        } else {
            PluginRuntime.context()
                .getExceptionHandler()
                .handle(new InvalidContainerException(value.tabTitle()));
        }
    }

    /**
     * Called by {@link DependsOnTabHandler#accept(Source, Target)} to store particular {@code DependsOnTab} value
     * @param value  Current {@link DependsOnTabConfig} value
     * @param target Resulting {@code Target} object
     */
    private void handleDependsOnTabConfig(DependsOnTabConfig value, Target target) {
        Arrays.stream(value.value()).forEach(val -> handleDependsOnTab(val, target));
    }
}
