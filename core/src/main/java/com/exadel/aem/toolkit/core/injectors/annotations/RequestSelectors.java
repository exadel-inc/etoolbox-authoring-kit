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
package com.exadel.aem.toolkit.core.injectors.annotations;

import com.exadel.aem.toolkit.core.injectors.RequestSelectorsInjector;

import org.apache.sling.models.annotations.Source;
import org.apache.sling.models.spi.injectorspecific.InjectAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation can be used on either methods, fields or parameters to let Sling Models inject request
 * selectors from the RequestPathInfo into fields of type String, List<String>, Collection<String> or String[]
 * or null if type is wrong.
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@InjectAnnotation
@Source(RequestSelectorsInjector.NAME)
public @interface RequestSelectors {

}
