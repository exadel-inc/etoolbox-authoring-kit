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
package com.exadel.aem.toolkit.plugin.handlers.common;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.meta.AnnotationRendering;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.utils.AnnotationUtil;
import com.exadel.aem.toolkit.plugin.utils.ScopeUtil;

/**
 * Implements {@code BiConsumer} to populate a {@link Target} instance with properties set to be automatically mapped
 * fot the use of meta-annotation such as {@link AnnotationRendering}
 */
public class PropertyMappingHandler implements BiConsumer<Source, Target> {

    /**
     * Enumerates built-in annotations that are out of the common property mapping flow
     */
    private static final List<Class<? extends Annotation>> SPECIALLY_PROCESSED = Arrays.asList(
        AemComponent.class,
        Dialog.class
    );

    /**
     * Processes data that can be extracted from the given {@code Source} and stores it into the provided {@code Target}
     * @param source {@code Source} object used for data retrieval
     * @param target Resulting {@code Target} object
     */
    @Override
    @SuppressWarnings("deprecation") // PropertyMapping usage to be removed in a version after 2.0.2
    public void accept(Source source, Target target) {
        Annotation[] annotations = source.adaptTo(Annotation[].class);
        Arrays.stream(annotations)
            // to make sure the annotation has one of "mapping" meta-annotations
            .filter(annotation -> annotation.annotationType().isAnnotationPresent(AnnotationRendering.class)
                || annotation.annotationType().isAnnotationPresent(PropertyMapping.class))
            // to sort out annotations with specific mapping processing
            .filter(annotation -> !SPECIALLY_PROCESSED.contains(annotation.annotationType()))
            // for the exact case when @AnnotationRendering is present -- to make sure the scope is right
            .filter(annotation -> !annotation.annotationType().isAnnotationPresent(AnnotationRendering.class)
                || ScopeUtil.fits(target.getScope(), annotation, annotations))

            .forEach(annotation -> target.attributes(annotation, AnnotationUtil.getPropertyMappingFilter(annotation)));
    }
}
