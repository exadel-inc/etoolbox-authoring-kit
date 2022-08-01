package com.exadel.aem.toolkit.core.injectors;

import com.exadel.aem.toolkit.api.annotations.injectors.Child;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Objects;
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
    public Object getValue(
        @Nonnull Object adaptable,
        String name,
        @Nonnull Type type,
        @Nonnull AnnotatedElement annotatedElement,
        @Nonnull DisposalCallbackRegistry callbackRegistry) {

        AnnotationType annotation = getAnnotation(annotatedElement);
        if (Objects.isNull(annotation)) {
            return null;
        }

        Object value = getValue(adaptable, name, type, annotation);
        if (Objects.isNull(value)) {
            logError(annotation);
        }

        return value;
    }

    /**
     * Get the annotation class based on elements declared annotation
     * The necessary implementation is in the descendant classes
     * @param element        A {@link AnnotatedElement} element
     * @return {@code AnnotationType} implementation of the {@link Annotation} interface
     */
    abstract AnnotationType getAnnotation(AnnotatedElement element);

    /**
     * Extracts value from a {@link SlingHttpServletRequest} or a {@link Resource} instance.
     * The necessary implementation is need to implemented in the descendant classes
     * @param adaptable        A {@link SlingHttpServletRequest} or a {@link Resource} instance
     * @param name             Name of the Java class member to inject the value into
     * @param type             Type of receiving Java class member
     * @return {@code Resource} or adapted object if successful. Otherwise, null is returned
     */
    abstract Object getValue(
        Object adaptable,
        String name,
        Type type,
        AnnotationType annotation
    );

    /**
     Generates and displays an error message if the annotated value cannot be injected.
     * @param  annotation            annotation declared on the target field
     */
     void logError(AnnotationType annotation) {
        LOG.debug("Could not inject a value for annotation {} in class {}", annotation, getName());
    }



}
