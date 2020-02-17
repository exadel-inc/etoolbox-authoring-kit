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

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import com.google.common.collect.ImmutableMap;

import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnActions;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnTab;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnTabConfig;
import com.exadel.aem.toolkit.core.exceptions.InvalidSettingException;
import com.exadel.aem.toolkit.core.exceptions.ValidationException;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.DialogConstants;

/**
 * {@link Handler} implementation used to create markup responsible for AEM Authoring Toolkit {@code DependsOn} functionality
 */
public class DependsOnTabHandler implements Handler, BiConsumer<Element, Class<?>> {
    private static final String NO_TABS_EXCEPTION_MESSAGE = "This dialog has no tabs defined";

    /**
     * Processes the user-defined data and writes it to XML entity
     * @param element Current XML element
     * @param dialogClass {@code Class} object representing the tab-defining class
     */
    @Override
    public void accept(Element element, Class<?> dialogClass) {
        if (dialogClass.isAnnotationPresent(DependsOnTab.class)) {
            handleDependsOnTab(element, dialogClass.getDeclaredAnnotation(DependsOnTab.class));
        } else if (dialogClass.isAnnotationPresent(DependsOnTabConfig.class)) {
            handleDependsOnTabConfig(element, dialogClass.getDeclaredAnnotation(DependsOnTabConfig.class));
        }
    }

    /**
     * Called by {@link DependsOnTabHandler#accept(Element, Class)} to store particular {@code DependsOnTab} value
     * in XML markup
     * @param element Current XML element
     * @param value Current {@link DependsOnTab} value
     */
    private void handleDependsOnTab(Element element, DependsOnTab value) {
        Element tabItemsNode = getXmlUtil().getChildElement(element, String.join(DialogConstants.PATH_SEPARATOR,
                DialogConstants.NN_CONTENT,
                DialogConstants.NN_ITEMS,
                DialogConstants.NN_TABS,
                DialogConstants.NN_ITEMS));
        if (tabItemsNode == null) {
            PluginRuntime.context().getExceptionHandler().handle(new InvalidSettingException(NO_TABS_EXCEPTION_MESSAGE));
            return;
        } else if (StringUtils.isAnyBlank(value.tabTitle(), value.query())) {
            PluginRuntime.context().getExceptionHandler().handle(new ValidationException(DependsOnHandler.EMPTY_VALUES_EXCEPTION_MESSAGE));
            return;
        }
        Element targetTab = getXmlUtil().getChildElement(tabItemsNode, getXmlUtil().getValidName(value.tabTitle()));
        if (targetTab != null) {
            getXmlUtil().appendDataAttributes(targetTab, ImmutableMap.of(
                    DialogConstants.PN_DEPENDS_ON, value.query(),
                    DialogConstants.PN_DEPENDS_ON_ACTION, DependsOnActions.TAB_VISIBILITY
            ));
        } else {
            PluginRuntime.context().getExceptionHandler()
                    .handle(new InvalidSettingException(String.format("Does not exist tab \"%s\"", value.tabTitle())));
        }
    }

    /**
     * Called by {@link DependsOnTabHandler#accept(Element, Class)} to store particular {@code DependsOnTab} value
     * in XML markup
     * @param element Current XML element
     * @param value Current {@link DependsOnTabConfig} value
     */
    private void handleDependsOnTabConfig(Element element, DependsOnTabConfig value) {
        Arrays.stream(value.value()).forEach(val -> handleDependsOnTab(element, val));
    }
}