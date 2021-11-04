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

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

/**
 * Common methods for the injectors.
 */
public class InjectorUtils {

    public static Resource getResource(Object adaptable) {
        if (adaptable instanceof SlingHttpServletRequest) {
            return ((SlingHttpServletRequest) adaptable).getResource();
        }
        if (adaptable instanceof Resource) {
            return (Resource) adaptable;
        }
        return null;
    }

    public static SlingHttpServletRequest getSlingHttpServletRequest(Object adaptable) {
        if (adaptable instanceof SlingHttpServletRequest) {
            return ((SlingHttpServletRequest) adaptable);
        }
        return null;
    }

    public static ValueMap getValueMap(Object adaptable) {
        if (adaptable instanceof SlingHttpServletRequest) {
            return ((SlingHttpServletRequest) adaptable).getResource().adaptTo(ValueMap.class);
        } else if (adaptable instanceof ValueMap) {
            return ((ValueMap) adaptable);
        }
        return ValueMap.EMPTY;
    }
}
