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
    private Collection<SimpleListItem> itemsCollectionSimpleList;

    @EToolboxList("/content/etoolbox-lists/contentList")
    private List<SimpleListItem> itemsListSimpleList;

    @EToolboxList("/content/etoolbox-lists/customContentList")
    private List<EToolboxListInjectorTest.ListItemModel> itemsListTestModel;

    @EToolboxList("/content/etoolbox-lists/contentList")
    private Map<String, Resource> itemsMapResource;

    @EToolboxList("/content/etoolbox-lists/contentList")
    private Map<String, String> itemsMapString;

    @EToolboxList(value = "/content/etoolbox-lists/customContentList", keyProperty = "textValue")
    private Map<String, EToolboxListInjectorTest.ListItemModel> itemsMapTestModel;

    @EToolboxList("/content/etoolbox-lists/customContentList")
    private EToolboxListInjectorTest.ListItemModel[] itemsArrayModel;

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
    private Map<Integer, Resource> itemsWithWrongMapKey;

    @EToolboxList("/content/etoolbox-lists/contentList")
    private String itemsWithWrongFieldType;

    @EToolboxList(value = "/content/etoolbox-lists/contentList", keyProperty = "value")
    private List<Resource> itemsListWithKeyValue;

    @EToolboxList
    private List<SimpleListItem> itemsWithoutAnnotationValue;

    @EToolboxList("/content/etoolbox-lists/contentList")
    private List<EToolboxListInjectorTest.NonModel> itemsNonModel;

    // Accessors - Valid cases

    public List<Resource> getItemsListResource() {
        return itemsListResource;
    }

    public Collection<SimpleListItem> getItemsCollectionSimpleList() {
        return itemsCollectionSimpleList;
    }

    public List<SimpleListItem> getItemsListSimpleList() {
        return itemsListSimpleList;
    }

    public List<EToolboxListInjectorTest.ListItemModel> getItemsListTestModel() {
        return itemsListTestModel;
    }

    public Map<String, Resource> getItemsMapResource() {
        return itemsMapResource;
    }

    public Map<String, String> getItemsMapString() {
        return itemsMapString;
    }

    public Map<String, EToolboxListInjectorTest.ListItemModel> getItemsMapTestModel() {
        return itemsMapTestModel;
    }

    public List<Resource> getItemsListResourceFromMethodParameter() {
        return itemsListResourceFromMethodParameter;
    }

    public EToolboxListInjectorTest.ListItemModel[] getItemsArrayModel() {
        return itemsArrayModel;
    }

    // Accessors - Invalid cases

    public Queue<Resource> getItemsWithWrongCollectionType() {
        return itemsWithWrongCollectionType;
    }

    public Map<Integer, Resource> getItemsWithWrongMapKey() {
        return itemsWithWrongMapKey;
    }

    public String getItemsWithWrongFieldType() {
        return itemsWithWrongFieldType;
    }

    public List<Resource> getItemsListWithKeyValue() {
        return itemsListWithKeyValue;
    }

    public List<SimpleListItem> getItemsWithoutAnnotationValue() {
        return itemsWithoutAnnotationValue;
    }

    public List<EToolboxListInjectorTest.NonModel> getItemsNonModel() {
        return itemsNonModel;
    }
}
