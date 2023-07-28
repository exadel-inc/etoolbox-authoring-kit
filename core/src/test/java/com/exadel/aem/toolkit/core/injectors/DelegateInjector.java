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
package com.exadel.aem.toolkit.core.injectors;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.exadel.aem.toolkit.core.injectors.utils.CastResult;
import com.exadel.aem.toolkit.core.injectors.utils.CastUtil;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import com.exadel.aem.toolkit.core.injectors.utils.AdaptationUtil;

/**
 * This is a testing scope injector that hooks on {@link RequestProperty} to make it possible to use the same testcase
 * Sling models for different injection scenarios
 */
@Component(
    service = Injector.class,
    property = Constants.SERVICE_RANKING + ":Integer=" + BaseInjector.SERVICE_RANKING)
public class DelegateInjector implements Injector {

    static final String NAME = "eak-delegate-injector";

    private final Injector delegate;

    public DelegateInjector(Injector delegate) {
        this.delegate = delegate;
    }

    @Nonnull
    @Override
    public String getName() {
        return NAME;
    }

    @CheckForNull
    @Override
    public Object getValue(
        @Nonnull Object adaptable,
        String name,
        @Nonnull Type type,
        @Nonnull AnnotatedElement annotatedElement,
        @Nonnull DisposalCallbackRegistry disposalCallbackRegistry) {

        RequestProperty requestProperty = annotatedElement.getAnnotation(RequestProperty.class);
        String effectiveName = StringUtils.defaultIfBlank(requestProperty.name(), name);

        if (delegate instanceof RequestAttributeInjector) {
            Object value = ((RequestAttributeInjector) delegate).getValue(
                AdaptationUtil.getRequest(adaptable),
                effectiveName,
                type);
            boolean isFallbackValue = value instanceof CastResult && ((CastResult) value).isFallback();
            if ((value == null || isFallbackValue) && annotatedElement.isAnnotationPresent(Default.class)) {
                value = getDefaultValue(annotatedElement.getDeclaredAnnotation(Default.class));
                value = CastUtil.toType(value, type);
            }
            return value instanceof CastResult ? ((CastResult) value).getValue() : value;
        }

        if (delegate instanceof RequestParamInjector) {
            Object value = ((RequestParamInjector) delegate).getValue(
                AdaptationUtil.getRequest(adaptable),
                effectiveName,
                type);
            boolean isFallbackValue = value instanceof CastResult && ((CastResult) value).isFallback();
            if ((value == null || isFallbackValue) && annotatedElement.isAnnotationPresent(Default.class)) {
                value = getDefaultValue(annotatedElement.getDeclaredAnnotation(Default.class));
                value = CastUtil.toType(value, type);
            }
            return value instanceof CastResult ? ((CastResult) value).getValue() : value;
        }

        if (delegate instanceof RequestSelectorsInjector) {
            Object value = ((RequestSelectorsInjector) delegate).getValue(
                AdaptationUtil.getRequest(adaptable),
                type);
            boolean isFallbackValue = value instanceof CastResult && ((CastResult) value).isFallback();
            if ((value == null || isFallbackValue) && annotatedElement.isAnnotationPresent(Default.class)) {
                value = getDefaultValue(annotatedElement.getDeclaredAnnotation(Default.class));
                value = CastUtil.toType(value, type);
            }
            return value instanceof CastResult ? ((CastResult) value).getValue() : value;
        }

        if (delegate instanceof RequestSuffixInjector) {
            Object value = ((RequestSuffixInjector) delegate).getValue(
                AdaptationUtil.getRequest(adaptable),
                type);
            boolean isFallbackValue = value instanceof CastResult && ((CastResult) value).isFallback();
            if ((value == null || isFallbackValue) && annotatedElement.isAnnotationPresent(Default.class)) {
                value = getDefaultValue(annotatedElement.getDeclaredAnnotation(Default.class));
                value = CastUtil.toType(value, type);
            }
            return value instanceof CastResult ? ((CastResult) value).getValue() : value;
        }

        if (delegate instanceof EnumValueInjector) {
            Object value = ((EnumValueInjector) delegate).getValue(
                adaptable,
                effectiveName,
                StringUtils.EMPTY,
                type);
            boolean isFallbackValue = value instanceof CastResult && ((CastResult) value).isFallback();
            if ((value == null || isFallbackValue) && annotatedElement.isAnnotationPresent(Default.class)) {
                value = getDefaultValue(annotatedElement.getDeclaredAnnotation(Default.class));
                value = CastUtil.toType(value, type);
            }
            return value instanceof CastResult ? ((CastResult) value).getValue() : value;
        }

        return null;
    }

    private Object getDefaultValue(Default defaultAnnotation) {
        if (ArrayUtils.isNotEmpty(defaultAnnotation.values())) {
            return defaultAnnotation.values();
        } else if (ArrayUtils.isNotEmpty(defaultAnnotation.booleanValues())) {
            return defaultAnnotation.booleanValues();
        } else if (ArrayUtils.isNotEmpty(defaultAnnotation.doubleValues())) {
            return defaultAnnotation.doubleValues();
        } else if (ArrayUtils.isNotEmpty(defaultAnnotation.floatValues())) {
            return defaultAnnotation.floatValues();
        } else if (ArrayUtils.isNotEmpty(defaultAnnotation.intValues())) {
            return defaultAnnotation.intValues();
        } else if (ArrayUtils.isNotEmpty(defaultAnnotation.longValues())) {
            return defaultAnnotation.longValues();
        } else if (ArrayUtils.isNotEmpty(defaultAnnotation.shortValues())) {
            return defaultAnnotation.shortValues();
        } else {
            return new String[0];
        }
    }
}
