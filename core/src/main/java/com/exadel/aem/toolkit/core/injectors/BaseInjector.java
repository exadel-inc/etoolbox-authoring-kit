package com.exadel.aem.toolkit.core.injectors;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Objects;

public abstract class BaseInjector<AnnotationType extends Annotation> implements Injector {

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
            logError(type);
            logError(name);
        }

        return value;
    }

    /**
     * Getting the annotation class based on elements declared annotation
     * The necessary implementation is in the descendant classes
     * @param element        A {@link AnnotatedElement} element
     * @return {@code AnnotationType} implementation of the {@link Annotation} interface
     */
    abstract AnnotationType getAnnotation(AnnotatedElement element);

    /**
     * Extracting value from a {@link SlingHttpServletRequest} or a {@link Resource} instance.
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
     * logging a message when an attempt to inject a value into an annotated element fails
     * The necessary implementation is need to implemented in the descendant classes
     * @param type             Type of receiving Java class member
     */
     void logError(Type type) {
        // implement if needed
    }

    /**
     * logging a object when an attempt to inject a value into an annotated element fails
     * The necessary implementation is need to implemented in the descendant classes
     * @param  object            name/value or another annotations variable
     */
     void logError(String object) {
        // implement if needed
    }
}
