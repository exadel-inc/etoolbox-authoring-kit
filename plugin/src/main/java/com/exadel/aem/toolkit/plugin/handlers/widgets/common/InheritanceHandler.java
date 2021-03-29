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
package com.exadel.aem.toolkit.plugin.handlers.widgets.common;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.function.BiConsumer;

import org.apache.maven.shared.utils.StringUtils;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;
import com.exadel.aem.toolkit.api.annotations.widgets.Extends;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;
import com.exadel.aem.toolkit.plugin.source.Sources;

/**
 * Handler for processing Granite UI widgets features "inherited" by the current {@code Source} from other {@code Source}s
 * via {@link Extends} mechanism
 */
public class InheritanceHandler implements BiConsumer<Source, Target> {
    private final BiConsumer<Source, Target> descendantChain;
    public InheritanceHandler(BiConsumer<Source, Target> descendantChain) {
        this.descendantChain = descendantChain;
    }

    /**
     * Processes the user-defined data and writes it to {@link Target}
     * @param source Current {@link Source} instance
     * @param target Current {@link Target} instance
     */
    @Override
    public void accept(Source source, Target target) {
        if (descendantChain == null) return;
        Deque<Source> inheritanceTree = getInheritanceTree(source);
        while (!inheritanceTree.isEmpty()) {
            descendantChain.accept(inheritanceTree.pollLast(), target); // to render 'ancestors' of context source starting from next handler in chain
        }
    }

    /**
     * Builds the inheritance sequence for the current {@link Source}
     * @param source Current {@link Source} instance
     * @return Ancestral {@link Target}s in an ordered sequence
     */
    private static Deque<Source> getInheritanceTree(Source source) {
        Deque<Source> result = new LinkedList<>();
        Annotation widgetAnnotation = getReferencedWidgetAnnotation(source);
        if (widgetAnnotation == null) {
            return result;
        }
        Extends extendsAnnotation = source.adaptTo(Extends.class);
        while (extendsAnnotation != null) {
            String referencedFieldName = extendsAnnotation.field().isEmpty() ? source.getName() : extendsAnnotation.field();
            try {
                Source referencedField = Sources.fromMember(extendsAnnotation.value().getDeclaredField(referencedFieldName), extendsAnnotation.value());
                if (referencedField.equals(source) || result.contains(referencedField)) { // to avoid circular references
                    break;
                }
                Annotation referencedFieldWidgetAnnotation = getReferencedWidgetAnnotation(referencedField);
                if (referencedFieldWidgetAnnotation != null
                    && widgetAnnotation.annotationType().equals(referencedFieldWidgetAnnotation.annotationType())) { // to avoid mixing up props of different components
                    result.add(referencedField);
                }
                extendsAnnotation = referencedField.adaptTo(Extends.class);
            } catch (NoSuchFieldException e) {
                PluginRuntime.context().getExceptionHandler().handle(e);
                extendsAnnotation = null;
            }
        }
        return result;
    }

    /**
     * Finds among the annotations of the provided {@code Source} the first annotation that defines a widget (i.e. has
     * a valid {@link ResourceType} meta-annotation
     * @param source Current {@link Source} instance
     * @return {@code Annotation} object or null in case no compliant annotation found
     */
    private static Annotation getReferencedWidgetAnnotation(Source source) {
        return Arrays.stream(source.adaptTo(Annotation[].class))
            .filter(annotation -> annotation.annotationType().isAnnotationPresent(ResourceType.class)
                && StringUtils.isNotBlank(annotation.annotationType().getDeclaredAnnotation(ResourceType.class).value()))
            .findFirst()
            .orElse(null);
    }
}
