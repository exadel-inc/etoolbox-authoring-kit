package com.exadel.aem.toolkit.plugin.metadata.scripting;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Scriptable;

import com.exadel.aem.toolkit.core.CoreConstants;

class AnnotationAdapter extends AbstractAdapter implements Annotated {

    private final Annotation reflectedAnnotation;

    public AnnotationAdapter(Annotation value) {
        this.reflectedAnnotation = value;
    }

    @Override
    public Object get(String name, Scriptable start) {
        if (CoreConstants.PN_NAME.equals(name)) {
            return reflectedAnnotation.annotationType().getSimpleName();
        }
        if (METHOD_ANNOTATION.equals(name)) {
            return (Callable) (context, scope, thisObj, args) -> getAnnotation(args);
        }
        if (METHOD_ANNOTATIONS.equals(name)) {
            return (Callable) (context, scope, thisObj, args) -> getAnnotations(args);
        }
        try {
            return new MemberAdapter(reflectedAnnotation.annotationType().getDeclaredMethod(name), null, reflectedAnnotation);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    @Override
    public AnnotatedElement getAnnotatedElement() {
        return reflectedAnnotation.annotationType();
    }
}
