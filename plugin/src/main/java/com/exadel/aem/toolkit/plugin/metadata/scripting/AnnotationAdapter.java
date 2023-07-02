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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Scriptable;

import com.exadel.aem.toolkit.core.CoreConstants;

class AnnotationAdapter extends AbstractAdapter implements Annotated {

    private final Annotation reflectedAnnotation;

    AnnotationAdapter(Annotation value) {
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
