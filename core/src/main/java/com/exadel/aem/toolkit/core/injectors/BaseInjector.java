package com.exadel.aem.toolkit.core.injectors;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Objects;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.exadel.aem.toolkit.core.injectors.InjectorConstants.EXCEPTION;

/**
 * The parent injector class, which is an implementation of the design pattern - template method. The class accumulates an abstract algorithm for injecting a value into an annotated field.
 * To add a new injector, you need to override the following methods in the child classes:
 * getName
 * getValue
 * getAnnotation
 * logError
 * @see Injector
 */
public abstract class BaseInjector<AnnotationType extends Annotation> implements Injector {

    private static final Logger LOG = LoggerFactory.getLogger(BaseInjector.class);

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
        @Nonnull AnnotatedElement annotatedElement,
        @Nonnull DisposalCallbackRegistry callbackRegistry) {

        AnnotationType annotation = getAnnotation(annotatedElement);
        if (Objects.isNull(annotation)) {
            return null;
        }

        Object defaultValue = null;
        if (getHasDefaultValue(annotatedElement)) {
            defaultValue = getDefaultValue(annotatedElement, type);
        }

        Object value = getValue(adaptable, name, type, annotation, defaultValue);
        if (Objects.isNull(value)) {
            logError(annotation);
        }

        return value;
    }

    private boolean getHasDefaultValue(AnnotatedElement element) {
        return element.isAnnotationPresent(Default.class);
    }

    /**
     * The method assumes getting a default value based on the Default annotation.
     * Can be useful in cases where the functionality of the standard default injection algorithm is not enough.
     * {@see org.apache.sling.models.impl.model.AbstractInjectableElement}
     * @param element           {@link AnnotatedElement} instance that facades the Java class member allowing to retrieve
     *                          annotation objects
     * @param type             Type of receiving Java class member
     * @return Object wrapped value
     */
    Object getDefaultValue(AnnotatedElement element, Type type) {
        //implement me if needed
        return null;
    }

    /**
     * Get the annotation class based on elements declared annotation
     * The necessary implementation is needed to implement in the descendant classes
     * @param element A {@link AnnotatedElement} element
     * @return {@code AnnotationType} implementation of the {@link Annotation} interface
     */
    abstract AnnotationType getAnnotation(AnnotatedElement element);

    /**
     * Extracts value from a {@link SlingHttpServletRequest} or a {@link Resource} instance.
     * The necessary implementation is needed to implement in the descendant classes
     * @param adaptable    A {@link SlingHttpServletRequest} or a {@link Resource} instance
     * @param name         Name of the Java class member to inject the value into
     * @param type         Type of receiving Java class member
     * @param defaultValue {@see BaseInjector.getDefaultValue}
     * @return {@code Resource} or adapted object if successful. Otherwise, null is returned
     */
    abstract Object getValue(
        Object adaptable,
        String name,
        Type type,
        AnnotationType annotation,
        Object defaultValue);

    /**
     * Generates and displays an error message if the value cannot be injected.
     * @param annotation target annotation
     */
    void logError(AnnotationType annotation) {
        LOG.debug(EXCEPTION, annotation, getName());
    }
}
