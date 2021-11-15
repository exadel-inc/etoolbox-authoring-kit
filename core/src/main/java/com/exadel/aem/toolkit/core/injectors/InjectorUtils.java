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

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

/**
 * Common methods for the injectors.
 */
public class InjectorUtils {

    /**
     * Default (instantiation-restricting) constructor.
     */
    private InjectorUtils() {
    }

    /**
     * Retrieves the {@code SlingHttpServletRequest}.
     * @param adaptable The object which Sling tries to adapt from.
     * @return {@code SlingHttpServletRequest} if adaptable is an instance of {@code SlingHttpServletRequest} or null if not.
     */
    public static SlingHttpServletRequest getSlingHttpServletRequest(Object adaptable) {
        if (adaptable instanceof SlingHttpServletRequest) {
            return ((SlingHttpServletRequest) adaptable);
        }
        return null;
    }

    /**
     * Retrieves the {@code Resource} if adaptable is an instance of {@code SlingHttpServletRequest} or {@code Resource}.
     * @param adaptable The object which Sling tries to adapt from.
     * @return {@code Resource} or null if no instance could be found.
     */
    public static Resource getResource(Object adaptable) {
        if (adaptable instanceof SlingHttpServletRequest) {
            return ((SlingHttpServletRequest) adaptable).getResource();
        }
        if (adaptable instanceof Resource) {
            return (Resource) adaptable;
        }
        return null;
    }

    /**
     * Retrieves the {@code ValueMap} if adaptable is an instance of {@code SlingHttpServletRequest} or {@code Resource} or {@code ValueMap}.
     * @param adaptable The object which Sling tries to adapt from.
     * @return {@code ValueMap} or empty ValueMap if no instance could be found.
     */
    public static ValueMap getValueMap(Object adaptable) {
        if (adaptable instanceof SlingHttpServletRequest) {
            return ((SlingHttpServletRequest) adaptable).getResource().adaptTo(ValueMap.class);
        } else if (adaptable instanceof Resource) {
            return ((Resource) adaptable).adaptTo(ValueMap.class);
        } else if (adaptable instanceof ValueMap) {
            return ((ValueMap) adaptable);
        }
        return ValueMap.EMPTY;
    }
}
