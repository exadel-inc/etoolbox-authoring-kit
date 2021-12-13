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

import com.exadel.aem.toolkit.api.annotations.injectors.Children;

import com.exadel.aem.toolkit.core.injectors.models.filters.NameFilter;
import com.exadel.aem.toolkit.core.injectors.models.filters.ResourceTypeFilter;
import com.exadel.aem.toolkit.core.lists.models.internal.ListItemModel;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;

import java.util.List;

@Model(adaptables = SlingHttpServletRequest.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class TestModelChildren {

    /* ------------------------------
       @Children injector test cases
       ------------------------------ */

    @Children
    private List<Resource> resourceItems;

    @Children(name = "./list")
    private List<Resource> resourceList;

    @Children(name = "list/nested-node")
    private List<ListItemModel> listItemModels2;

    @Children(namePrefix = "list/nested-node/nested_")
    private List<ListItemModel> listItemModels3;

    @Children(namePostfix = "list/nested-node/_list2")
    private List<ListItemModel> listItemModels4;

    @Children(name = "/list", filters = {ResourceTypeFilter.class, NameFilter.class})
    private List<ListItemModel> listItemModels5;

    @Children(filters = {ResourceTypeFilter.class})
    private List<ListItemModel> listItemModels6;

    /* ------------------------------
               Invalid cases
       ------------------------------ */

    @Children(name = "./notExistedPath")
    private List<Resource> notExistedResources;

    @Children(name = "notExisted/path/route")
    private List<ListItemModel> notExistedModel;

    @Children(namePrefix = "list/_notExistedPrefix")
    private List<ListItemModel> notExistedPrefix;

    @Children(namePostfix = "list/notExistedPostfix")
    private List<ListItemModel> notExistedPostfix;

    @Children(name = "/list/notExisted/path", filters = {ResourceTypeFilter.class, NameFilter.class})
    private List<ListItemModel> notExistedFilter;

    /* ------------------------------
            Accessor valid cases
       ------------------------------ */

    public List<Resource> getResourceItems() {
        return resourceItems;
    }

    public List<Resource> getResourceList() {
        return resourceList;
    }

    public List<ListItemModel> getListItemModels2() {
        return listItemModels2;
    }

    public List<ListItemModel> getListItemModels3() {
        return listItemModels3;
    }

    public List<ListItemModel> getListItemModels4() {
        return listItemModels4;
    }

    public List<ListItemModel> getListItemModels5() {
        return listItemModels5;
    }

    public List<ListItemModel> getListItemModels6() {
        return listItemModels6;
    }

    /* ------------------------------
         Accessor for invalid cases
       ------------------------------ */

    public List<Resource> getNotExistedResources() {
        return notExistedResources;
    }

    public List<ListItemModel> getNotExistedModel() {
        return notExistedModel;
    }

    public List<ListItemModel> getNotExistedPrefix() {
        return notExistedPrefix;
    }

    public List<ListItemModel> getNotExistedPostfix() {
        return notExistedPostfix;
    }

    public List<ListItemModel> getNotExistedFilter() {
        return notExistedFilter;
    }
}
