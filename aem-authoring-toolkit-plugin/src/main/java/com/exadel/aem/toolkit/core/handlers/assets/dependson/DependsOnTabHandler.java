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

package com.exadel.aem.toolkit.core.handlers.assets.dependson;

import java.util.Arrays;
import java.util.function.BiConsumer;

import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.core.util.PluginXmlUtility;
import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.ImmutableMap;

import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnActions;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnTab;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnTabConfig;
import com.exadel.aem.toolkit.core.exceptions.InvalidTabException;
import com.exadel.aem.toolkit.core.exceptions.ValidationException;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.DialogConstants;

/**
 * {@code BiConsumer<Source, Target>} implementation used to create markup responsible for AEM Authoring Toolkit {@code DependsOn} functionality
 */
public class DependsOnTabHandler implements BiConsumer<Target, Class<?>> {

    /**
     * Processes the user-defined data and writes it to {@link Target}
     * @param target Current {@link Target} instance
     * @param dialogClass {@code Class} object representing the tab-defining class
     */
    @Override
    public void accept(Target target, Class<?> dialogClass) {
        if (dialogClass.isAnnotationPresent(DependsOnTab.class)) {
            handleDependsOnTab(dialogClass.getDeclaredAnnotation(DependsOnTab.class), target);
        } else if (dialogClass.isAnnotationPresent(DependsOnTabConfig.class)) {
            handleDependsOnTabConfig(dialogClass.getDeclaredAnnotation(DependsOnTabConfig.class), target);
        }
    }

    /**
     * Called by {@link DependsOnTabHandler#accept(Target, Class)} to store particular {@code DependsOnTab} value
     * in XML markup
     * @param value Current {@link DependsOnTab} value
     * @param target Current {@link Target} instance
     */
    private void handleDependsOnTab(DependsOnTab value, Target target) {
        Target tabItemsNode = target.getChild(String.join(DialogConstants.PATH_SEPARATOR,
                DialogConstants.NN_CONTENT,
                DialogConstants.NN_ITEMS,
                DialogConstants.NN_TABS,
                DialogConstants.NN_ITEMS));
        if (tabItemsNode == null) {
            PluginRuntime.context().getExceptionHandler().handle(new InvalidTabException());
            return;
        } else if (StringUtils.isAnyBlank(value.tabTitle(), value.query())) {
            PluginRuntime.context().getExceptionHandler().handle(new ValidationException(DependsOnHandler.EMPTY_VALUES_EXCEPTION_MESSAGE));
            return;
        }
        Target targetTab = tabItemsNode.getChild(value.tabTitle());
        if (targetTab != null) {
            PluginXmlUtility.appendDataAttributes(targetTab, ImmutableMap.of(
                    DialogConstants.PN_DEPENDS_ON, value.query(),
                    DialogConstants.PN_DEPENDS_ON_ACTION, DependsOnActions.TAB_VISIBILITY
            ));
        } else {
            PluginRuntime.context().getExceptionHandler()
                    .handle(new InvalidTabException(value.tabTitle()));
        }
    }

    /**
     * Called by {@link DependsOnTabHandler#accept(Target, Class)} to store particular {@code DependsOnTab} value
     * in XML markup
     * @param value Current {@link DependsOnTabConfig} value
     * @param target Current {@link Target} instance
     */
    private void handleDependsOnTabConfig(DependsOnTabConfig value, Target target) {
        Arrays.stream(value.value()).forEach(val -> handleDependsOnTab(val, target));
    }
}