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

import java.util.List;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

import com.exadel.aem.toolkit.api.annotations.injectors.Children;
import com.exadel.aem.toolkit.core.lists.models.SimpleListItem;

@Model(
    adaptables = {SlingHttpServletRequest.class, Resource.class},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class InjectedByMemberName {

    @Children
    private List<SimpleListItem> list;

    @Children(name = "list")
    private Object objectValue;

    @Self
    private ValueSupplier supplier;

    private final List<Resource> valueFromConstructor;

    @Inject
    public InjectedByMemberName(@Children @Named(Constants.CHILDREN_NAME) List<Resource> value) {
        valueFromConstructor = value;
    }

    @Nullable
    public List<SimpleListItem> getValue() {
        return list;
    }

    @Nullable
    public Object getObjectValue() {
        return objectValue;
    }

    @Nullable
    public ValueSupplier getSupplier() {
        return supplier;
    }

    public List<Resource> getValueFromConstructor() {
        return valueFromConstructor;
    }

    @Model(adaptables = {SlingHttpServletRequest.class, Resource.class})
    public interface ValueSupplier {
        @Children
        List<SimpleListItem> getList();
    }
}
