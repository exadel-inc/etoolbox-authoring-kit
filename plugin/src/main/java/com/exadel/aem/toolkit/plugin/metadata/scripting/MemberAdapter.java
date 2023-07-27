/*
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.exadel.aem.toolkit.plugin.metadata.scripting;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.sources.ModifiableMemberSource;

/**
 * Extends {@link AbstractAdapter} to expose {@link Member} objects to the {@code Rhino} engine
 */
class MemberAdapter extends AbstractAdapter implements Annotated, Callable {

    private static final String PN_CLASS = "class";
    private static final String PN_CONTEXT = "context";

    private final Member reflectedMember;
    private final Member upstreamMember;
    private final Object declaringClass;

    /**
     * Initializes a class instance storing a reference to the {@code Member} that serves as the data source for an
     * inline script
     * @param value {@code Member} instance
     */
    MemberAdapter(Member value) {
        this(value, null, null);
    }

    /**
     * Initializes a class instance storing a reference to the {@code Member} that serves as the data source for an
     * inline script
     * @param reflectedMember The {@code Member} instance that stands for "current" member
     * @param upstreamMember  The {@code Member} instance that corresponds in meaning to
     *                        {@link ModifiableMemberSource#getUpstreamMember()}
     * @param declaringClass  The {@code Class} instance that corresponds in meaning to
     *                        {@link ModifiableMemberSource#getDeclaringClass()}
     */
    MemberAdapter(Member reflectedMember, Member upstreamMember, Object declaringClass) {
        this.reflectedMember = reflectedMember;
        this.upstreamMember = upstreamMember;
        this.declaringClass = declaringClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AnnotatedElement getAnnotatedElement() {
        return (AnnotatedElement) reflectedMember;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getDefaultValue(Class<?> typeHint) {
        if (String.class.equals(typeHint)) {
            return reflectedMember.getName();
        }
        return super.getDefaultValue(typeHint);
    }

    /**
     * {@inheritDoc}
     */
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
