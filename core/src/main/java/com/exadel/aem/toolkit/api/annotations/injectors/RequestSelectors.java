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
package com.exadel.aem.toolkit.api.annotations.injectors;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.sling.models.annotations.Source;
import org.apache.sling.models.spi.injectorspecific.InjectAnnotation;

import com.exadel.aem.toolkit.core.injectors.RequestSelectorsInjector;

/**
 * Used on a field, a method, or a method parameter of a Sling model to inject Sling request selectors.
 * <p>If the annotated Java class member is of type {@code String} or {@code Object}, the selector string is injected.
 * Otherwise, the annotated member can be a string of any primitive type or a boxed variant. Each selector is then
 * considered a string and parsed to the target type.</p>
 * <p>The annotated member can represent an array, or else a {@link Collection}, a {@link List}, or a {@link Set}.
 * Selectors are then injected into the collection one by one.</p>
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@InjectAnnotation
@Source(RequestSelectorsInjector.NAME)
public @interface RequestSelectors {
}
