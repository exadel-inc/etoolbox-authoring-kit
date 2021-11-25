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

import com.exadel.aem.toolkit.core.injectors.EToolboxListInjector;

/**
 * Used on either a field, a method, or a method parameter of a Sling model to inject EToolbox Lists
 * obtained via {@code ResourceResolver} instance. <p>Injects list values of the same type that the ListHelper retrieves.
 * The annotation contains elements: {@code value()} and {@code keyProperty()}.
 * <p> The {@code value()} annotation element sets the JCR path of the items list.
 * <p> The {@code keyProperty()} annotation element sets the item resource property that holds the key of the resulting map.
 * <p>If the annotated member is of type {@code Collection<T>}, {@code List<T>}, {@code Map<String, T>},
 * {@code T[]} <p> the collection or array of list entries stored in the specified {@code JCR path}
 * will be injected. <p> If {@code value()} is empty or type is wrong nothing will be injected.
 * Supports only models that are adaptable from request and resource.
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@InjectAnnotation
@Source(EToolboxListInjector.NAME)
public @interface EToolboxList {

    /**
     * Retrieves the JCR path of the items list
     * @return Optional non-blank string
     */
    String value() default "";

    /**
     * Retrieves the property name of the underlying resource specified by the given {@code keyProperty}.
     * <p>Can be used only with the collection type Map, otherwise returns null
     * @return Optional non-blank string
     */
    String keyProperty() default "";
}
