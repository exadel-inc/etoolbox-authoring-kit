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
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.utils.ClassUtil;

/**
 * Extends {@link AbstractAdapter} to expose {@link Class} objects to the {@code Rhino} engine
 */
class ClassAdapter extends AbstractAdapter implements Annotated, Callable {

    private static final String METHOD_ANCESTORS = "ancestors";
    private static final String METHOD_MEMBER = "member";
    private static final String METHOD_PARENT = "parent";

    private final Class<?> reflectedClass;

    /**
     * Initializes a class instance storing a reference to the {@code Class} that serves as the data source for an
     * inline script
     * @param value {@code Class} instance
     */
    ClassAdapter(Class<?> value) {
        reflectedClass = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AnnotatedElement getAnnotatedElement() {
        return reflectedClass;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * Gets the name of the underlying {@code Class} instance for use with inline scripts
     * @return String value
     */
    public String getName() {
        return reflectedClass.getSimpleName();
    }

    /**
     * Gets the list of {@code Class} objects representing the inheritance tree of the underlying {@code Class}
     * instance. The signature of this method is per the contract of {@code Rhino} engine's {@link Callable}
     * @param context The {@code Context} instance
     * @param scope   The {@code Scriptable} instance representing the script scope
     * @param thisObj The {@code Scriptable} instance representing the object standing for {@code this} in a JavaScript
     *                snippet
     * @param args    The arguments of the method call
     * @return List of {@code Class} objects wrapped into {@link ListAdapter} for use with inline scripts
     */
    private Object getAncestors(Context context, Scriptable scope, Scriptable thisObj, Object[] args) {
        List<ClassAdapter> classAdapters = ClassUtil.getInheritanceTree(reflectedClass, false)
            .stream()
            .map(ClassAdapter::new)
            .collect(Collectors.toList());
        return new ListAdapter<>(classAdapters, (adapter, name) -> StringUtils.equals(adapter.getName(), String.valueOf(name)));
    }

    /**
     * Gets the {@link MemberAdapter} instance representing the field or method of the underlying {@code Class}
     * instance. The signature of this method is per the contract of {@code Rhino} engine's {@link Callable}
     * @param context The {@code Context} instance
     * @param scope   The {@code Scriptable} instance representing the script scope
     * @param thisObj The {@code Scriptable} instance representing the object standing for {@code this} in a JavaScript
     *                snippet
     * @param args    The arguments of the method call
     * @return {@code MemberAdapter} object, or {@code null} if matching member is not found
     */
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
                // An exception is plausible here
                return null;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getDefaultValue(Class<?> typeHint) {
        if (String.class.equals(typeHint)) {
            return reflectedClass.getName();
        }
        return super.getDefaultValue(typeHint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        return this;
    }
}
