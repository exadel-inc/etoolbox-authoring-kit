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
package com.exadel.aem.toolkit.core.optionprovider.services.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.day.cq.commons.jcr.JcrConstants;
import com.adobe.granite.ui.components.ds.ValueMapResource;

/**
 * Implements {@link OptionSourceResolver} to facilitate extracting option datasources from enum classes.
 */
public class EnumOptionSourceResolver implements OptionSourceResolver {
    private static final Logger LOG = LoggerFactory.getLogger(EnumOptionSourceResolver.class);
    private static final String VALUE_ATTRIBUTE_KEY = "value";
    private Class<? extends Enum<?>> clazz;

    /**
     * Default constructor
     */
    public EnumOptionSourceResolver() {
    }

    /**
     * Constructor for testing
     * @param clazz to be used instead of looking for it in {@link Bundle} array from {@link BundleContext}
     */
    public EnumOptionSourceResolver(Class<? extends Enum<?>> clazz) {
        this.clazz = clazz;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource pathResolve(SlingHttpServletRequest request, PathParameters pathParameters) {
        return resolve(request, pathParameters.getPath(), pathParameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource fallbackResolve(SlingHttpServletRequest request, PathParameters pathParameters) {
        return resolve(request, pathParameters.getFallbackPath(), pathParameters);
    }

    /**
     * Method, called by either {@link EnumOptionSourceResolver#resolve(SlingHttpServletRequest, PathParameters)} or
     * {@link EnumOptionSourceResolver#fallbackResolve(SlingHttpServletRequest, PathParameters)} public methods.
     * Method retrieves {@link Class} from {@link BundleContext} that should be {@link Enum} and then builds
     * a {@link Resource} based on this Enum and {@link PathParameters}
     * @param request {@link SlingHttpServletRequest} object
     * @param path           should be a valid {@link String} class name.
     * @param pathParameters represents key containing object to retrieve values from enum {@link Class}
     * @return {@link Resource}
     */
    private Resource resolve(SlingHttpServletRequest request, String path, PathParameters pathParameters) {
        if (clazz == null) {
            setClass(path);
        }

        if (clazz == null) {
            LOG.error("EnumOptionSourceResolver::resolve - value isn't a valid class name: {}", path);
            return null;
        }

        if (!clazz.isEnum()) {
            LOG.error("EnumOptionSourceResolver::resolve - class {} is not an Enum", clazz.getName());
            return null;
        }

        if (clazz.getEnumConstants().length == 0) {
            LOG.error("EnumOptionSourceResolver::resolve - Enum class {} doesn't have constants", clazz.getName());
            return null;
        }

        List<Resource> children = new ArrayList<>();

        for (Enum<?> enm : clazz.getEnumConstants()) {
            children.add(new ValueMapResource(request.getResourceResolver(),
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                new ValueMapDecorator(buildEnumConstantParams(enm, pathParameters))));
        }

        clazz = null;

        return new ValueMapResource(request.getResourceResolver(),
            StringUtils.EMPTY,
            StringUtils.EMPTY,
            new ValueMapDecorator(Collections.emptyMap()),
            children);
    }

    /**
     * Called by {@link EnumOptionSourceResolver#resolve(SlingHttpServletRequest, String, PathParameters)}
     * to set a clazz loaded from {@link BundleContext} by
     * @param className {@link String}
     */
    private void setClass(String className) {
        BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();

        for (Bundle bundle : bundleContext.getBundles()) {
            try {
                clazz = (Class<? extends Enum<?>>) bundle.loadClass(className);
                if (clazz != null) {
                    break;
                }
            } catch (ClassNotFoundException ignore) {
            }
        }
    }

    /**
     * Called by {@link EnumOptionSourceResolver#resolve(SlingHttpServletRequest, String, PathParameters)}
     * to build a {@link Map} of params based on
     * @param enm {@link Enum} and
     * @param pathParameters {@link PathParameters}.
     * Each Enum constant eventually will represent a child {@link Resource}.
     * @return {@link Map} value
     */
    private Map<String, Object> buildEnumConstantParams(Enum<?> enm, PathParameters pathParameters) {
        Map<String, Object> map = new HashMap<>();

        map.put(JcrConstants.JCR_TITLE, enm.name());
        map.put(VALUE_ATTRIBUTE_KEY, enm.toString());

        if (pathParameters.getTextMember() != null) {
            map.put(pathParameters.getTextMember(), getEnumMethodInvokeResult(pathParameters.getTextMember(), enm));
        }

        if (pathParameters.getValueMember() != null) {
            map.put(pathParameters.getValueMember(),
                getEnumMethodInvokeResult(pathParameters.getValueMember(), enm));
        }

        if (pathParameters.getAttributeMembers() != null) {
            for (String methodName : pathParameters.getAttributeMembers()) {
                map.put(methodName, getEnumMethodInvokeResult(methodName, enm));
            }
        }

        return map;
    }

    /**
     * Called by {@link EnumOptionSourceResolver#resolve(SlingHttpServletRequest, String, PathParameters)}
     * to retrieve a method by
     * @param methodName from enum {@link Class}. Then it invokes on
     * @param enm        constant and
     * @return {@link String} value or an empty {@link String} in case of {@link Exception}
     */
    private String getEnumMethodInvokeResult(String methodName, Enum<?> enm) {
        try {
            return (String) clazz.getMethod(methodName).invoke(enm);
        } catch (NoSuchMethodException ignore) {
            LOG.error("EnumOptionSourceResolver::resolve - there is no method {} at enum class {}",
                methodName, clazz.getName());
        } catch (InvocationTargetException e) {
            LOG.error("EnumOptionSourceResolver::resolve - can't invoke method {} on Enum {}", methodName, enm.name());
        } catch (IllegalAccessException ignore) {
            LOG.error("EnumOptionSourceResolver::resolve - method {} of class {} should be public",
                methodName, clazz.getName());
        }

        return "";
    }
}
