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
package com.exadel.aem.toolkit.core.injectors.models.lists;

import java.util.Objects;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
@SuppressWarnings("unused")
public class CustomListItem {

    @ValueMapValue
    private String textValue;

    @ValueMapValue
    private boolean booleanValue;

    @ValueMapValue
    private int intValue;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomListItem that = (CustomListItem) o;
        return booleanValue == that.booleanValue && intValue == that.intValue && Objects.equals(textValue, that.textValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(textValue, booleanValue, intValue);
    }
}
