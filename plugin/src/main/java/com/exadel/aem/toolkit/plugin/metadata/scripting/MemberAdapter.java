package com.exadel.aem.toolkit.plugin.metadata.scripting;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import com.exadel.aem.toolkit.core.CoreConstants;

class MemberAdapter extends AbstractAdapter implements Annotated, Callable {

    private static final String PN_CLASS = "class";
    private static final String PN_CONTEXT = "context";

    private final Member reflectedMember;
    private final Member upstreamMember;
    private final Object declaringClass;


    public MemberAdapter(Member value) {
        this(value, null, null);
    }

    public MemberAdapter(Member reflectedMember, Member upstreamMember, Object declaringClass) {
        this.reflectedMember = reflectedMember;
        this.upstreamMember = upstreamMember;
        this.declaringClass = declaringClass;
    }

    @Override
    public AnnotatedElement getAnnotatedElement() {
        return (AnnotatedElement) reflectedMember;
    }

    @Override
    public Object get(String name, Scriptable start) {
        if (CoreConstants.PN_NAME.equals(name)) {
            return reflectedMember.getName();
        }
        if (PN_CLASS.equals(name) && reflectedMember.getDeclaringClass() != null) {
            return new ClassAdapter(reflectedMember.getDeclaringClass());
        }
        if (PN_CONTEXT.equals(name) && upstreamMember != null) {
            return new MemberAdapter(upstreamMember);
        }
        if (METHOD_ANNOTATION.equals(name)) {
            return (Callable) (context, scope, thisObj, args) -> getAnnotation(args);
        }
        if (METHOD_ANNOTATIONS.equals(name)) {
            return (Callable) (context, scope, thisObj, args) -> getAnnotations(args);
        }
        return super.get(name, start);
    }

    @Override
    public Object getDefaultValue(Class<?> typeHint) {
        if (String.class.equals(typeHint)) {
            return reflectedMember.getName();
        }
        return super.getDefaultValue(typeHint);
    }

    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        if (!(reflectedMember instanceof Method)) {
            return null;
        }
        try {
            return ((Method) reflectedMember).invoke(declaringClass, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }
}
