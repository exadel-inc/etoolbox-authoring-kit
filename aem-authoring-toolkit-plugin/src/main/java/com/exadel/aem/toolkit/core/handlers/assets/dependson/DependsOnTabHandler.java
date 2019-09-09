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

public class DependsOnTabHandler implements Handler, BiConsumer<Element, Class<?>> {
    private static final String NO_TABS_EXCEPTION_MESSAGE = "This dialog has no tabs defined";

    @Override
    public void accept(Element element, Class<?> dialogClass) {
        if (dialogClass.isAnnotationPresent(DependsOnTab.class)) {
            handleDependsOnTab(element, dialogClass.getDeclaredAnnotation(DependsOnTab.class));
        } else if (dialogClass.isAnnotationPresent(DependsOnTabConfig.class)) {
            handleDependsOnTabConfig(element, dialogClass.getDeclaredAnnotation(DependsOnTabConfig.class));
        }
    }

    private void handleDependsOnTab(Element element, DependsOnTab value) {
        Element tabItemsNode = getXmlUtil().getDescendantElementNode(element,
                DialogConstants.NN_CONTENT,
                DialogConstants.NN_ITEMS,
                DialogConstants.NN_TABS,
                DialogConstants.NN_ITEMS);
        if (tabItemsNode == null) {
            PluginRuntime.context().getExceptionHandler().handle(new InvalidSettingException(NO_TABS_EXCEPTION_MESSAGE));
            return;
        } else if (StringUtils.isAnyBlank(value.tabTitle(), value.query())) {
            PluginRuntime.context().getExceptionHandler().handle(new ValidationException(DependsOnHandler.EMPTY_VALUES_EXCEPTION_MESSAGE));
            return;
        }
        Element targetTab = getXmlUtil().getChildElementNode(tabItemsNode, getXmlUtil().getValidName(value.tabTitle()));
        getXmlUtil().appendDataAttributes(targetTab, ImmutableMap.of(
                DialogConstants.PN_DEPENDS_ON, value.query(),
                DialogConstants.PN_DEPENDS_ON_ACTION, DependsOnActions.TAB_VISIBILITY
        ));
    }

    private void handleDependsOnTabConfig(Element element, DependsOnTabConfig config) {
        Arrays.stream(config.value()).forEach(val -> handleDependsOnTab(element, val));
    }
}