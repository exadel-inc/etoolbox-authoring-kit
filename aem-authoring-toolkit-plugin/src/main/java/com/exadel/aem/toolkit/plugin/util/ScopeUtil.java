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

import org.apache.commons.lang3.ArrayUtils;

import com.exadel.aem.toolkit.api.annotations.editconfig.ChildEditConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.DesignDialog;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.HtmlTag;
import com.exadel.aem.toolkit.api.annotations.meta.MapProperties;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.Scope;
import com.exadel.aem.toolkit.plugin.source.Sources;

/**
 * Contains utility methods for managing {@link Scope} values
 */
public class ScopeUtil {

    /**
     * Default (hiding) constructor
     */
    private ScopeUtil() {
    }

    /**
     * Gets whether the given scope matches the provided class {@code Member}
     * @param scope Non-null {@code Scope} object to test matching
     * @param member Non-null {@code Member} instance
     * @return True or false
     */
    public static boolean fits(Scope scope, Member member) {
        List<Scope> activeScopes = Sources.fromMember(member)
            .tryAdaptTo(PropertyRendering.class)
            .map(PropertyRendering::scope)
            .map(Arrays::asList)
            .orElse(Collections.singletonList(Scope.DEFAULT));

        return activeScopes.contains(scope) || activeScopes.contains(Scope.DEFAULT);
    }

    /**
     * Gets whether the given scope matches the provided {@code Annotation}
     * @param scope Non-null {@code Scope} object to test matching
     * @param annotation Non-null {@code Annotation} instance
     * @param context Array of sibling annotations to decide on the scope of the current annotation if set to default,
     *                nullable
     * @return True or false
     */
    public static boolean fits(Scope scope, Annotation annotation, Annotation[] context) {
        if (!annotation.annotationType().isAnnotationPresent(MapProperties.class)) {
            return false;
        }
        Scope[] scopes = annotation.annotationType().getDeclaredAnnotation(MapProperties.class).scope();
        if (scopes.length == 1 && scopes[0].equals(Scope.DEFAULT) && ArrayUtils.isNotEmpty(context)) {
            scopes = new Scope[] {designate(Arrays.stream(context).map(Annotation::annotationType).toArray(Class<?>[]::new))};
        }
        return ArrayUtils.contains(scopes, scope) || ArrayUtils.contains(scopes, Scope.DEFAULT);
    }

    /**
     * Gets whether the given scope matches one or more other scopes
     * @param scope Non-null {@code Scope} object to test matching
     * @param others Non-null array of {@code Scope} objects
     * @return True or false
     */
    public static boolean fits(Scope scope, Scope[] others) {
        if (ArrayUtils.contains(others, scope)) {
            return true;
        }
        return ArrayUtils.contains(others, Scope.DEFAULT);
    }

    /**
     * Picks up an appropriate {@link Scope} value judging by the annotations provided. This is usually required when
     * an auto-mapped annotation or a handler has no particular scope specified, and we need to guess the scope in order
     * to avoid applying same logic to all the available scopes
     * @param annotationTypes Non-null array of {@code Annotation} objects
     * @return {@code Scope} object
     */
    public static Scope designate(Class<?>[] annotationTypes) {
        if (annotationTypes == null) {
            return Scope.DEFAULT;
        }
        if (ArrayUtils.contains(annotationTypes, Dialog.class) && !ArrayUtils.contains(annotationTypes, DesignDialog.class)) {
            return Scope.CQ_DIALOG;
        }
        if (ArrayUtils.contains(annotationTypes, AemComponent.class)) {
            return Scope.COMPONENT;
        }
        if (ArrayUtils.contains(annotationTypes, DesignDialog.class)) {
            return Scope.CQ_DESIGN_DIALOG;
        }
        if (ArrayUtils.contains(annotationTypes, EditConfig.class)) {
            return Scope.CQ_EDIT_CONFIG;
        }
        if (ArrayUtils.contains(annotationTypes, ChildEditConfig.class)) {
            return Scope.CQ_CHILD_EDIT_CONFIG;
        }
        if (ArrayUtils.contains(annotationTypes, HtmlTag.class)) {
            return Scope.CQ_HTML_TAG;
        }
        return Scope.DEFAULT;
    }
}
