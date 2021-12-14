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

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;

import com.exadel.aem.toolkit.api.annotations.injectors.Child;
import com.exadel.aem.toolkit.core.lists.models.internal.ListItemModel;

@Model(adaptables = SlingHttpServletRequest.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class TestModelChild {

    /* ------------------------------
         @Child injector test cases
       ------------------------------ */

    @Child
    private Resource list;

    @Child(name = "list")
    private Resource listItemResource;

    @Child(name = "./list")
    private Resource listItemResource2;

    @Child(name = "./list/list_item_1")
    private Resource nestedResource;

    @Child(name = "/list/list_item_1")
    private ListItemModel listItemModel;

    @Child(prefix = "list/list_item_")
    private ListItemModel listItemModel2;

    @Child(postfix = "/list/nested-node/_list2")
    private ListItemModel listItemModel3;

    /* ------------------------------
               Invalid cases
       ------------------------------ */

    @Child
    private Resource notExistedResource;

    @Child(name = "notExistedModel")
    private ListItemModel notExistedModel;

    @Child(prefix = "/list/notExistedPrefix_")
    private ListItemModel notExistedPrefix;

    @Child(postfix = "./list/_notExistedPostfix")
    private ListItemModel notExistedPostfix;

    /* ------------------------------
            Accessor valid cases
       ------------------------------ */

    public Resource getList() {
        return list;
    }

    public Resource getListItemResource() {
        return listItemResource;
    }

    public Resource getListItemResource2() {
        return listItemResource2;
    }

    public Resource getNestedResource() {
        return nestedResource;
    }

    public ListItemModel getListItemModel() {
        return listItemModel;
    }
    public ListItemModel getListItemModel2() {
        return listItemModel2;
    }

    public ListItemModel getListItemModel3() {
        return listItemModel3;
    }

    /* ------------------------------
         Accessor for invalid cases
       ------------------------------ */

    public Resource getNotExistedResource() {
        return notExistedResource;
    }

    public ListItemModel getNotExistedModel() {
        return notExistedModel;
    }

    public ListItemModel getNotExistedPrefix() {
        return notExistedPrefix;
    }

    public ListItemModel getNotExistedPostfix() {
        return notExistedPostfix;
    }
}
