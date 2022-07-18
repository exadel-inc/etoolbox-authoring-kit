package com.exadel.aem.toolkit.core.injectors;

import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.models.spi.DisposalCallbackRegistry;
import org.apache.sling.models.spi.Injector;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.function.Supplier;

public abstract class BaseInjectorTemplateMethod<T extends Annotation, R extends Adaptable> implements Injector {


    protected T type;
    protected R resource;

    @CheckForNull
    @Override
    public Object getValue(
        @Nonnull Object o,
        String name,
        @Nonnull Type type,
        @Nonnull AnnotatedElement annotatedElement,
        @Nonnull DisposalCallbackRegistry disposalCallbackRegistry
    ) {
        T annotation = getAnnotation(annotatedElement);

        if (annotation == null) {
            return null;
        }

        R adaptable = getAdaptable(o);

        if(adaptable == null) {
            return null;
        }

        Supplier<Object> annotationValueSupplier = getAnnotationValueSupplier(
                                                adaptable,
                                                name,
                                                type,
                                                annotatedElement,
                                                disposalCallbackRegistry,
                                                annotation );

        Object value = annotationValueSupplier.get();

        if( value == null) {
            defaultMessage();
        }

        return value;
    }

    public abstract T getAnnotation(AnnotatedElement element);

    public abstract R getAdaptable(Object object);


    public abstract Supplier<Object> getAnnotationValueSupplier(
        Object o,
        String name,
        Type type,
        AnnotatedElement element,
        DisposalCallbackRegistry disposalCallbackRegistry,
        T annotation
    );

    public abstract void defaultMessage();

}
