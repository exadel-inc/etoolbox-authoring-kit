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
package com.exadel.aem.toolkit.core.injectors.models;

import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;

import com.exadel.aem.toolkit.api.annotations.injectors.Children;
import com.exadel.aem.toolkit.core.injectors.models.filters.NameFilter;
import com.exadel.aem.toolkit.core.injectors.models.filters.ResourceTypeFilter;
import com.exadel.aem.toolkit.core.lists.models.internal.ListItemModel;

@Model(adaptables = SlingHttpServletRequest.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
@SuppressWarnings("unused")
public class TestModelChildren {

    /* -----------
       Valid cases
       ----------- */

    @Children
    private List<Resource> list;

    @Children(name = "./")
    private List<Object> ownList;

    @Children(name = "./list")
    private List<Resource> resourceList;

    @Children(name = "./list")
    private List<NestedModel> nestedModelList;

    @Children(name = "./list/nested-node")
    private Collection<ListItemModel> listItemModels;

    @Children(name = "/content/jcr:content/list/nested-node", prefix = "prefix_")
    private ListItemModel[] listItemModelsWithPrefix;

    @Children(name = "/content/jcr:content/list/nested-node", postfix = "_postfix")
    private List<ListItemModel> listItemModelsWithPostfix;

    @Children(name = "/content/jcr:content/list/nested-node", prefix = "prefix_", postfix = "_postfix")
    private List<ListItemModel> listItemModelsWithPrefixAndPostfix;

    @Children(name = "list", filters = {ResourceTypeFilter.class, NameFilter.class})
    private List<ListItemModel> listItemModelsFiltered;

    /* -------------
       Invalid cases
       ------------- */

    @Children(name = "./nonExistentPath")
    private List<Resource> nonExistentResources;

    @Children(name = "nonExistent/path/route")
    private List<ListItemModel> notExistentModel;

    @Children(name = "/content/jcr:content/list/nested-node", prefix = "noprefix_")
    private List<ListItemModel> nonExistentPrefix;

    @Children(name = "list/nestedNode", filters = {ResourceTypeFilter.class, NameFilter.class})
    private List<ListItemModel> unmatchedFilters;

    private final ListItemModel[] injectedViaConstructor;

    /* -----------
       Constructor
       ----------- */

    @Inject
    public TestModelChildren(
        @Children
        @Named("./list/nested-node")
        ListItemModel[] injected) {
        this.injectedViaConstructor = injected;
    }

    /* -------------------------
       Accessors for valid cases
       ------------------------- */

    public List<Resource> getList() {
        return list;
    }

    public List<Object> getOwnList() {
        return ownList;
    }

    public List<NestedModel> getNestedModelList() {
        return nestedModelList;
    }

    public List<Resource> getResourceList() {
        return resourceList;
    }

    public Collection<ListItemModel> getListItemModels() {
        return listItemModels;
    }

    public ListItemModel[] getListItemModelsWithPrefix() {
        return listItemModelsWithPrefix;
    }

    public List<ListItemModel> getListItemModelsWithPostfix() {
        return listItemModelsWithPostfix;
    }

    public List<ListItemModel> getListItemModelsWithPrefixAndPostfix() {
        return listItemModelsWithPrefixAndPostfix;
    }

    public List<ListItemModel> getListItemModelsFiltered() {
        return listItemModelsFiltered;
    }

    public ListItemModel[] getInjectedViaConstructor() {
        return injectedViaConstructor;
    }

    /* ---------------------------
       Accessors for invalid cases
       --------------------------- */

    public List<Resource> getNonExistentResources() {
        return nonExistentResources;
    }

    public List<ListItemModel> getNonExistentModel() {
        return notExistentModel;
    }

    public List<ListItemModel> getNonExistentPrefix() {
        return nonExistentPrefix;
    }

    public List<ListItemModel> getUnmatchedFilters() {
        return unmatchedFilters;
    }
}
