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
import java.util.Map;
import java.util.Queue;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;

import com.exadel.aem.toolkit.api.annotations.injectors.EToolboxList;
import com.exadel.aem.toolkit.core.injectors.EToolboxListInjectorTest;
import com.exadel.aem.toolkit.core.lists.models.SimpleListItem;

@Model(adaptables = SlingHttpServletRequest.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
@SuppressWarnings("unused")
public class TestModelEToolboxList {

    // Valid cases

    @EToolboxList("/content/etoolbox-lists/contentList")
    private List<Resource> itemsListResource;

    @EToolboxList("/content/etoolbox-lists/contentList")
    private Collection<Resource> itemsCollectionResource;

    @EToolboxList("/content/etoolbox-lists/contentList")
    private List<SimpleListItem> itemsListSimpleList;

    @EToolboxList("/content/etoolbox-lists/customContentList")
    private List<EToolboxListInjectorTest.LocalListItemModel> itemsListTestModel;

    @EToolboxList("/content/etoolbox-lists/contentList")
    private Map<String, Resource> itemsMapResource;

    @EToolboxList("/content/etoolbox-lists/contentList")
    private Map<String, String> itemsMapString;

    @EToolboxList(value = "/content/etoolbox-lists/customContentList", keyProperty = "textValue")
    private Map<String, EToolboxListInjectorTest.LocalListItemModel> itemsMapTestModel;

    @EToolboxList("/content/etoolbox-lists/customContentList")
    private EToolboxListInjectorTest.LocalListItemModel[] itemsArrayModel;

    @EToolboxList("/content/etoolbox-lists/contentList")
    private List<Object> itemsListObject;

    @EToolboxList("/content/etoolbox-lists/contentList")
    private Map<Object, Object> itemsMapObjects;

    private final List<Resource> itemsListResourceFromMethodParameter;

    // Constructor

    @Inject
    public TestModelEToolboxList(@EToolboxList("/content/etoolbox-lists/contentList") @Named List<Resource> listResource) {
        this.itemsListResourceFromMethodParameter = listResource;
    }

    // Invalid cases

    @EToolboxList("/content/etoolbox-lists/contentList")
    private Queue<Resource> itemsWithWrongCollectionType;

    @EToolboxList("/content/etoolbox-lists/contentList")
    private String itemsWithWrongFieldType;

    // Accessors - Valid cases

    public List<Resource> getItemsListResource() {
        return itemsListResource;
    }

    public Collection<Resource> getItemsCollectionResource() {
        return itemsCollectionResource;
    }

    public List<SimpleListItem> getItemsListSimpleList() {
        return itemsListSimpleList;
    }

    public List<EToolboxListInjectorTest.LocalListItemModel> getItemsListTestModel() {
        return itemsListTestModel;
    }

    public Map<String, Resource> getItemsMapResource() {
        return itemsMapResource;
    }

    public Map<String, String> getItemsMapString() {
        return itemsMapString;
    }

    public Map<String, EToolboxListInjectorTest.LocalListItemModel> getItemsMapTestModel() {
        return itemsMapTestModel;
    }

    public List<Resource> getItemsListResourceFromMethodParameter() {
        return itemsListResourceFromMethodParameter;
    }

    public EToolboxListInjectorTest.LocalListItemModel[] getItemsArrayModel() {
        return itemsArrayModel;
    }

    public List<Object> getItemsListObject() {
        return itemsListObject;
    }

    public Map<Object, Object> getItemsMapObjects() {
        return itemsMapObjects;
    }

    // Accessors - Invalid cases

    public Queue<Resource> getItemsWithWrongCollectionType() {
        return itemsWithWrongCollectionType;
    }

    public String getItemsWithWrongFieldType() {
        return itemsWithWrongFieldType;
    }
}
