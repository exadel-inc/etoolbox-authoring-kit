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

import org.apache.sling.models.annotations.Source;
import org.apache.sling.models.spi.injectorspecific.InjectAnnotation;

/**
 * <code>RequestSelectors annotation</code> can be used on fields to let Sling Models inject selectors from the RequestPathInfo.
 * <p>
 * Annotation injects selectors into a field of type String or List <String> or String [].
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@InjectAnnotation
@Source(RequestSelectorsInjector.NAME)
public @interface RequestSelectors {
}
