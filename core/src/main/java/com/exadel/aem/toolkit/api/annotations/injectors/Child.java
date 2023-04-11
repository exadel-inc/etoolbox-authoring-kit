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

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Source;
import org.apache.sling.models.spi.injectorspecific.InjectAnnotation;

import com.exadel.aem.toolkit.core.injectors.ChildInjector;

/**
 * Used on a field, a method, or a method parameter of a Sling model to inject one of the following: a child/relative
 * resource of the current one; the current resource itself; or another Sling model based on one of the previous. Use
 * "{@code ..}" to traverse to a parent resource. Use "{@code ./}" to refer to the current resource itself.
 * <p>You can choose to use only a subset of properties of the given resource by specifying their common {@code prefix}
 * and/or {@code postfix}.</p>
 * <p>The type of the annotated Java class member is expected to be {@link Resource} or a
 * class adaptable from {@code Resource} or else {@link SlingHttpServletRequest}. If an array, a {@code Collection},
 * {@code List}, or {@code Set} is specified instead of a simple type, an array/collection consisting of just one member
 * is injected. Otherwise, nothing is injected
 * @see Children
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@InjectAnnotation
@Source(ChildInjector.NAME)
public @interface Child {

    /**
     * Used to provide the relative path to the child resource. If not specified, defaults to the name of the underlying
     * field/method. If such a resource does not exist, nothing is injected.
     * <p>Note: use {@code ./} to position the very current resource as the source of injection (the prefix and postfix
     * values will apply to the properties of the current resource)
     * @return Optional non-blank string
     */
    String name() default StringUtils.EMPTY;

    /**
     * Used to specify the prefix. If set to a non-blank string, the properties of the child/relative resource that
     * starts with the given value will be used for injection, while others will be skipped
     * @return Optional non-blank string
     */
    String prefix() default StringUtils.EMPTY;

    /**
     * Used to specify the postfix. If set to a non-blank string, the properties of the child/relative resource that end
     * with the given value will be used for injection, while others will be skipped
     * @return Optional non-blank string
     */
    String postfix() default StringUtils.EMPTY;
}
