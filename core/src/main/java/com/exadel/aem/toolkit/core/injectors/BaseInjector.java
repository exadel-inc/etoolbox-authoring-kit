package com.exadel.aem.toolkit.core.injectors;

import static com.exadel.aem.toolkit.core.injectors.InjectorConstants.EXCEPTION;

import org.apache.commons.lang.ArrayUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
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
     * @param defaultValue
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
     * @param annotation Аннотация, объявленная в целевом поле
     */
    void logError(AnnotationType annotation) {
        LOG.debug(EXCEPTION, annotation, getName());
    }

    static boolean getHasDefaultValue(AnnotatedElement element) {
        return element.isAnnotationPresent(Default.class);
    }

    static Object getDefaultValue(AnnotatedElement element, Type type) {
        Default defaultAnnotation = element.getAnnotation(Default.class);
        if (defaultAnnotation == null) {
            return null;
        }

        Object value = null;

        if (type instanceof Class) {
            Class<?> injectedClass = (Class<?>) type;
            if (injectedClass.isArray()) {
                Class<?> componentType = injectedClass.getComponentType();
                if (componentType == String.class) {
                    value = defaultAnnotation.values();
                } else if (componentType == Integer.TYPE) {
                    value = defaultAnnotation.intValues();
                } else if (componentType == Integer.class) {
                    value = ArrayUtils.toObject(defaultAnnotation.intValues());
                } else if (componentType == Long.TYPE) {
                    value = defaultAnnotation.longValues();
                } else if (componentType == Long.class) {
                    value = ArrayUtils.toObject(defaultAnnotation.longValues());
                } else if (componentType == Boolean.TYPE) {
                    value = defaultAnnotation.booleanValues();
                } else if (componentType == Boolean.class) {
                    value = ArrayUtils.toObject(defaultAnnotation.booleanValues());
                } else if (componentType == Short.TYPE) {
                    value = defaultAnnotation.shortValues();
                } else if (componentType == Short.class) {
                    value = ArrayUtils.toObject(defaultAnnotation.shortValues());
                } else if (componentType == Float.TYPE) {
                    value = defaultAnnotation.floatValues();
                } else if (componentType == Float.class) {
                    value = ArrayUtils.toObject(defaultAnnotation.floatValues());
                } else if (componentType == Double.TYPE) {
                    value = defaultAnnotation.doubleValues();
                } else if (componentType == Double.class) {
                    value = ArrayUtils.toObject(defaultAnnotation.doubleValues());
                } else if (((Class<?>) type).getComponentType() != null && ((Class<?>) type).getComponentType().isEnum()) {
                    value = defaultAnnotation.values();
                } else {
                    LOG.warn("Default values for {} are not supported", componentType);
                }
            } else {
                if (injectedClass == String.class) {
                    value = defaultAnnotation.values().length == 0 ? "" : defaultAnnotation.values()[0];
                } else if (injectedClass == Integer.class) {
                    value = defaultAnnotation.intValues().length == 0 ? 0 : defaultAnnotation.intValues()[0];
                } else if (injectedClass == Long.class) {
                    value = defaultAnnotation.longValues().length == 0 ? 0l : defaultAnnotation.longValues()[0];
                } else if (injectedClass == Boolean.class) {
                    value = defaultAnnotation.booleanValues().length == 0 ? false : defaultAnnotation.booleanValues()[0];
                } else if (injectedClass == Short.class) {
                    value = defaultAnnotation.shortValues().length == 0 ? ((short) 0) : defaultAnnotation.shortValues()[0];
                } else if (injectedClass == Float.class) {
                    value = defaultAnnotation.floatValues().length == 0 ? 0f : defaultAnnotation.floatValues()[0];
                } else if (injectedClass == Double.class) {
                    value = defaultAnnotation.doubleValues().length == 0 ? 0d : defaultAnnotation.doubleValues()[0];
                } else if (injectedClass.isEnum()) {
                    value = defaultAnnotation.values();
                }
                else {
                    LOG.warn("Default values for {} are not supported", injectedClass);
                }
            }
        } else {
            LOG.warn("Cannot provide default for {}", type);
        }
        return value;
    }

}
