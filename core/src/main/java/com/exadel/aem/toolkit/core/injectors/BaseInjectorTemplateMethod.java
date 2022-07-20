package com.exadel.aem.toolkit.core.injectors;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Objects;

public abstract class BaseInjectorTemplateMethod<AnnotationType extends Annotation> implements Injector {


    AnnotationType type;

    /**
     * Attempts to inject a value into the given adaptable
     * @param adaptable        A {@link SlingHttpServletRequest} instance
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
            logError(value);
        }

        return value;
    }

    abstract AnnotationType getAnnotation(AnnotatedElement element);

    abstract Object getValue(
        Object adaptable,
        String name,
        Type type,
        AnnotationType annotation
    );

    abstract void logError(Object message);
}
