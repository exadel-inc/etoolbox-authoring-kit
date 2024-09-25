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

package com.exadel.aem.toolkit.core.injectors;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.spi.injectorspecific.AbstractInjectAnnotationProcessor2;
import org.apache.sling.models.spi.injectorspecific.InjectAnnotationProcessor2;
import org.apache.sling.models.spi.injectorspecific.StaticInjectAnnotationProcessorFactory;

/**
 * Extends {@link BaseInjector} with an default implementation of {@link InjectAnnotationProcessor2} to support
 * injecting into class members annotated with {@link Default}
 * @param <T> The type of annotation handled by this injector
 * @see BaseInjector
 */
abstract class DefaultAwareInjector<T extends Annotation>
    extends BaseInjector<T> implements StaticInjectAnnotationProcessorFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public InjectAnnotationProcessor2 createAnnotationProcessor(AnnotatedElement element) {
        if (getManagedAnnotation(element) == null) {
            return null;
        }
        return new AbstractInjectAnnotationProcessor2() {
            @Override
            public boolean hasDefault() {
                return element.isAnnotationPresent(Default.class);
            }
        };
    }
}
