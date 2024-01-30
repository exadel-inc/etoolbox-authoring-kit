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
package com.exadel.aem.toolkit.api.annotations.policies;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.ValueRestriction;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestrictions;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxChildren {
    /**
     * Specifies the maximum number of children allowed for container component
     * @return Long value, 0 or greater
     */
    @ValueRestriction(ValueRestrictions.NON_NEGATIVE)
    long value();

    /**
     * Used to specify target node for max children limit. The limitation applies to the current annotated component
     * if set to {@link PolicyTarget#CURRENT}. Otherwise, the limitation is applied to a container nested within the current
     * component. E.g. if the current component is an inheritor of parsys, setting {@code targetContainer} to {@link
     * PolicyTarget#CURRENT} means that the limitation affects which components can be added to the current
     * one. But if the current component contains a parsys inside you need to skip {@code targetContainer} or set it to
     * {@link PolicyTarget#CHILD} so that the limitation applies to the parsys.
     * @return {@link PolicyTarget} value
     */
    PolicyTarget targetContainer() default PolicyTarget.CHILD;
}
