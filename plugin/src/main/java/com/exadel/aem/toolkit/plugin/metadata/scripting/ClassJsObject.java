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
import java.util.function.Function;

import jdk.nashorn.api.scripting.AbstractJSObject;
import jdk.nashorn.api.scripting.JSObject;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.metadata.Metadata;
import com.exadel.aem.toolkit.plugin.utils.ClassUtil;

class ClassJsObject extends AbstractJSObject implements AnnotatedJsObject {

    private static final String METHOD_ANCESTORS = "ancestors";
    private static final String METHOD_MEMBER = "member";
    private static final String METHOD_PARENT = "parent";

    private final Class<?> reflected;

    public ClassJsObject(Class<?> value) {
        reflected = value;
    }

    @Override
    public Object call(Object loopback, Object... args) {
        return this;
    }

    @Override
    public Object getMember(String name) {
        if (CoreConstants.PN_NAME.equals(name)) {
            return reflected.getSimpleName();
        }
        if (METHOD_ANCESTORS.equals(name)) {
            return new ListJsObject<>(
                ClassUtil.getInheritanceTree(reflected, false),
                (cls, clsName) -> cls.getSimpleName().equals(clsName) || cls.getName().equals(clsName));
        }
        if (METHOD_ANNOTATION.equals(name)) {
            return (Function<String, Metadata>) this::getAnnotation;
        }
        if (METHOD_ANNOTATIONS.equals(name)) {
            return (Function<String, List<Metadata>>) this::getAnnotations;
        }
        if (METHOD_MEMBER.equals(name)) {
            return (Function<String, JSObject>) argument -> MemberJsObject.from(reflected, argument);
        }
        if (METHOD_PARENT.equals(name)) {
            return reflected.getSuperclass() != null ? new ClassJsObject(reflected.getSuperclass()) : null;
        }
        return super.getMember(name);
    }

    @Override
    public AnnotatedElement getAnnotatedElement() {
        return reflected;
    }
}
