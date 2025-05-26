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
package com.exadel.aem.toolkit.plugin.handlers.dependson;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.Maps;

import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOn;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnConfig;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnParam;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnRef;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnRefTypes;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.exceptions.ValidationException;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;
import com.exadel.aem.toolkit.plugin.utils.StringUtil;

/**
 * Implements {@code BiConsumer} to populate a {@link Target} instance with properties originating from a {@link Source}
 * object that define relations between Granite components per {@code DependsOn} specification
 */
public class DependsOnHandler implements BiConsumer<Source, Target> {

    static final String EMPTY_VALUES_EXCEPTION_MESSAGE = "Non-empty string values are required for DependsOn params";

    /**
     * Processes data that can be extracted from the given {@code Source} and stores it into the provided {@code Target}
     * @param source {@code Source} object used for data retrieval
     * @param target Resulting {@code Target} object
     */
    @Override
    public void accept(Source source, Target target) {
        if (source.adaptTo(DependsOn.class) != null) {
            handleDependsOn(source.adaptTo(DependsOn.class), target);
        } else if (source.adaptTo(DependsOnConfig.class) != null) {
            handleDependsOnConfig(source.adaptTo(DependsOnConfig.class), target);
        }
        handleDependsOnRefValue(source, target);
    }

    /**
     * Called by {@link DependsOnHandler#accept(Source, Target)} to store particular {@code DependsOn} value in {@link Target}
     * @param value  Current {@link DependsOn} value
     * @param target Resulting {@code Target} object
     */
    private static void handleDependsOn(DependsOn value, Target target) {
        String query = value.query();
        String[] actions = value.action();

        if (StringUtils.isBlank(query) || actions.length == 0 || Arrays.stream(actions).anyMatch(StringUtils::isBlank)) {
            PluginRuntime.context().getExceptionHandler().handle(new ValidationException(EMPTY_VALUES_EXCEPTION_MESSAGE));
            return;
        }
        Map<String, Object> valueMap = Maps.newHashMap();
        String escapedQuery = escapeValue(query);
        valueMap.put(DialogConstants.PN_DEPENDS_ON, escapedQuery);

        String actionValue = actions.length > 1
            ? String.join(DialogConstants.SEPARATOR_COMMA, actions)
            : actions[0];
        valueMap.put(DialogConstants.PN_DEPENDS_ON_ACTION, actionValue);
        valueMap.putAll(buildParamsMap(value, 0));

        target.getOrCreateTarget(CoreConstants.NN_GRANITE_DATA).attributes(valueMap);
    }

    /**
     * Called by {@link DependsOnHandler#accept(Source, Target)} to store {@code DependsOnConfig} value in {@link Target}
     * @param value  Current {@link DependsOnConfig} value
     * @param target Resulting {@code Target} object
     */
    private static void handleDependsOnConfig(DependsOnConfig value, Target target) {
        List<DependsOn> validDeclarations = Arrays.stream(value.value())
            .filter(dependsOn -> StringUtils.isNotBlank(dependsOn.query())
                && dependsOn.action().length > 0
                && Arrays.stream(dependsOn.action()).allMatch(StringUtils::isNotBlank))
            .collect(Collectors.toList());

        if (value.value().length != validDeclarations.size()) {
            PluginRuntime.context().getExceptionHandler()
                .handle(new ValidationException(EMPTY_VALUES_EXCEPTION_MESSAGE));
        }

        Map<String, Object> valueMap = new HashMap<>();

        String queries = validDeclarations.stream()
            .map(DependsOn::query)
            .map(str -> StringUtils.replace(str, ";", "\\\\;"))
            .collect(Collectors.joining(DialogConstants.SEPARATOR_SEMICOLON));
        String actions = validDeclarations.stream()
            .map(dependsOn -> dependsOn.action().length > 1
                ? String.join(DialogConstants.SEPARATOR_COMMA, dependsOn.action())
                : dependsOn.action()[0])
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.joining(DialogConstants.SEPARATOR_SEMICOLON));

        valueMap.put(DialogConstants.PN_DEPENDS_ON, queries);
        valueMap.put(DialogConstants.PN_DEPENDS_ON_ACTION, actions);
        Map<String, Integer> counter = new HashMap<>();
        validDeclarations.stream()
            .map(dependsOn -> DependsOnHandler.buildParamsMap(dependsOn, counter.merge(String.join(",", dependsOn.action()), 1, Integer::sum) - 1))
            .forEach(valueMap::putAll);

        target.getOrCreateTarget(CoreConstants.NN_GRANITE_DATA).attributes(valueMap);
    }

    /**
     * Builds a dictionary of parameters for the passed {@code DependsOn} annotation
     * Parameters format is:<br>
     * - for the first action (index = 0): {@code dependson-{action}-{param}}<br>
     * - otherwise: {@code dependson-{action}-{param}-{index}}
     * @param dependsOn Current {@link DependsOn} value
     * @param index     Action index
     * @return {@code Map} representing settings for the {@code DependsOn} instance
     */
    private static Map<String, String> buildParamsMap(DependsOn dependsOn, int index) {
        Map<String, String> valueMap = new HashMap<>();
        for (DependsOnParam param : dependsOn.params()) {
            String paramName = StringUtils.joinWith(
                CoreConstants.SEPARATOR_HYPHEN,
                DialogConstants.PN_DEPENDS_ON,
                String.join(DialogConstants.SEPARATOR_COMMA, dependsOn.action()),
                param.name());
            if (index > 0) {
                paramName = StringUtils.joinWith(CoreConstants.SEPARATOR_HYPHEN, paramName, index);
            }
            valueMap.put(paramName, param.value());
        }
        return valueMap;
    }

    /**
     * Called by {@link DependsOnHandler#accept(Source, Target)} to store particular {@code DependsOnRef} value in {@link Target}
     * @param source {@code Source} object used for data retrieval
     * @param target Resulting {@code Target} object
     */
    private static void handleDependsOnRefValue(Source source, Target target) {
        DependsOnRef value = source.adaptTo(DependsOnRef.class);
        if (value == null) {
            return;
        }

        String dependsOnRefName = value.name();
        if (StringUtils.isBlank(dependsOnRefName)) {
            dependsOnRefName = source.getName();
        }

        Map<String, Object> valueMap = Maps.newHashMap();
        valueMap.put(DialogConstants.PN_DEPENDS_ON_REF, dependsOnRefName);
        if (!value.type().toString().equals(DependsOnRefTypes.AUTO.toString())) {
            valueMap.put(DialogConstants.PN_DEPENDS_ON_REFTYPE, value.type().toString().toLowerCase());
        }
        if (value.lazy()) {
            valueMap.put(DialogConstants.PN_DEPENDS_ON_REFLAZY, StringUtils.EMPTY);
        }
        target.getOrCreateTarget(CoreConstants.NN_GRANITE_DATA).attributes(valueMap);
    }

    /**
     * Escape characters in the given string
     * @param value The string to process
     * @return The string with escaped values
     * */
    private static String escapeValue(String value) {
        String result = StringUtils.replace(
            value,
            DialogConstants.SEPARATOR_SEMICOLON,
            "\\\\" + DialogConstants.SEPARATOR_SEMICOLON);
        result = StringUtil.escapeArray(result);
        return result;
    }
}
