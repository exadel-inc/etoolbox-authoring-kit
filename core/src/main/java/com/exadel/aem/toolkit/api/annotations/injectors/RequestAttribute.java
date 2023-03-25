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

import com.exadel.aem.toolkit.core.injectors.RequestAttributeInjector;

/**
 * Used on either a field, a method, or a method parameter of a Sling model to inject a request attribute.
 * <p>The annotated member can be of an arbitrary object or primitive type, or else a {@code List}, a {@code Set}, or
 * an array.</p>
 * <p>While injecting, a numeric value can be cast to a widening type if needed ({@code byte -> int}, {@code int ->
 * long}, {@code long -> double}, etc.) Similarly, an object type can be cast to an ancestor class or an interface. If
 * the injectable member is a collection/array, and the injected value is singular, a singleton collection/array is
 * created</p>
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
