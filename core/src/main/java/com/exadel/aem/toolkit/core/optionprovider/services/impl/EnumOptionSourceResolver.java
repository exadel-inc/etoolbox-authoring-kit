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
     * @param path           should be a valid String enum class name. It is used to scan for a {@link Bundle} with the
     *                       corresponding class.
     * @param pathParameters as keys and methods return values from enum {@link Class} as values.
     * @return {@link Resource} populated with attributes from
     */
    private Resource resolve(SlingHttpServletRequest request, String path, PathParameters pathParameters) {
        if (clazz == null) {
            BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();

            for (Bundle bundle : bundleContext.getBundles()) {
                try {
                    clazz = (Class<? extends Enum<?>>) bundle.loadClass(path);
                    if (clazz != null) {
                        break;
                    }
                } catch (ClassNotFoundException ignore) {
                }
            }
        }

        if (clazz == null) {
            LOG.error("EnumOptionSourceResolver::resolve - value isn't a valid class name: " + path);
            return null;
        }

        if (!clazz.isEnum()) {
            LOG.error("EnumOptionSourceResolver::resolve - class " + clazz.getName() + " is not an Enum");
            return null;
        }

        if (clazz.getEnumConstants().length == 0) {
            LOG.error("EnumOptionSourceResolver::resolve - Enum class " + clazz.getName() + " doesn't have constants");
            return null;
        }

        List<Resource> children = new ArrayList<>();

        for (Enum<?> enm : clazz.getEnumConstants()) {
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

            children.add(new ValueMapResource(request.getResourceResolver(),
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                new ValueMapDecorator(map)));
        }

        return new ValueMapResource(request.getResourceResolver(),
            StringUtils.EMPTY,
            StringUtils.EMPTY,
            new ValueMapDecorator(Collections.emptyMap()),
            children);
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
            LOG.error("EnumOptionSourceResolver::resolve - there is no method " + methodName +
                " at enum class " + clazz.getName());
        } catch (InvocationTargetException e) {
            LOG.error("EnumOptionSourceResolver::resolve - can't invoke method " + methodName +
                " on Enum " + enm.name());
        } catch (IllegalAccessException ignore) {
            LOG.error("EnumOptionSourceResolver::resolve - method " + methodName + " of class " +
                clazz.getName() + " should be public");
        }

        return "";
    }
}
