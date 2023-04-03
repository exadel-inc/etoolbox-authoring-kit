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
import com.exadel.aem.toolkit.core.injectors.filters.NonGhostFilter;
import com.exadel.aem.toolkit.core.injectors.models.child.ExtendedListItem;
import com.exadel.aem.toolkit.core.injectors.models.child.InjectedWithSelection;
import com.exadel.aem.toolkit.core.injectors.models.children.filters.NameFilter;
import com.exadel.aem.toolkit.core.injectors.models.children.filters.ResourceTypeFilter;
import com.exadel.aem.toolkit.core.lists.models.SimpleListItem;

@Model(
    adaptables = {SlingHttpServletRequest.class, Resource.class},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class InjectedWithFilters {

    private static final String LIST_ITEM_PATH = "/list_item_4";

    @Children(
        name = Constants.CHILDREN_PATH + LIST_ITEM_PATH,
        postfix = InjectedWithSelection.POSTFIX,
        filters = NonGhostFilter.class)
    private List<ExtendedListItem.Nested> nonGhosts;

    @Children(
        name = Constants.CHILDREN_PATH,
        filters = {NameFilter.class, ResourceTypeFilter.class})
    private SimpleListItem[] byNameFilter;

    @Self
    private ValueSupplier supplier;

    private final List<Resource> valueFromConstructor;

    @Inject
    public InjectedWithFilters(
        @Children(
            name = Constants.CHILDREN_PATH + LIST_ITEM_PATH,
            postfix = InjectedWithSelection.POSTFIX,
            filters = NonGhostFilter.class)
        @Named
        List<Resource> value) {
        valueFromConstructor = value;
    }

    @Nullable
    public List<ExtendedListItem.Nested> getNonGhosts() {
        return nonGhosts;
    }

    @Nullable
    public SimpleListItem[] getByNameFilter() {
        return byNameFilter;
    }

    public List<Resource> getValueFromConstructor() {
        return valueFromConstructor;
    }

    @Nullable
    public ValueSupplier getSupplier() {
        return supplier;
    }

    @Model(adaptables = {SlingHttpServletRequest.class, Resource.class})
    public interface ValueSupplier {
        @Children(
            name = Constants.CHILDREN_PATH_ABSOLUTE,
            filters = ResourceTypeFilter.class)
        List<SimpleListItem> getByRestypeFilter();
    }
}
