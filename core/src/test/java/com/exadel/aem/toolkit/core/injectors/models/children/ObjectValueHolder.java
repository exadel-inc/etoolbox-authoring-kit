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
package com.exadel.aem.toolkit.core.injectors.models.children;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.sling.api.resource.Resource;
import com.day.cq.commons.jcr.JcrConstants;

interface ObjectValueHolder {

    Object getRawObjectValue();

    default List<Resource> getCastObjectValue() {
        if (!(getRawObjectValue() instanceof List)) {
            return Collections.emptyList();
        }
        return ((List<?>) getRawObjectValue())
            .stream()
            .filter(Resource.class::isInstance)
            .map(Resource.class::cast)
            .filter(resource -> resource.getValueMap().containsKey(JcrConstants.JCR_TITLE))
            .collect(Collectors.toList());
    }
}
