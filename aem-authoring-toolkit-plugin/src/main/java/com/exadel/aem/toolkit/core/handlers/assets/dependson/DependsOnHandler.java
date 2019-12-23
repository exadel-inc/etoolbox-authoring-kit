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

import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOn;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnConfig;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnParam;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnRef;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnRefTypes;
import com.exadel.aem.toolkit.core.exceptions.ValidationException;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * {@link Handler} implementation used to create markup responsible for AEM Authoring Toolkit {@code DependsOn} functionality
 */
public class DependsOnHandler implements Handler, BiConsumer<Element, Field> {

    static final String EMPTY_VALUES_EXCEPTION_MESSAGE = "Non-empty string values required for DependsOn params";

    private static final String TERM_SEPARATOR = "-";

    /**
     * Processes the user-defined data and writes it to XML entity
     * @param element Current XML element
     * @param field Current {@code Field} instance
     */
    @Override
    public void accept(Element element, Field field) {
        if (field.isAnnotationPresent(DependsOn.class)) {
            handleDependsOn(element, field.getDeclaredAnnotation(DependsOn.class));
        } else if (field.isAnnotationPresent(DependsOnConfig.class)) {
            handleDependsOnConfig(element, field.getDeclaredAnnotation(DependsOnConfig.class));
        }
        handleDependsOnRefValue(element, field.getDeclaredAnnotation(DependsOnRef.class));
    }

    /**
     * Called by {@link DependsOnHandler#accept(Element, Field)} to store particular {@code DependsOn} value in XML markup
     * @param element Current XML element
     * @param value Current {@link DependsOn} value
     */
    private void handleDependsOn(Element element, DependsOn value) {
        if (StringUtils.isAnyBlank(value.query(), value.action())) {
            PluginRuntime.context().getExceptionHandler().handle(new ValidationException(EMPTY_VALUES_EXCEPTION_MESSAGE));
            return;
        }
        Map<String, String> valueMap = Maps.newHashMap();
        valueMap.put(DialogConstants.PN_DEPENDS_ON, value.query());
        valueMap.put(DialogConstants.PN_DEPENDS_ON_ACTION, value.action());
        valueMap.putAll(buildParamsMap(value));
        getXmlUtil().appendDataAttributes(element, valueMap);
    }

    /**
     * Called by {@link DependsOnHandler#accept(Element, Field)} to store {@code DependsOnConfig} value in XML markup
     * @param element Current XML element
     * @param value Current {@link DependsOnConfig} value
     */
    private void handleDependsOnConfig(Element element, DependsOnConfig value) {
        List<DependsOn> validDeclarations = Arrays.stream(value.value())
                .filter(dependsOn -> StringUtils.isNoneBlank(dependsOn.action(), dependsOn.query()))
                .collect(Collectors.toList());

        if (value.value().length != validDeclarations.size()) {
            PluginRuntime.context().getExceptionHandler()
                    .handle(new ValidationException(EMPTY_VALUES_EXCEPTION_MESSAGE));
        }

        Map<String, String> valueMap = new HashMap<>();

        String queries = validDeclarations.stream().map(DependsOn::query).collect(Collectors.joining());
        String actions = validDeclarations.stream().map(DependsOn::action).collect(Collectors.joining());

        valueMap.put(DialogConstants.PN_DEPENDS_ON, queries);
        valueMap.put(DialogConstants.PN_DEPENDS_ON_ACTION, actions);

        validDeclarations.stream().map(DependsOnHandler::buildParamsMap).forEach(valueMap::putAll);

        getXmlUtil().appendDataAttributes(element, valueMap);
    }

    /**
     * Build {@code DependsOnParam} parameters for the passed {@code DependsOn} annotation
     * @param dependsOn Current {@link DependsOn} value
     */
    private static Map<String, String> buildParamsMap(DependsOn dependsOn){
        Map<String, String> valueMap = new HashMap<>();
        for (DependsOnParam param : dependsOn.params()) {
            String paramName = StringUtils.joinWith(TERM_SEPARATOR, DialogConstants.PN_DEPENDS_ON, dependsOn.action(), param.name());
            valueMap.put(paramName, param.value());
        }
        return valueMap;
    }

    /**
     * Called by {@link DependsOnHandler#accept(Element, Field)} to store particular {@code DependsOnRef} value in XML markup
     * @param element Current XML element
     * @param value Current {@link DependsOnRef} value
     */
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
