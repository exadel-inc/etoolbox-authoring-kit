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
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The parent injector class, which is an implementation of the design pattern - template method. The class accumulates an abstract algorithm for injecting a value into an annotated field.
 * To add a new injector, you need to override the following methods in the child classes:
 * getName
 * getValue
 * getAnnotation
 * logError
 * @see Injector
 */
abstract class BaseInjector<T extends Annotation> implements Injector {

    private static final Logger LOG = LoggerFactory.getLogger(BaseInjector.class);

    static final int SERVICE_RANKING = 10000;

    private static final String BRIEF_INJECTION_ERROR_MESSAGE = "Could not inject a value for annotation {}";
    private static final String INJECTION_ERROR_MESSAGE = BRIEF_INJECTION_ERROR_MESSAGE + " at {}#{}";

    /**
     * Attempts to inject a value into the given adaptable
     * @param adaptable        A {@link SlingHttpServletRequest} or a {@link Resource} instance.
     * @param name             Name of the Java class member to inject the value into
     * @param type             Type of receiving Java class member
     * @param annotatedElement {@link AnnotatedElement} instance that facades the Java class member allowing to retrieve
     *                         annotation objects
     * @param callbackRegistry {@link DisposalCallbackRegistry} object
     * @return {@code Resource} or adapted object if successful. Otherwise, null is returned
     */
    @CheckForNull
    @Override
    public final Object getValue(
        @Nonnull Object adaptable,
        String name,
        @Nonnull Type type,
        @Nonnull AnnotatedElement element,
        @Nonnull DisposalCallbackRegistry callbackRegistry) {

        T annotation = getAnnotationType(element);
        if (Objects.isNull(annotation)) {
            return null;
        }

        Object value = getValue(adaptable, name, type, annotation);
        if (Objects.isNull(value)) {
            logException(element, annotation);
        }

        return value;
    }

    /**
     * Get the annotation class based on elements declared annotation
     * The necessary implementation is needed to implement in the descendant classes
     * @param element        A {@link AnnotatedElement} element
     * @return {@code AnnotationType} implementation of the {@link Annotation} interface
     */
    abstract T getAnnotationType(AnnotatedElement element);

    /**
     * Extracts value from a {@link SlingHttpServletRequest} or a {@link Resource} instance.
     * The necessary implementation is needed to implement in the descendant classes
     * @param adaptable        A {@link SlingHttpServletRequest} or a {@link Resource} instance
     * @param name             Name of the Java class member to inject the value into
     * @param type             Type of receiving Java class member
     * @return {@code Resource} or adapted object if successful. Otherwise, null is returned
     */
    abstract Object getValue(Object adaptable, String name, Type type, T annotation);

    /**
     Generates and displays an error message if the value cannot be injected.
     * @param  annotation            Аннотация, объявленная в целевом поле
     */
    private void logException(AnnotatedElement annotatedElement, T annotationType) {
        if (annotationType instanceof Member) {
            String className = ((Member) annotatedElement).getDeclaringClass().getName();
            String memberName = ((Member) annotatedElement).getName();
            LOG.debug(INJECTION_ERROR_MESSAGE, annotationType, className, memberName);
        } else {
            LOG.debug(BRIEF_INJECTION_ERROR_MESSAGE, annotationType);
        }
    }
}
