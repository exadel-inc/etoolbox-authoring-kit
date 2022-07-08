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

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;

import com.exadel.aem.toolkit.api.annotations.injectors.Child;
import com.exadel.aem.toolkit.core.lists.models.SimpleListItem;
import com.exadel.aem.toolkit.core.lists.models.internal.ListItemModel;

@Model(adaptables = SlingHttpServletRequest.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
@SuppressWarnings("unused")
public class TestModelChild {

    @Child
    private SimpleListItem defaultChild;

    @Child(name = "..")
    private Resource parent;

    @Child(name = "list")
    private Resource listResource;

    @Child(name = "./list_item_1")
    private Object listItemResource;

    @Child(name = "/content/jcr:content/list/nested-node/nested_list_item_2")
    private ListItemModel modelByAbsolutePath;

    @Child(name = "list/nested-node/nested_list_item_2")
    private ListItemModel modelByRelativePath;

    @Child(name = "/content/jcr:content/list/nested-node/nested_list_item_2", prefix = "prefix_")
    private ListItemModel modelFilteredByPrefix;

    @Child(name = "/content/jcr:content/list/nested-node/nested_list_item_2", postfix = "_postfix")
    private ListItemModel modelFilteredByPostfix;

    @Child(name = "/content/jcr:content/list/nested-node/nested_list_item_2", prefix = "foo_", postfix = "_bar")
    private ListItemModel modelFilteredByPrefixAndPostfix;

    @Child(name = "list/list_item_1006003058")
    private NestedModel modelAdaptedFromRequest;

    @Child(name = "./", prefix = "prefix_")
    private Resource selfResourceFiltered;

    private final Resource constructorArgument1;
    private final ListItemModel constructorArgument2;

    @Inject
    public TestModelChild(
        @Child @Named("./list_item_1") Resource constructorArgument1,
        @Child @Named("list/nested-node/nested_list_item_2") ListItemModel constructorArgument2) {

        this.constructorArgument1 = constructorArgument1;
        this.constructorArgument2 = constructorArgument2;
    }

    public SimpleListItem getDefaultChild() {
        return defaultChild;
    }

    public Resource getParent() {
        return parent;
    }

    public Resource getListResource() {
        return listResource;
    }

    public Object getListItemResource() {
        return listItemResource;
    }

    public ListItemModel getModelByAbsolutePath() {
        return modelByAbsolutePath;
    }

    public ListItemModel getModelByRelativePath() {
        return modelByRelativePath;
    }

    public ListItemModel getModelFilteredByPrefix() {
        return modelFilteredByPrefix;
    }

    public ListItemModel getModelFilteredByPostfix() {
        return modelFilteredByPostfix;
    }

    public ListItemModel getModelFilteredByPrefixAndPostfix() {
        return modelFilteredByPrefixAndPostfix;
    }

    public NestedModel getModelAdaptedFromRequest() {
        return modelAdaptedFromRequest;
    }

    public Resource getSelfResourceFiltered() {
        return selfResourceFiltered;
    }

    public Resource getConstructorArgument1() {
        return constructorArgument1;
    }

    public ListItemModel getConstructorArgument2() {
        return constructorArgument2;
    }
}
