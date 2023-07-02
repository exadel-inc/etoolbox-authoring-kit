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
                .filter(a -> name.contains(CoreConstants.SEPARATOR_DOT)
                    ? a.annotationType().getName().equals(name)
                    : a.annotationType().getSimpleName().equals(name))
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
            .filter(a -> name.contains(CoreConstants.SEPARATOR_DOT)
                ? a.annotationType().getName().equals(name)
                : a.annotationType().getSimpleName().equals(name))
            .map(AnnotationAdapter::new)
            .collect(Collectors.toList());
        return new ListAdapter<>(annotationAdapters);
    }

}
