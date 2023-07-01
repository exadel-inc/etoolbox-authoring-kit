package com.exadel.aem.toolkit.plugin.metadata.scripting;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.core.CoreConstants;

interface Annotated {

    String METHOD_ANNOTATION = "annotation";
    String METHOD_ANNOTATIONS = "annotations";

    AnnotatedElement getAnnotatedElement();

    default Object getAnnotation(Object[] args) {
        if (args == null || args.length < 1) {
            return null;
        }
        String name = StringUtils.stripStart(String.valueOf(args[0]), CoreConstants.SEPARATOR_AT);
        Annotation annotation = Arrays.stream(getAnnotatedElement().getDeclaredAnnotations())
                .filter(a -> name.contains(CoreConstants.SEPARATOR_DOT) ? a.annotationType().getName().equals(name) :
                        a.annotationType().getSimpleName().equals(name))
                .findFirst()
                .orElse(null);
        if (annotation == null) {
            return null;
        }
        return new AnnotationAdapter(annotation);
    }

    default Object getAnnotations(Object[] args) {
        if (args == null || args.length < 1) {
            return null;
        }
        String name = StringUtils.stripStart(String.valueOf(args[0]), CoreConstants.SEPARATOR_AT);
        List<AnnotationAdapter> annotationAdapters = Arrays.stream(getAnnotatedElement().getDeclaredAnnotations())
            .filter(a -> name.contains(CoreConstants.SEPARATOR_DOT) ? a.annotationType().getName().equals(name) :
                a.annotationType().getSimpleName().equals(name))
            .map(AnnotationAdapter::new)
            .collect(Collectors.toList());
        return new ListAdapter<>(annotationAdapters);
    }

}
