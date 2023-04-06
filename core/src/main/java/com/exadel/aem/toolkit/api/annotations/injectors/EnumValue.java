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
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.apache.sling.models.spi.injectorspecific.InjectAnnotation;

import com.exadel.aem.toolkit.core.injectors.EnumValueInjector;

/**
 * Used on a field, a method, or a method parameter of a Sling model to inject a value of an {@link Enum}.
 * <p>The annotated Java class member is expected to be an enum type. It can also be an array, a {@link Collection},
 * a {@link List}, or a {@link Set} of enum constants.</p>
 * <p>The source value for an enum constant is extracted from the current resource's {@code ValueMap} in the same
 * manner that the injector for the standard @{@link ValueMapValue} would do. The value map value needs to be a string
 * that is compared to enum constants' {@code name()} values, then to the return values of enum constants'
 * {@code toString()} methods, or else to the return value of a specified arbitrary enum method/field. The comparison is
 * case-insensitive</p>
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@InjectAnnotation
@Source(EnumValueInjector.NAME)
public @interface EnumValue {

    /**
     * Used to specify the name or path of the value map parameter if it differs from the name of the underlying Java
     * class member
     * @return Optional non-blank string
     */
    String name() default "";

    /**
     * Used to specify the name of an enum object member (a method or a field) used to match an enum constant to a
     * string-typed value map value. If set, it overrides the default behavior when the matching enum constant is
     * searched by the value of the {@code name()} method and then the return value of the {@code toString()} method
     * @return Optional non-blank string
     */
    String valueMember() default "";
}
