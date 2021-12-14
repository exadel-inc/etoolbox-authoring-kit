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

/**
 * Used on either a field, a method, or a method parameter of a Sling model to inject a collection of children,
 * all elements in the collection will be adapted to the collection's parameterized type if success, otherwise null returned.
 * <p>If the user has not specified name, prefix, or postfix all children resources of the current resource will be injected</p>
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@InjectAnnotation
@Source(ChildrenInjector.NAME)
public @interface Children {

    /**
     * Used to specify a relative path to the parent resource, all of its children resources will be injected.
     * @return Optional non-blank string
     */
    String name() default StringUtils.EMPTY;

    /**
     * Used to specify the prefix.
     * All children resources that will match the prefix will be injected, if there are no matches null will be returned.
     * The prefix also can be specified as a relative path, in this case, the prefix will be specified at the end of the path:
     * <pre>{@code
     *     @Child(prefix = "/components/prefix_")
     * }</pre>
     * @return Optional non-blank string
     */
    String prefix() default StringUtils.EMPTY;

    /**
     * Used to specify the postfix.
     * All children resources that will match the postfix will be injected, if there are no matches null will be returned.
     * The postfix also can be specified as a relative path, in this case, the postfix will be specified at the end of the path:
     * <pre>{@code
     *     @Child(postfix = "/components/_postfix")
     * }</pre>
     * @return Optional non-blank string
     */
    String postfix() default StringUtils.EMPTY;

    /**
     * Used to specify predicates array.
     * Resources will be filtered according to these predicates. If there will be no matches, null will be returned.
     * @return Optional non-blank array of predicates
     */
    Class<? extends Predicate<Resource>>[] filters() default {};
}
