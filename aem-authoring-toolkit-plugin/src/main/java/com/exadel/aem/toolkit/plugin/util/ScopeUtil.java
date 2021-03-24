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
package com.exadel.aem.toolkit.plugin.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.ImmutableMap;

import com.exadel.aem.toolkit.api.annotations.editconfig.ChildEditConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.DesignDialog;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.HtmlTag;
import com.exadel.aem.toolkit.api.annotations.meta.MapProperties;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.Scopes;
import com.exadel.aem.toolkit.plugin.source.Sources;

/**
 * Contains utility methods for managing scope values
 * @see Scopes
 */
public class ScopeUtil {

    private static final Map<Class<? extends Annotation>, String> PREDEFINED_SCOPES =
        ImmutableMap.<Class<? extends Annotation>, String>builder()
        .put(AemComponent.class, Scopes.COMPONENT)
        .put(Dialog.class, Scopes.CQ_DIALOG)
        .put(DesignDialog.class, Scopes.CQ_DESIGN_DIALOG)
        .put(EditConfig.class, Scopes.CQ_EDIT_CONFIG)
        .put(ChildEditConfig.class, Scopes.CQ_CHILD_EDIT_CONFIG)
        .put(HtmlTag.class, Scopes.CQ_HTML_TAG)
        .build();

    /**
     * Default (instantiation-restricting) constructor
     */
    private ScopeUtil() {
    }

    /**
     * Gets whether the given scope matches the provided class {@code Member}
     * @param scope Non-null string value representing a scope
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
     * @param scope Non-null string value representing a scope
     * @param annotation Non-null {@code Annotation} instance
     * @param context Array of sibling annotations to decide on the scope of the current annotation if set to default,
     *                nullable
     * @return True or false
     */
    public static boolean fits(String scope, Annotation annotation, Annotation[] context) {
        if (!annotation.annotationType().isAnnotationPresent(MapProperties.class)) {
            return false;
        }
        String[] scopes = annotation.annotationType().getDeclaredAnnotation(MapProperties.class).scope();
        if (scopes.length == 1 && scopes[0].equals(Scopes.DEFAULT) && ArrayUtils.isNotEmpty(context)) {
            scopes = designate(Arrays.stream(context).map(Annotation::annotationType).toArray(Class<?>[]::new));
        }
        return ArrayUtils.contains(scopes, scope) || ArrayUtils.contains(scopes, Scopes.DEFAULT);
    }

    /**
     * Gets whether the given scope matches one or more other scopes
     * @param scope Non-null string value representing a scope
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
     * Picks up an appropriate scope value judging by the annotations provided. Each one is tested for having
     * its {@link MapProperties} meta-annotation with an optional non-default {@code Scope}. If such is found, its value
     * is returned; otherwise, a default scope if returned
     * @param annotations Non-null array of {@code Annotation} objects
     * @return Array of {@code Scope} objects
     */
    public static String[] designate(Annotation[] annotations) {
        if (ArrayUtils.isEmpty(annotations)) {
            return new String[] {Scopes.DEFAULT};
        }
        String[] result = Arrays.stream(annotations)
            .map(Annotation::annotationType)
            .map(PREDEFINED_SCOPES::get)
            .filter(StringUtils::isNotEmpty)
            .toArray(String[]::new);
        return result.length > 0 ? result : new String[] {Scopes.DEFAULT};
    }

    /**
     * Picks up an appropriate scope value judging by the annotation types provided
     * @param annotationTypes Non-null array of {@code Class} references representing annotation types
     * @return Array of strings representing valid scopes. Default is the array containing the single "default scope" entry
     */
    public static String[] designate(Class<?>[] annotationTypes) {
        if (annotationTypes == null) {
            return new String[] {Scopes.DEFAULT};
        }
        String result;
        if (ArrayUtils.contains(annotationTypes, Dialog.class) && !ArrayUtils.contains(annotationTypes, DesignDialog.class)) {
            result = Scopes.CQ_DIALOG;
        } else {
            result = PREDEFINED_SCOPES.keySet().stream().filter(cls -> ArrayUtils.contains(annotationTypes, cls))
                .findFirst()
                .map(PREDEFINED_SCOPES::get)
                .orElse(Scopes.DEFAULT);
        }
        return new String[] {result};
    }
}
