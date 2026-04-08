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
package com.exadel.aem.toolkit.core.optionprovider.services.impl.resolvers;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.day.cq.commons.jcr.JcrConstants;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.optionprovider.OptionProviderConstants;
import com.exadel.aem.toolkit.core.optionprovider.services.impl.PathParameters;
import com.exadel.aem.toolkit.core.optionprovider.utils.PatternUtil;
import com.exadel.aem.toolkit.core.utils.ResourceFactory;

/**
 * Invoked by {@link ClassOptionSourceResolver} to convert a Java class containing constants into an options data
 * source
 */
class ConstantsResolverHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ConstantsResolverHelper.class);

    private final Class<?> source;
    private final PathParameters pathParameters;

    /**
     * Default constructor
     * @param source {@code Class} object that contains constants
     * @param params {@link PathParameters} object used in the resolution process
     */
    ConstantsResolverHelper(Class<?> source, PathParameters params) {
        this.source = source;
        this.pathParameters = params;
    }

    /**
     * Creates an options data source based on the set of constants from a Java class
     * @param request {@link SlingHttpServletRequest} object that we use to create an options data source
     * @return A non-null {@code Resource} object
     */
    Resource resolve(SlingHttpServletRequest request) {
        List<Map<String, Object>> individualFieldValueMaps = Arrays.stream(source.getFields())
            .filter(field -> Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers()))
            .map(field -> {
                Map<String, Object> map = new HashMap<>();
                map.put(OptionProviderConstants.PARAMETER_NAME, field.getName());
                map.put(JcrConstants.JCR_TITLE, field.getName());
                map.put(CoreConstants.PN_VALUE, getFieldInvocationResult(field));
                return map;
            })
            .collect(Collectors.toList());
        List<Map<String, Object>> pairedValueMaps = reduce(individualFieldValueMaps, pathParameters);

        List<Resource> dataSourceOptions = pairedValueMaps
            .stream()
            .map(valueMap -> ResourceFactory
                .newResource(request.getResourceResolver())
                .path(valueMap.getOrDefault(OptionProviderConstants.PARAMETER_NAME, StringUtils.EMPTY).toString())
                .properties(valueMap)
                .build())
            .collect(Collectors.toList());
        return ResourceFactory.newResource(request.getResourceResolver())
            .children(dataSourceOptions)
            .build();
    }

    /**
     * Compacts the provided list of {@code Map}s that represent separate Java class constants. We search among the
     * value maps for every pair that refer to the same logical item (like {@code COLOR_LABEL} and {@code COLOR_VALUE})
     * and merge it into a single value map that contains both title and value
     * @param individualMaps Collection of {@code Resource} instances representing constants
     * @param pathParameters {@link PathParameters} object that is used to modify the list of options
     * @return A reduced list of value maps
     */
    private static List<Map<String, Object>> reduce(
        List<Map<String, Object>> individualMaps,
        PathParameters pathParameters) {

        if (!PatternUtil.isPattern(pathParameters.getTextMember())
            || !PatternUtil.isPattern(pathParameters.getValueMember())
            || IterableUtils.isEmpty(individualMaps)) {
            return individualMaps;
        }
        Map<String, Pair<String, String>> nameTextEntries = individualMaps
            .stream()
            .filter(valueMap -> PatternUtil.isMatch(
                valueMap.getOrDefault(JcrConstants.JCR_TITLE, StringUtils.EMPTY).toString(),
                pathParameters.getTextMember()))
            .collect(Collectors.toMap(
                valueMap -> PatternUtil.strip(
                    valueMap.getOrDefault(JcrConstants.JCR_TITLE, StringUtils.EMPTY).toString(),
                    pathParameters.getTextMember()),
                valueMap -> Pair.of(
                    valueMap.getOrDefault(OptionProviderConstants.PARAMETER_NAME, StringUtils.EMPTY).toString(),
                    valueMap.getOrDefault(CoreConstants.PN_VALUE, StringUtils.EMPTY).toString()),
                (first, second) -> first,
                LinkedHashMap::new));

        Map<String, Object> valueEntries = individualMaps
            .stream()
            .filter(valueMap -> PatternUtil.isMatch(
                valueMap.getOrDefault(JcrConstants.JCR_TITLE, StringUtils.EMPTY).toString(),
                pathParameters.getValueMember()))
            .collect(Collectors.toMap(
                valueMap -> PatternUtil.strip(
                    valueMap.getOrDefault(JcrConstants.JCR_TITLE, StringUtils.EMPTY).toString(),
                    pathParameters.getValueMember()),
                valueMap -> valueMap.getOrDefault(CoreConstants.PN_VALUE, StringUtils.EMPTY).toString()));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Pair<String, String>> textEntry : nameTextEntries.entrySet()) {
            Object value = valueEntries.get(textEntry.getKey());
            if (value == null) {
                continue;
            }
            Pair<String, String> nameAndText = textEntry.getValue();
            Map<String, Object> map = new HashMap<>();
            map.put(
                    OptionProviderConstants.PARAMETER_NAME,
                    PatternUtil.strip(nameAndText.getLeft(), pathParameters.getTextMember()));
            map.put(pathParameters.getTextMember(), nameAndText.getRight());
            map.put(pathParameters.getValueMember(), value);
            result.add(map);
        }
        return result;
    }

    /**
     * Attempts to get a value from accessing the given class field
     * @param field {@link Field} instance representing a static Java class field
     * @return A nullable object
     */
    private Object getFieldInvocationResult(Field field) {
        try {
            return field.get(null);
        } catch (IllegalAccessException e) {
            LOG.error(ClassOptionSourceResolver.EXCEPTION_COULD_NOT_INVOKE, source.getName(), field.getName(), e);
        }
        return null;
    }
}
