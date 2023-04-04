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

import org.apache.sling.models.annotations.Source;
import org.apache.sling.models.spi.injectorspecific.InjectAnnotation;

import com.exadel.aem.toolkit.core.injectors.RequestAttributeInjector;

/**
 * Used on a field, a method, or a method parameter of a Sling model to inject the value of an arbitrary request
 * attribute.
 * <p>The annotated member can be of an arbitrary object type or primitive type. It can also be an array, or else a
 * {@link Collection}, a {@link List} or a {@code Set} of arbitrary object types/primitives.</p>
 * <p>While injecting type coercion is performed. A numeric value can be cast to a widening type ({@code byte -> int},
 * {@code int -> long}, {@code long -> double}, etc.) and also boxed/unboxed if needed. A provided string is parsed into
 * a numeric or boolean if the receiving member has this type. Similarly, an object type can be cast to an ancestor class or
 * an interface. If the injectable member is a collection/array, and the injected value is singular, a singleton
 * collection/array is created</p>
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@InjectAnnotation
@Source(RequestAttributeInjector.NAME)
public @interface RequestAttribute {

    /**
     * Used to specify the name of the attribute to inject if it differs from the name of the underlying Java class
     * member
     * @return Optional non-blank string
     */
    String name() default "";
}
