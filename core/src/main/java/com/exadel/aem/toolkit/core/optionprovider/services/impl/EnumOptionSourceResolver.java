package com.exadel.aem.toolkit.core.optionprovider.services.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adobe.granite.ui.components.ds.ValueMapResource;

import com.exadel.aem.toolkit.api.annotations.widgets.common.OptionProvider;
import com.exadel.aem.toolkit.api.annotations.widgets.common.OptionSource;
import com.exadel.aem.toolkit.api.annotations.widgets.radio.RadioGroup;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Select;

public class EnumOptionSourceResolver implements OptionSourceResolver {
    private static final Logger LOG = LoggerFactory.getLogger(HttpOptionSourceResolver.class);
    private boolean isFallbackEnumNeeded = false;

    @Override
    public Resource resolve(SlingHttpServletRequest request, String uri) {
        String[] params = uri.split("[#@]");
        BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        Class<?> clazz = null;
        long start = System.currentTimeMillis();

        for (Bundle bundle: bundleContext.getBundles()) {
            try {
                clazz = bundle.loadClass(params[0]);
                if (clazz != null) {
                    break;
                }
            } catch (ClassNotFoundException ignore) {
            }
        }

        if (clazz == null) {
            LOG.error("EnumOptionSourceResolver::resolve - value doesn't contain an existing class: " + params[0]);
            return null;
        }

        LOG.error("Bundle iteration time :" + (System.currentTimeMillis() - start));

        Optional<Field> optionalField = Arrays.stream(clazz.getDeclaredFields())
            .filter(m -> m.getName().equals(params[1])).findFirst();

        if (!optionalField.isPresent()) {
            LOG.error("EnumOptionSourceResolver::resolve - class " + params[0] + " doesn't have a field with the name "
                + params[1]);
            return null;
        }

        Optional<Annotation> optionalFieldAnnotation = Arrays.stream(optionalField.get().getAnnotations())
            .filter(a -> a.annotationType().getName().endsWith(params[2])).findFirst();

        if (!optionalFieldAnnotation.isPresent()) {
            LOG.error("EnumOptionSourceResolver::resolve - field " + params[1] + " isn't annotated with "
                + params[2] +  " annotation");
            return null;
        }

        Annotation annotation = optionalFieldAnnotation.get();
        OptionProvider optionProvider = null;

        if (annotation instanceof Select) {
            optionProvider = ((Select) annotation).optionProvider();
        } else if (annotation instanceof RadioGroup) {
            optionProvider = ((RadioGroup) annotation).buttonProvider();
        } else {
            LOG.error("EnumOptionSourceResolver::resolve - annotation " + params[2] + " doesn't support " +
                "OptionProvider attribute");
            return null;
        }

        Optional<OptionSource> optionSourceOptional = Arrays.stream(optionProvider.value())
            .filter(os -> os.value().equals(uri)).findFirst();

        if (!optionSourceOptional.isPresent()) {
            LOG.error("EnumOptionSourceResolver::resolve - there is no OptionSource with the value " + uri);
            return null;
        }

        OptionSource optionSource = optionSourceOptional.get();

        if (!optionSource.enumClass().isEnum()) {
            LOG.error("EnumOptionSourceResolver::resolve - OptionSource enumClass " + optionSource.enumClass().getName() +
                " is not an Enum");
            return null;
        }

        Class<? extends Enum<?>> enumClass =
            (Class<? extends Enum<?>>) (isFallbackEnumNeeded ? optionSource.fallbackEnumClass()
                                                    : optionSource.enumClass());
        List<Resource> children = new ArrayList<>();

        if (enumClass.getEnumConstants().length == 0) {
            LOG.error("EnumOptionSourceResolver::resolve - Enum class \"" + uri + "\" doesn't have constants");
            return null;
        }

        for (Enum<?> enm: enumClass.getEnumConstants()) {
            Map<String, Object> map = new HashMap<>();
            map.put("jcr:title", enm.name());
            map.put("value", enm.toString());

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

    @Override
    public Resource fallbackResolve(SlingHttpServletRequest request, String uri) {
        isFallbackEnumNeeded = true;
        return resolve(request, uri);
    }
}
