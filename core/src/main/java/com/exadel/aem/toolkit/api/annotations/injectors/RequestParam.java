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

import com.exadel.aem.toolkit.core.injectors.RequestParamInjector;

/**
 * Used on a field, a method, or a method parameter of a Sling model to inject a request parameter.
 * <p>If the annotated Java class member is of type  {@code RequestParameter}, {@code RequestParameter[]} or
 * {@code RequestParameterMap}, values of the correspondent type are injected.</p>
 * <p>Otherwise, the annotated member can be a string or of any primitive type or a boxed variant. The request
 * parameter is then considered a string and parsed to the target type. Array-typed values are supported, as well as
 * {@link Collection}s, {@link List}s, and {@link Set}s. If the annotated member if of array/collection type but the
 * request parameter is singular, a one-entry collection is created.</p>
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@InjectAnnotation
@Source(RequestParamInjector.NAME)
public @interface RequestParam {

    /**
     * Used to specify the parameter to inject if its name differs from the name of the underlying Java class member. If
     * the underlying member is of type {@code List<RequestParameter>}, {@code RequestParameter[]} or
     * {@code RequestParameterMap}, this value is ignored
     * @return Optional non-blank string
     */
    String name() default "";
}
