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
package com.exadel.aem.toolkit.core.handlers.widget.common;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.function.BiConsumer;

import com.exadel.aem.toolkit.api.handlers.SourceFacade;
import com.exadel.aem.toolkit.api.handlers.TargetBuilder;

import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;

/**
 * Handler for storing sets of generic widget properties to a Granite UI widget XML node
 */
public class PropertyMappingHandler implements BiConsumer<SourceFacade, TargetBuilder> {
    /**
     * Processes the user-defined data and writes it to XML entity
     * @param source Current {@code SourceFacade} instance
     * @param target Current {@code TargetFacade} instance
     */
    @Override
    public void accept(SourceFacade source, TargetBuilder target) {
        Arrays.stream(source.adaptTo(Annotation[].class))
                .filter(a -> a.annotationType().isAnnotationPresent(PropertyMapping.class))
                .forEach(target::mapProperties);
    }
}
