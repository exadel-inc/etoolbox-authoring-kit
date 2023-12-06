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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.function.BiFunction;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.core.injectors.utils.CastUtil;

/**
 * Represents a base for a Sling injector. A descendant of this class must extract an annotation from a Java class
 * member and provide a value that matches the annotation. This value is subsequently assigned to the Java class member
 * by the Sling engine
 * @param <T> The type of annotation handled by this injector
 * @see Injector
 */
abstract class BaseInjector<T extends Annotation> implements Injector {

    private static final Logger LOG = LoggerFactory.getLogger(BaseInjector.class);

    static final int SERVICE_RANKING = 10000;

    private static final String BRIEF_INJECTION_ERROR_MESSAGE = "Could not inject a value for annotation {}";
    private static final String INJECTION_ERROR_MESSAGE = BRIEF_INJECTION_ERROR_MESSAGE + " at {}#{}";

    /**
     * Attempts to produce a value that can be further injected by Sling into the given adaptable
     * @param adaptable        A {@link SlingHttpServletRequest} or a {@link Resource} instance
     * @param name             Name of the Java class member to inject the value into
     * @param type             Type of receiving Java class member
     * @param element          {@link AnnotatedElement} instance that facades the Java class member and allows to
     *                         retrieve annotations
     * @param callbackRegistry {@link DisposalCallbackRegistry} object
     * @return A nullable value
     */
    @CheckForNull
    @Override
    public final Object getValue(
        @Nonnull Object adaptable,
        String name,
        @Nonnull Type type,
        @Nonnull AnnotatedElement element,
        @Nonnull DisposalCallbackRegistry callbackRegistry) {

        T annotation = getManagedAnnotation(element);
        if (Objects.isNull(annotation)) {
            return null;
        }
        Injectable rawValue = getValue(adaptable, name, type, annotation);
        Object value = defaultIfEmpty(rawValue, type, element);
        if (value == null) {
            logNullValue(element, annotation);
        }
        return value;
    }

    /**
     * When overridden in an injector class, extracts a value from a {@link SlingHttpServletRequest} or a
     * {@link Resource} instance
     * @param adaptable  A {@link SlingHttpServletRequest} or a {@link Resource} instance
     * @param name       Name of the Java class member to inject the value into
     * @param type       Type of the receiving Java class member
     * @param annotation Annotation handled by the current injector
     * @return A non-null {@link Injectable} instance that contains the payload that can be null
     */
    @Nonnull
    abstract Injectable getValue(Object adaptable, String name, Type type, T annotation);

    /**
     * When overridden in an injector class, retrieves a custom routine used to convert a provided value (usually the
     * value of the {@link Default} annotation) to the needs of the current injector. This method only needs overriding
     * when the injector supports conversion not covered by {@link CastUtil} (i.e., when conversion depends on the
     * type of the injectable or an annotation parameter)
     * @param type       Type of the receiving Java class member
     * @param annotation Annotation handled by the current injector
     * @return A nullable {@code BiFunction} instance
     */
    BiFunction<Object, Type, Object> getValueConverter(Type type, T annotation) {
        return null;
    }

    /**
     * When overridden in an injector class, retrieves the annotation processed by this particular injector. Takes into
     * account that there might be several annotations attached to the current Java class member, and an injector can
     * potentially process several annotation types, depending on the setup
     * @param element {@link AnnotatedElement} instance that facades the Java class member and allows retrieving
     *                annotations
     * @return {@code Annotation} instance
     */
    abstract T getManagedAnnotation(AnnotatedElement element);

    /**
     * Unwraps the value or the cast result retrieved from an injector implementation and attempts to replace it with a
     * user-specified default if null
     * @param source  The value retrieved from an injector implementation
     * @param type    Type of the receiving Java class member
     * @param element {@link AnnotatedElement} instance that facades the Java class member and allows retrieving
     * @return A nullable value
     */
    final Object defaultIfEmpty(Injectable source, Type type, AnnotatedElement element) {
        if (source != null && !source.isDefault()) {
            return source.getValue();
        }
        if (!element.isAnnotationPresent(Default.class)) {
            return source != null ? source.getValue() : null;
        }
        Object defaultValue = extractDefault(element.getDeclaredAnnotation(Default.class));
        return CastUtil.toType(
            defaultValue,
            type,
            getValueConverter(type, getManagedAnnotation(element))).getValue();
    }

    /**
     * Extracts the value from the provided {@link Default} annotation
     * @param annotation {@code Default} annotation instance
     * @return An array-typed value per the {@link Default} signature
     */
    private static Object extractDefault(Default annotation) {
        if (ArrayUtils.isNotEmpty(annotation.values())) {
            return annotation.values();
        } else if (ArrayUtils.isNotEmpty(annotation.booleanValues())) {
            return annotation.booleanValues();
        } else if (ArrayUtils.isNotEmpty(annotation.doubleValues())) {
            return annotation.doubleValues();
        } else if (ArrayUtils.isNotEmpty(annotation.floatValues())) {
            return annotation.floatValues();
        } else if (ArrayUtils.isNotEmpty(annotation.longValues())) {
            return annotation.longValues();
        } else if (ArrayUtils.isNotEmpty(annotation.intValues())) {
            return annotation.intValues();
        } else if (ArrayUtils.isNotEmpty(annotation.shortValues())) {
            return annotation.shortValues();
        }
        return new String[0];
    }

    /**
     * Outputs a formatted message informing that the injection has not been successful
     * @param annotatedElement {@link AnnotatedElement} instance that facades the Java class member and allows
     *                         retrieving annotations
     * @param annotation       The annotation that they attempted to retrieve
     */
    private static void logNullValue(AnnotatedElement annotatedElement, Annotation annotation) {
        if (annotatedElement instanceof Member) {
            String className = ((Member) annotatedElement).getDeclaringClass().getName();
            String memberName = ((Member) annotatedElement).getName();
            LOG.debug(INJECTION_ERROR_MESSAGE, annotation, className, memberName);
        } else {
            LOG.debug(BRIEF_INJECTION_ERROR_MESSAGE, annotation);
        }
    }
}
