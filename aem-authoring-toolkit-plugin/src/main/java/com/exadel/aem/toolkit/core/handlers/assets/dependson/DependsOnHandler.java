package com.exadel.aem.toolkit.core.handlers.assets.dependson;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOn;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnActions;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnConfig;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnRef;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnRefTypes;
import com.exadel.aem.toolkit.core.exceptions.ValidationException;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.DialogConstants;

public class DependsOnHandler implements Handler, BiConsumer<Element, Field> {
    static final String EMPTY_VALUES_EXCEPTION_MESSAGE = "Non-empty string values required for DependsOn params";

    @Override
    public void accept(Element element, Field field) {
        if (field.isAnnotationPresent(DependsOn.class)) {
            handleDependsOn(element, field.getDeclaredAnnotation(DependsOn.class));
        } else if (field.isAnnotationPresent(DependsOnConfig.class)) {
            handleDependsOnConfig(element, field.getDeclaredAnnotation(DependsOnConfig.class));
        }
        handleDependsOnRefValue(element, field.getDeclaredAnnotation(DependsOnRef.class));
    }

    private void handleDependsOn(Element element, DependsOn value) {
        if (StringUtils.isAnyBlank(value.query(), value.action())) {
            PluginRuntime.context().getExceptionHandler().handle(new ValidationException(EMPTY_VALUES_EXCEPTION_MESSAGE));
            return;
        }
        Map<String, String> valueMap = Maps.newHashMap();
        valueMap.put(DialogConstants.PN_DEPENDS_ON, value.query());
        if (!value.action().equals(DependsOnActions.VISIBILITY)) {
            valueMap.put(DialogConstants.PN_DEPENDS_ON_ACTION, value.action());
        }
        getXmlUtil().appendDataAttributes(element, valueMap);
    }

    private void handleDependsOnConfig(Element element, DependsOnConfig config) {
        String queries = Arrays.stream(config.value())
                .filter(conf -> StringUtils.isNoneBlank(conf.action(), conf.query()))
                .map(DependsOn::query)
                .collect(Collectors.joining(DialogConstants.VALUE_SEPARATOR));
        String actions = Arrays.stream(config.value())
                .filter(conf -> StringUtils.isNoneBlank(conf.action(), conf.query()))
                .map(DependsOn::action)
                .collect(Collectors.joining(DialogConstants.VALUE_SEPARATOR));
        if (StringUtils.isAllEmpty(queries, actions))  {
            return;
        }
        if (StringUtils.isAnyBlank(actions, queries)) {
            PluginRuntime.context().getExceptionHandler().handle(new ValidationException(EMPTY_VALUES_EXCEPTION_MESSAGE));
            return;
        }
        getXmlUtil().appendDataAttributes(element, ImmutableMap.of(
                DialogConstants.PN_DEPENDS_ON, queries,
                DialogConstants.PN_DEPENDS_ON_ACTION, actions
        ));
    }

    private void handleDependsOnRefValue(Element element, DependsOnRef value) {
        if (value == null) {
            return;
        }
        if (StringUtils.isBlank(value.name())) {
            PluginRuntime.context().getExceptionHandler().handle(new ValidationException(EMPTY_VALUES_EXCEPTION_MESSAGE));
            return;
        }

        Map<String, String> valueMap = Maps.newHashMap();
        valueMap.put(DialogConstants.PN_DEPENDS_ON_REF, value.name());
        if (!value.type().toString().equals(DependsOnRefTypes.AUTO.toString())) {
            valueMap.put(DialogConstants.PN_DEPENDS_ON_REFTYPE, value.type().toString().toLowerCase());
        }
        getXmlUtil().appendDataAttributes(element, valueMap);
    }
}
