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
package com.exadel.aem.toolkit.core.injectors;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.sling.models.annotations.Source;
import org.apache.sling.models.spi.injectorspecific.InjectAnnotation;

import com.exadel.aem.toolkit.api.annotations.injectors.RequestAttribute;
import com.exadel.aem.toolkit.api.annotations.injectors.RequestParam;
import com.exadel.aem.toolkit.api.annotations.injectors.RequestSelectors;
import com.exadel.aem.toolkit.api.annotations.injectors.RequestSuffix;

/**
 * This is a testing-scope annotation that stands in place of either {@link RequestAttribute}, {@link RequestParam},
 * {@link RequestSelectors} or {@link RequestSuffix} (probably more) to make it possible to use the same testcase Sling
 * models for different injection scenarios
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@InjectAnnotation
@Source(DelegateInjector.NAME)
public @interface RequestProperty {
    String name() default "";
}
