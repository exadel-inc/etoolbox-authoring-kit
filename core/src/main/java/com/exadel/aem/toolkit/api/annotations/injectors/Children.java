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
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Source;
import org.apache.sling.models.spi.injectorspecific.InjectAnnotation;

import com.exadel.aem.toolkit.core.injectors.ChildrenInjector;
import com.exadel.aem.toolkit.core.injectors.filters.NonGhostFilter;
import com.exadel.aem.toolkit.core.injectors.filters.NonNullFilter;

/**
 * Used on either a field, a method, or a method parameter of a Sling model. Allows injecting a collection of either
 * secondary models derived from the child (relative) resources of a target resource or such resources themselves. One
 * can select particular properties used for injection by specifying their common prefix and/or postfix, or else provide
 * a filtering {@link Predicate}.
 * <p>Unlike in {@link Child}, one does not specify the precise Sling object to inject but instead the "parent" object
 * children of which are injected.
 * <p>The type of the underlying Java array (parameter type of Java collection) must be {@link
 * org.apache.sling.api.resource.Resource} or a class that is adaptable from {@code Resource}. Otherwise, nothing is
 * injected
 * @see Child
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@InjectAnnotation
@Source(ChildrenInjector.NAME)
public @interface Children {

    /**
     * Used to specify the relative path to the common "parent" of the children that need to be injected. If not
     * specified, defaults to the name of the underlying field/method. If such a resource does not exist, nothing is
     * injected
     * <p>Note: use {@code ./} to consider the current resource as the parent resource
     * @return Optional non-blank string
     */
    String name() default StringUtils.EMPTY;

    /**
     * Used to specify the prefix. If set to a non-blank string, the properties of the child (relative) resource that
     * start with the given value will be used for injection, while others will be skipped
     * @return Optional non-blank string
     */
    String prefix() default StringUtils.EMPTY;

    /**
     * Used to specify the prefix. If set to a non-blank string, the properties of the child (relative) resource that
     * end with the given value will be used for injection, while others will be skipped
     * @return Optional non-blank string
     */
    String postfix() default StringUtils.EMPTY;

    /**
     * Used to specify filters for the children The filters are a sequence of {@code Predicate<Resource>} instances
     * referred to with their class names. Resources will be probed against these predicates. One possible predicate
     * is "this resource is not a ghost component", or else "this resource is not null"
     * @return Optional array of predicates
     * @see NonGhostFilter
     * @see NonNullFilter
     */
    Class<? extends Predicate<Resource>>[] filters() default {};
}
