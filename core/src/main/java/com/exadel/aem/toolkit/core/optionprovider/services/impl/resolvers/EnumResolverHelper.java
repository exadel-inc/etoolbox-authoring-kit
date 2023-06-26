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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.day.cq.commons.jcr.JcrConstants;
import com.adobe.granite.ui.components.ds.ValueMapResource;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.optionprovider.OptionProviderConstants;

/**
 * Invoked by {@link ClassOptionSourceResolver} to convert a Java enum into an options data source
 */
class EnumResolverHelper {

    private static final Logger LOG = LoggerFactory.getLogger(EnumResolverHelper.class);

    private static final String ENUM_METHOD_VALUES = "values";

    private final Class<?> source;

    /**
     * Default constructor
     * @param source {@code Class} object representing an enum
     */
    EnumResolverHelper(Class<?> source) {
        this.source = source;
    }

    /**
     * Creates an options data source based on the set of constants from an {@link Enum}
     * @param request {@link SlingHttpServletRequest} object that we use to create an options data source
     * @return A non-null {@code Resource} object
     */
    Resource resolve(SlingHttpServletRequest request) {
        List<Resource> children = new ArrayList<>();
        for (Object enumConstant : source.getEnumConstants()) {
            ValueMap valueMap = new ValueMapDecorator(buildPropertyMap(enumConstant));
            children.add(new ValueMapResource(
                request.getResourceResolver(),
                valueMap.get(OptionProviderConstants.PARAMETER_NAME, String.class),
                JcrConstants.NT_UNSTRUCTURED,
                new ValueMapDecorator(buildPropertyMap(enumConstant))));
        }
        return new ValueMapResource(
            request.getResourceResolver(),
            StringUtils.EMPTY,
            JcrConstants.NT_UNSTRUCTURED,
            new ValueMapDecorator(Collections.emptyMap()),
            children);
    }

    /**
     * Creates a {@link ValueMap} instance representing a single data source option for the given enum constant
     * @param enumConstant An enum object
     * @return {@link Map} object
     */
    private Map<String, Object> buildPropertyMap(Object enumConstant) {
        Map<String, Object> result = new HashMap<>();
        Object rawName = getMethodInvocationResult(enumConstant, CoreConstants.PN_NAME);
        String name = rawName != null ? rawName.toString() : enumConstant.toString();
        result.put(OptionProviderConstants.PARAMETER_NAME, name);
        result.put(JcrConstants.JCR_TITLE, rawName);
        result.put(CoreConstants.PN_VALUE, enumConstant.toString());
        Arrays.stream(source.getDeclaredMethods())
            .filter(method -> !ENUM_METHOD_VALUES.equals(method.getName()))
            .filter(method -> method.getParameterCount() == 0)
            .filter(method -> !Modifier.isPrivate(method.getModifiers()))
            .forEach(method -> {
                Object value = getMethodInvocationResult(enumConstant, method);
                if (value != null) {
                    result.put(method.getName(), value);
                }
            });
        Arrays.stream(source.getDeclaredFields())
            .filter(field -> Modifier.isPublic(field.getModifiers()) && !Modifier.isStatic(field.getModifiers()))
            .forEach(field -> {
                Object value = getFieldInvocationResult(enumConstant, field);
                if (value != null) {
                    result.put(field.getName(), value);
                }
            });
        return result;
    }

    /**
     * Attempts to get a string value from the invocation of the enum method by its name
     * @param enumConstant An enum object
     * @param methodName   A string representing the name of the method
     * @return A nullable string
     */
    @SuppressWarnings("SameParameterValue")
    private Object getMethodInvocationResult(Object enumConstant, String methodName) {
        try {
            return getMethodInvocationResult(enumConstant, enumConstant.getClass().getMethod(methodName));
        } catch (NoSuchMethodException e) {
            LOG.error(ClassOptionSourceResolver.EXCEPTION_COULD_NOT_INVOKE, source.getName(), methodName, e);
        }
        return null;
    }

    /**
     * Attempts to get a value from the invocation of the given enum method
     * @param enumConstant An enum object
     * @param method       A nullable {@link Method} instance
     * @return A nullable object
     */
    private Object getMethodInvocationResult(Object enumConstant, Method method) {
        if (method == null) {
            return null;
        }
        try {
            return method.invoke(enumConstant);
        } catch (InvocationTargetException | IllegalAccessException e) {
            LOG.error(ClassOptionSourceResolver.EXCEPTION_COULD_NOT_INVOKE, source.getName(), method.getName(), e);
        }
        return null;
    }

    /**
     * Attempts to get a value from accessing the given field of an enum constant
     * @param enumConstant An enum object
     * @param field        {@link Field} instance
     * @return A nullable object
     */
    private Object getFieldInvocationResult(Object enumConstant, Field field) {
        try {
            return field.get(enumConstant);
        } catch (IllegalAccessException e) {
            LOG.error(ClassOptionSourceResolver.EXCEPTION_COULD_NOT_INVOKE, source.getName(), field.getName(), e);
        }
        return null;
    }
}
