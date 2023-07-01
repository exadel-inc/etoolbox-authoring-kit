package com.exadel.aem.toolkit.plugin.metadata.scripting;

import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.utils.ClassUtil;

class ClassAdapter extends AbstractAdapter implements Annotated, Callable {

    private static final String METHOD_ANCESTORS = "ancestors";
    private static final String METHOD_MEMBER = "member";
    private static final String METHOD_PARENT = "parent";

    private final Class<?> reflectedClass;

    ClassAdapter(Class<?> value) {
        reflectedClass = value;
    }

    @Override
    public AnnotatedElement getAnnotatedElement() {
        return reflectedClass;
    }

    @Override
    public Object get(String name, Scriptable start) {
        if (CoreConstants.PN_NAME.equals(name)) {
            return getName();
        }
        if (METHOD_PARENT.equals(name)) {
            return reflectedClass.getSuperclass() != null ? new ClassAdapter(reflectedClass.getSuperclass()) : null;
        }
        if (METHOD_ANCESTORS.equals(name)) {
            return (Callable) this::getAncestors;
        }
        if (METHOD_ANNOTATION.equals(name)) {
            return (Callable) (context, scope, thisObj, args) -> getAnnotation(args);
        }
        if (METHOD_ANNOTATIONS.equals(name)) {
            return (Callable) (context, scope, thisObj, args) -> getAnnotations(args);
        }
        if (METHOD_MEMBER.equals(name)) {
            return (Callable) this::getMember;
        }
        return super.get(name, start);
    }

    public String getName() {
        return reflectedClass.getSimpleName();
    }

    private Object getAncestors(Context context, Scriptable scope, Scriptable thisObj, Object[] args) {
        List<ClassAdapter> classAdapters = ClassUtil.getInheritanceTree(reflectedClass, false)
            .stream()
            .map(ClassAdapter::new)
            .collect(Collectors.toList());
        return new ListAdapter<>(classAdapters, (adapter, name) -> StringUtils.equals(adapter.getName(), String.valueOf(name)));
    }

    private Object getMember(Context context, Scriptable scope, Scriptable thisObj, Object[] args) {
        if (args == null || args.length < 1 || args[0] == null) {
            return null;
        }
        String name = String.valueOf(args[0]);
        try {
            return new MemberAdapter(reflectedClass.getDeclaredField(name));
        } catch (NoSuchFieldException nsf) {
            try {
                return new MemberAdapter(reflectedClass.getDeclaredMethod(name));
            } catch (NoSuchMethodException nsm) {
                // Exception is plausible here
                return null;
            }
        }
    }

    @Override
    public Object getDefaultValue(Class<?> typeHint) {
        if (String.class.equals(typeHint)) {
            return reflectedClass.getName();
        }
        return super.getDefaultValue(typeHint);
    }

    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        return this;
    }
}
