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
package com.exadel.aem.toolkit.core.injectors.filters;

import java.util.Objects;
import java.util.function.Predicate;

import org.apache.sling.api.resource.Resource;

/**
 * The predicate that checks if a resource is non-null
 */
public class NonNullPredicate implements Predicate<Resource> {

    /**
     * Evaluates this predicate on the given argument.
     *
     * @param resource the input argument
     * @return {@code true} if the input argument matches the predicate,
     * otherwise {@code false}
     */
    @Override
    public boolean test(Resource resource) {
        return Objects.nonNull(resource);
    }
}
