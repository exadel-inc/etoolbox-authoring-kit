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

import com.exadel.aem.toolkit.core.injectors.RequestParamInjector;

/**
 * Used on either a field, a method, or a method parameter of a Sling model to inject a request parameter.
 * <p>If the annotated member is of type {@code String} or {@code Object}, the string value is injected. If the
 * annotated member is a {@code Collection}, {@code List}, or an array of strings , the array or list if string
 * values is injected. If the annotated member is of type {@code RequestParameter}, {@code RequestParameter[]} or {@code
 * RequestParameterMap}, the corresponding objects obtained via the {@code SlingHttpServletRequest} instance are
 * injected. Otherwise, nothing is injected</p>
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@InjectAnnotation
@Source(RequestParamInjector.NAME)
public @interface RequestParam {

    /**
     * Retrieves the name of the parameter to inject if it differs from the name of the underlying Java class member. If
     * the underlying member is of type {@code List<RequestParameter>}, {@code RequestParameter[]} or {@code
     * RequestParameterMap}, this value is ignored
     * @return Optional non-blank string
     */
    String name() default "";
}
