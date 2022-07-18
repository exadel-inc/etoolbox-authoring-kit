package com.exadel.aem.toolkit.core.injectors;

import com.exadel.aem.toolkit.core.injectors.utils.AdaptationUtil;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class BaseInjectorTemplateMethod<T extends Annotation> implements Injector {


    protected T type;

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
        @Nonnull DisposalCallbackRegistry callbackRegistry
    ) {
        T annotation = getAnnotation(annotatedElement);

        if (annotation == null) {
            return null;
        }

        SlingHttpServletRequest request = AdaptationUtil.getRequest(adaptable);

        if(Objects.isNull(request)) {
            return null;
        }

        Supplier<Object> annotationValueSupplier = getAnnotationValueSupplier(
                                                request,
                                                name,
                                                type,
                                                annotation );

        Object value = annotationValueSupplier.get();

        if( value == null) {
            defaultMessage();
        }

        return value;
    }

    public abstract T getAnnotation(AnnotatedElement element);


    public abstract Supplier<Object> getAnnotationValueSupplier(
        SlingHttpServletRequest request,
        String name,
        Type type,
        T annotation
    );

    public abstract void defaultMessage();

}
