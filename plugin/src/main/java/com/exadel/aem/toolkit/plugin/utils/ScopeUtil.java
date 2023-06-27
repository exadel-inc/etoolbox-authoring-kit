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
package com.exadel.aem.toolkit.plugin.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

import com.exadel.aem.toolkit.api.annotations.editconfig.ChildEditConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.DesignDialog;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.HtmlTag;
import com.exadel.aem.toolkit.api.annotations.meta.AnnotationRendering;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.Scopes;
import com.exadel.aem.toolkit.plugin.annotations.Metadata;
import com.exadel.aem.toolkit.plugin.sources.Sources;

/**
 * Contains utility methods for managing scope values
 * @see Scopes
 */
public class ScopeUtil {

    private static final List<Class<? extends Annotation>> ENTRY_POINT_ANNOTATION_TYPES = Arrays.asList(
        AemComponent.class,
        Dialog.class,
        DesignDialog.class,
        EditConfig.class,
        ChildEditConfig.class,
        HtmlTag.class
    );

    /**
     * Default (instantiation-restricting) constructor
     */
    private ScopeUtil() {
    }

    /**
     * Gets whether the given scope matches the provided class {@code Member}
     * @param scope  Non-null string value representing a scope
     * @param member Non-null {@code Member} instance
     * @return True or false
     */
    public static boolean fits(String scope, Member member) {
        List<String> activeScopes = Sources.fromMember(member)
            .tryAdaptTo(PropertyRendering.class)
            .map(PropertyRendering::scope)
            .map(Arrays::asList)
            .orElse(Collections.singletonList(Scopes.DEFAULT));

        return activeScopes.contains(scope) || activeScopes.contains(Scopes.DEFAULT);
    }

    /**
     * Gets whether the given scope matches the provided {@code Annotation}
     * @param scope      Non-null string value representing a scope
     * @param annotation Non-null {@code Annotation} instance
     * @param context    A nullable array of sibling annotations to decide on the scope of the current annotation if set
     *                   to default
     * @return True or false
     */
    public static boolean fits(String scope, Annotation annotation, Annotation[] context) {
        AnnotationRendering annotationRendering = Metadata.from(annotation).getAnnotation(AnnotationRendering.class);
        if (annotationRendering == null) {
            return false;
        }
        String[] scopes = annotationRendering.scope();
        if (scopes.length == 1 && scopes[0].equals(Scopes.DEFAULT) && ArrayUtils.isNotEmpty(context)) {
            scopes = designate(Arrays.stream(context).map(Annotation::annotationType).toArray(Class<?>[]::new));
        }
        return ArrayUtils.contains(scopes, scope) || ArrayUtils.contains(scopes, Scopes.DEFAULT);
    }

    /**
     * Gets whether the given scope matches one or more other scopes
     * @param scope  Non-null string value representing a scope
     * @param others Non-null array of {@code Scope} objects
     * @return True or false
     */
    public static boolean fits(String scope, String[] others) {
        if (ArrayUtils.contains(others, scope)) {
            return true;
        }
        return ArrayUtils.contains(others, Scopes.DEFAULT);
    }

    /**
     * Picks up an appropriate scope value judging by the annotation types provided. If one of the annotation types is
     * that of an entry-point annotation, such as {@link AemComponent}, {@link Dialog}, {@link EditConfig}, etc., the
     * corresponding scope is returned (first choice).
     * Otherwise, if the annotation types provided all have {@link AnnotationRendering meta-annotation} and share the
     * same {@code scope} value, the scope which is common for all of them is returned (second choice). Else, the
     * default scope is returned
     * @param types Non-null array of {@code Class} references representing annotation types
     * @return Array of strings representing valid scopes. Default is the array containing the single "default scope"
     * entry
     */
    public static String[] designate(Class<?>[] types) {
        if (ArrayUtils.isEmpty(types)) {
            return new String[] {Scopes.DEFAULT};
        }
        if (ArrayUtils.contains(types, Dialog.class) && !ArrayUtils.contains(types, DesignDialog.class)) {
            return new String[] {Scopes.CQ_DIALOG};
        }
        for (Class<?> annotationType : ENTRY_POINT_ANNOTATION_TYPES) {
            if (ArrayUtils.contains(types, annotationType)) {
                return annotationType.getDeclaredAnnotation(AnnotationRendering.class).scope();
            }
        }
        List<String> scopesByAnnotationRendering = Arrays.stream(types)
            .flatMap(annotationType -> annotationType.isAnnotationPresent(AnnotationRendering.class)
                ? Arrays.stream(annotationType.getDeclaredAnnotation(AnnotationRendering.class).scope())
                : Stream.of(Scopes.DEFAULT))
            .distinct()
            .collect(Collectors.toList());
        if (types.length == 1 || scopesByAnnotationRendering.size() == 1) {
            return scopesByAnnotationRendering.toArray(new String[0]);
        }
        return new String[] {Scopes.DEFAULT};
    }
}
