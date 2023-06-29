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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.metadata.Metadata;

interface AnnotatedJsObject {

    String METHOD_ANNOTATION = "annotation";
    String METHOD_ANNOTATIONS = "annotations";

    AnnotatedElement getAnnotatedElement();

    default Metadata getAnnotation(String name) {
        List<Metadata> annotations = getAnnotations(name);
        if (annotations == null || annotations.isEmpty()) {
            return null;
        }
        return annotations.get(0);
    }

    default List<Metadata> getAnnotations(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        String effectiveName = StringUtils.stripStart(name, CoreConstants.SEPARATOR_AT);
        return Arrays.stream(getAnnotatedElement().getDeclaredAnnotations())
            .filter(annotation -> annotation.annotationType().getName().endsWith(effectiveName))
            .map(Metadata::from)
            .collect(Collectors.toList());
    }
}
