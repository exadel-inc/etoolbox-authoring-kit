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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.day.cq.commons.jcr.JcrConstants;
import com.adobe.granite.ui.components.ds.ValueMapResource;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.optionprovider.services.impl.PathParameters;
import com.exadel.aem.toolkit.core.optionprovider.utils.PatternUtil;

class ConstantsClassResolverCore {

    private static final Logger LOG = LoggerFactory.getLogger(ConstantsClassResolverCore.class);

    private static final String INVOCATION_ERROR_MESSAGE = "Could not invoke {}#{}";

    private final Class<?> source;
    private final PathParameters pathParameters;

    public ConstantsClassResolverCore(Class<?> source, PathParameters pathParameters) {
        this.source = source;
        this.pathParameters = pathParameters;
    }

    public Resource resolve(SlingHttpServletRequest request) {
        List<ValueMap> valueMaps = Arrays.stream(source.getFields())
            .filter(field -> Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers()))
            .map(field -> buildValueMap(field.getName(), field.getName(), getFieldInvocationResult(field)))
            .map(ValueMapDecorator::new)
            .collect(Collectors.toList());

        valueMaps = merge(valueMaps);

        List<Resource> children = valueMaps
            .stream()
            .map(valueMap -> new ValueMapResource(
                request.getResourceResolver(),
                valueMap.get(CoreConstants.PARAMETER_NAME, String.class),
                JcrConstants.NT_UNSTRUCTURED,
                valueMap))
            .collect(Collectors.toList());

        return new ValueMapResource(request.getResourceResolver(),
            StringUtils.EMPTY,
            JcrConstants.NT_UNSTRUCTURED,
            new ValueMapDecorator(Collections.emptyMap()),
            children);
    }

    private Object getFieldInvocationResult(Field field) {
        try {
            return field.get(null);
        } catch (IllegalAccessException e) {
            LOG.error(INVOCATION_ERROR_MESSAGE, source.getName(), field.getName(), e);
        }
        return StringUtils.EMPTY;
    }

    private List<ValueMap> merge(List<ValueMap> valueMaps) {
        if (pathParameters == null
            || !PatternUtil.isPattern(pathParameters.getTextMember())
            || !PatternUtil.isPattern(pathParameters.getValueMember())) {
            return valueMaps;
        }
        Map<String, Pair<String, String>> textEntries = valueMaps
            .stream()
            .filter(valueMap -> PatternUtil.isMatch(valueMap.get(JcrConstants.JCR_TITLE, String.class), pathParameters.getTextMember()))
            .collect(Collectors.toMap(
                valueMap ->  PatternUtil.strip(valueMap.get(JcrConstants.JCR_TITLE, String.class), pathParameters.getTextMember()),
                valueMap -> Pair.of(valueMap.get(CoreConstants.PARAMETER_NAME, String.class), valueMap.get(CoreConstants.PN_VALUE, String.class)),
                (first, second) -> first,
                LinkedHashMap::new
            ));
        Map<String, Object> valueEntries = valueMaps
            .stream()
            .filter(valueMap -> PatternUtil.isMatch(valueMap.get(JcrConstants.JCR_TITLE, String.class), pathParameters.getValueMember()))
            .collect(Collectors.toMap(
                valueMap ->  PatternUtil.strip(valueMap.get(JcrConstants.JCR_TITLE, String.class), pathParameters.getValueMember()),
                valueMap -> valueMap.get(CoreConstants.PN_VALUE, StringUtils.EMPTY)));

        List<ValueMap> result = new ArrayList<>();
        for (String entryKey : textEntries.keySet()) {
            Object value = valueEntries.get(entryKey);
            if (value == null) {
                continue;
            }
            Pair<String, String> nameAndText = textEntries.get(entryKey);
            Map<String, Object> valueMapProperties = buildValueMap(
                PatternUtil.strip(nameAndText.getLeft(), pathParameters.getTextMember()),
                nameAndText.getRight(),
                value);
            ValueMap valueMap = new ValueMapDecorator(valueMapProperties);
            result.add(valueMap);
        }
        return result;
    }

    private static Map<String, Object> buildValueMap(String name, String text, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(CoreConstants.PARAMETER_NAME, name);
        map.put(CoreConstants.PN_VALUE, value);
        map.put(JcrConstants.JCR_TITLE, text);
        return map;
    }
}
