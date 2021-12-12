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

import com.exadel.aem.toolkit.core.injectors.ChildInjector;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.models.annotations.Source;
import org.apache.sling.models.spi.injectorspecific.InjectAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used on either a field, a method, or a method parameter of a Sling model to inject a requested child resources.
 * Otherwise. nothing is injected
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@InjectAnnotation
@Source(ChildInjector.NAME)
public @interface Child {

    /**
     * Retrieves the relative path to child node
     * @return Optional non-blank string
     */
    String name() default StringUtils.EMPTY;

    /**
     * Retrieves relative path to child node with the last node as a prefix
     * @return Optional non-blank string
     */
    String namePrefix() default StringUtils.EMPTY;

    /**
     * Retrieves relative path to child node with the last node as a postfix
     * @return Optional non-blank string
     */
    String namePostfix() default StringUtils.EMPTY;
}
