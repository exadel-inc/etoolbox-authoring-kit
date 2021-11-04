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

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Source;
import org.apache.sling.models.spi.injectorspecific.InjectAnnotation;

/**
 * <code>Expression annotation</code> accepts a string expression containing possible values.
 * <p>
 * Annotation injects the same sort of values the ValueMapValue injector.
 * Injector takes {@link Resource} if adaptable is the {@link SlingHttpServletRequest} and gets property from the {@link ValueMap}.
 * <p>
 * If the annotation name contains a logical OR operator like <code>"varOne||varTwo||'Message'"</code> injector tries to inject first property
 * and if it is missing, tries to inject the value of next property, if no value wasn't found
 * and annotation name contains message in single quotes <code>'...'</code> it will inject the message from the single quotes.
 * If annotation name is the ternary operator <code>"varOne != 10 ? varOne : 23"</code>
 * computes the value map value of varOne and proceeds with a ternary.
 * If annotation name contains prefix and value, for example: <code>"'My Text' + value"</code> it injects value with prefix.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@InjectAnnotation
@Source(ExpressionInjector.NAME)
public @interface Expression {

    String name() default "";
}
