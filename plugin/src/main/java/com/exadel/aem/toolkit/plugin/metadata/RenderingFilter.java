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
package com.exadel.aem.toolkit.plugin.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.meta.AnnotationRendering;
import com.exadel.aem.toolkit.api.annotations.meta.IgnorePropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.utils.ArrayUtil;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;

/**
 * Implements {@link Predicate} to filter methods entitled for rendering in the {@code Granite UI} dialogs.
 * This filter uses a list of mappings obtained from the annotations of the method.
 */
public class RenderingFilter implements Predicate<Method> {

    public static final Predicate<?> EMPTY = arg -> true;

    private final List<String> mappings;

    /**
     * Initializes a new instance of {@link RenderingFilter} using the data from the provided {@code Annotation}
     * @param annotation The {@link Annotation} from which the mappings are derived
     */
    public RenderingFilter(Annotation annotation) {
        mappings = getMappings(annotation);
    }

    /**
     * Applies the filter to the provided {@code Method}
     * @param value The {@code Method} to test
     * @return {@code true} if the method passes the filter, {@code false} otherwise
     */
    @Override
    @SuppressWarnings({"squid:S1874", "deprecation"})
    // Processing of IgnorePropertyMapping is retained for compatibility and will be removed in a version after 2.0.2
    public boolean test(Method value) {
        return test(value.getName(), value.getAnnotation(IgnorePropertyMapping.class) != null);
    }

    /**
     * Applies the filter to the provided {@code Method}
     * @param name The name of the method to test
     * @param ignoreMappingIsSet Indicates if the ignore mapping is set for this method
     * @return {@code true} if the method name passes the filter, {@code false} otherwise
     */
    private boolean test(String name, boolean ignoreMappingIsSet) {
        if (mappings.isEmpty() || mappings.contains(DialogConstants.VALUE_NONE)) {
            return false;
        }
        if (mappings.stream().anyMatch(mapping -> !mapping.startsWith(DialogConstants.NEGATION)
            && !CoreConstants.WILDCARD.equals(mapping))) {
            return mappings.contains(name) && !ignoreMappingIsSet;
        }
        return mappings.stream().noneMatch(mapping -> mapping.equals(DialogConstants.NEGATION + name))
            && mappings.contains(CoreConstants.WILDCARD);

    }

    /**
     * Retrieves the mappings from the provided {@code Annotation}
     * @param annotation The {@link Annotation} from which the mappings are derived
     * @return A {@code List} of mappings
     */
    @SuppressWarnings("deprecation")
    // Processing of PropertyMapping is retained for compatibility and will be removed in a version after 2.0.2
    private static List<String> getMappings(Annotation annotation) {
        Stream<String> mappingsByAnnotationRendering = Optional.ofNullable(annotation)
            .map(Annotation::annotationType)
            .map(annotationType -> annotationType.getAnnotation(AnnotationRendering.class))
            .map(AnnotationRendering::properties)
            .map(ArrayUtil::flatten)
            .map(Arrays::stream)
            .orElse(Stream.empty());

        Stream<String> mappingsByPropertyMapping = Optional.ofNullable(annotation)
            .map(Annotation::annotationType)
            .map(annotationType -> annotationType.getAnnotation(PropertyMapping.class))
            .map(PropertyMapping::mappings)
            .map(ArrayUtil::flatten)
            .map(Arrays::stream)
            .orElse(Stream.empty());

        List<String> cumulativeMappings = Stream.concat(mappingsByAnnotationRendering, mappingsByPropertyMapping)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toList());

        if (cumulativeMappings.stream().allMatch(mapping -> mapping.startsWith(DialogConstants.NEGATION))) {
            cumulativeMappings.add(DialogConstants.WILDCARD);
        }
        return cumulativeMappings.stream()
            .map(mapping -> DialogConstants.VALUE_ALL.equals(mapping) ? DialogConstants.WILDCARD : mapping)
            .distinct()
            .collect(Collectors.toList());
    }
}
