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
import java.lang.reflect.Member;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import jdk.nashorn.api.scripting.AbstractJSObject;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.metadata.Metadata;

class MemberJsObject extends AbstractJSObject implements AnnotatedJsObject {

    private static final String METHOD_CLASS = "class";
    private static final String METHOD_CONTEXT = "context";

    private final Member reflected;
    private final Member context;

    private MemberJsObject(Member reflected) {
        this(reflected, null);
    }

    MemberJsObject(Member reflected, Member context) {
        this.reflected = reflected;
        this.context = context;
    }

    @Override
    public Object call(Object loopback, Object... args) {
        return this;
    }

    @Override
    public Object getMember(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        if (CoreConstants.PN_NAME.equals(name)) {
            return reflected.getName();
        }
        if (METHOD_ANNOTATION.equals(name)) {
            return (Function<String, Metadata>) this::getAnnotation;
        }
        if (METHOD_ANNOTATIONS.equals(name)) {
            return (Function<String, List<Metadata>>) this::getAnnotations;
        }
        if (METHOD_CLASS.equals(name)) {
            return new ClassJsObject(reflected.getDeclaringClass());
        }
        if (METHOD_CONTEXT.equals(name)) {
            return context != null ? new MemberJsObject(context) : null;
        }
        return super.getMember(name);
    }

    @Override
    public AnnotatedElement getAnnotatedElement() {
        return (AnnotatedElement) reflected;
    }

    /* ---------------
       Factory methods
       --------------- */

    public static MemberJsObject from(Class<?> reflected, String name) {
        MemberJsObject result = fromField(reflected, name);
        if (result == null) {
            result = fromMethod(reflected, name);
        }
        return result;
    }

    private static MemberJsObject fromField(Class<?> reflected, String name) {
        try {
            return new MemberJsObject(reflected.getDeclaredField(name));
        } catch (NoSuchFieldException e) {
            // Exception is plausible
            return null;
        }
    }

    private static MemberJsObject fromMethod(Class<?> reflected, String name) {
        try {
            return new MemberJsObject(reflected.getDeclaredMethod(name));
        } catch (NoSuchMethodException e) {
            // Exception is plausible
            return null;
        }
    }
}
