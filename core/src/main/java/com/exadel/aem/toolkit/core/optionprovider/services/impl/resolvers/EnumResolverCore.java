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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.day.cq.commons.jcr.JcrConstants;
import com.adobe.granite.ui.components.ds.ValueMapResource;

import com.exadel.aem.toolkit.core.CoreConstants;

/**
 * Implements {@link OptionSourceResolver} to facilitate extracting option datasources from enum classes.
 */
class EnumResolverCore {

    private static final Logger LOG = LoggerFactory.getLogger(EnumResolverCore.class);

    private static final String INVOCATION_ERROR_MESSAGE = "Could not invoke {}#{}";

    private final Class<?> source;

    public EnumResolverCore(Class<?> source) {
        this.source = source;
    }

    public Resource resolve(SlingHttpServletRequest request) {
        List<Resource> children = new ArrayList<>();
        for (Object enumConstant : source.getEnumConstants()) {
            children.add(new ValueMapResource(request.getResourceResolver(),
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                new ValueMapDecorator(getPropertyMap(enumConstant))));
        }
        return new ValueMapResource(request.getResourceResolver(),
            StringUtils.EMPTY,
            StringUtils.EMPTY,
            new ValueMapDecorator(Collections.emptyMap()),
            children);
    }

    /**
     * Called by {@link EnumResolverCore#resolve(SlingHttpServletRequest)} to build a {@link Map} of
     * params based on
     * @param enumConstant {@link Enum} and Each Enum constant eventually will represent a child {@link Resource}.
     * @return {@link Map} value
     */
    private Map<String, Object> getPropertyMap(Object enumConstant) {
        Map<String, Object> map = new HashMap<>();
        map.put(JcrConstants.JCR_TITLE, getMethodInvocationResult(enumConstant, "name"));
        map.put(CoreConstants.PN_VALUE, enumConstant.toString());
        Arrays.stream(source.getDeclaredMethods())
            .filter(method -> method.getParameterCount() == 0)
            .forEach(method -> {
                Object value = getMethodInvocationResult(enumConstant, method);
                map.put(method.getName(), value);
            });
        return map;
    }

    @SuppressWarnings("SameParameterValue")
    private String getMethodInvocationResult(Object enumElement, String methodName) {
        try {
            return getMethodInvocationResult(enumElement, enumElement.getClass().getMethod(methodName));
        } catch (NoSuchMethodException e) {
            LOG.error(INVOCATION_ERROR_MESSAGE, source.getName(), methodName, e);
        }
        return null;
    }

    private String getMethodInvocationResult(Object enumElement, Method method) {
        if (method == null) {
            return null;
        }
        try {
            return method.invoke(enumElement).toString();
        } catch (InvocationTargetException | IllegalAccessException e) {
            LOG.error(INVOCATION_ERROR_MESSAGE, source.getName(), method.getName(), e);
        }
        return StringUtils.EMPTY;
    }
}
