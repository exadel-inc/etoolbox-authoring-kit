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
package com.exadel.aem.toolkit.core.lists.utils;

import java.beans.Transient;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.WCMException;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.lists.models.SimpleListItem;

import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class ListHelperTest {

    private static final String SIMPLE_LIST_PATH = "/content/etoolbox-lists/simpleList";
    private static final String CUSTOM_LIST_PATH = "/content/etoolbox-lists/customList";

    @Rule
    public AemContext context = new AemContext(ResourceResolverType.RESOURCERESOLVER_MOCK);

    @Before
    public void setUp() {
        context.load().json("/com/exadel/aem/toolkit/core/lists/utils/simpleList.json", SIMPLE_LIST_PATH);
        context.load().json("/com/exadel/aem/toolkit/core/lists/utils/customList.json", CUSTOM_LIST_PATH);
        context.addModelsForClasses(SimpleListItem.class, ItemModel.class);
    }

    @Test
    public void shouldReturnEmptyListForInvalidInputs() {
        List<NonModel> actual = ListHelper.getList(context.resourceResolver(), CUSTOM_LIST_PATH, NonModel.class);
        assertEquals(0, actual.size());

        Map<String, NonModel> actual2 = ListHelper.getMap(context.resourceResolver(), CUSTOM_LIST_PATH, "title", NonModel.class);
        assertEquals(0, actual2.size());

        List<Resource> actual3 = ListHelper.getResourceList(context.resourceResolver(), "non-existing-path");
        assertEquals(0, actual3.size());

        List<Resource> actual4 = ListHelper.getResourceList(null, CUSTOM_LIST_PATH);
        assertEquals(0, actual4.size());
    }

    @Test
    public void shouldRetrieveBasicList() {
        List<SimpleListItem> actual = ListHelper.getList(context.resourceResolver(), SIMPLE_LIST_PATH);
        assertEquals(5, actual.size());
        assertEquals("key1", actual.get(0).getTitle());
        assertEquals("value1", actual.get(0).getValue());

        assertEquals("key2", actual.get(1).getTitle());
        assertEquals("value2", actual.get(1).getValue());

        assertEquals("key1", actual.get(2).getTitle());
        assertEquals("value3", actual.get(2).getValue());

        assertEquals("", actual.get(3).getTitle());
        assertEquals("", actual.get(3).getValue());

        assertEquals("", actual.get(4).getTitle());
        assertEquals("value4", actual.get(4).getValue());
    }

    @Test
    public void shouldRetrieveResourceList() {
        List<Resource> actual = ListHelper.getList(context.resourceResolver(), CUSTOM_LIST_PATH, Resource.class);
        assertEquals("Hello", actual.get(0).getValueMap().get("textValue"));
        assertEquals(true, actual.get(0).getValueMap().get("booleanValue"));

        assertEquals("World", actual.get(1).getValueMap().get("textValue"));
        assertEquals(false, actual.get(1).getValueMap().get("booleanValue"));
    }

    @Test
    public void shouldAdaptListItemsToModel() {
        List<ItemModel> actual = ListHelper.getList(context.resourceResolver(), CUSTOM_LIST_PATH, ItemModel.class);
        assertEquals(new ItemModel("Hello", true), actual.get(0));
        assertEquals(new ItemModel("World", false), actual.get(1));
    }

    @Test
    public void shouldRetrieveBasicMap() {
        Map<String, String> actual = ListHelper.getMap(context.resourceResolver(), SIMPLE_LIST_PATH);
        assertEquals(3, actual.size());
        assertEquals("value3", actual.get("key1"));
        assertEquals("value2", actual.get("key2"));
        assertEquals("value4", actual.get(""));
    }

    @Test
    public void shouldRetrieveResourceMap() {
        Map<String, Resource> actual = ListHelper.getResourceMap(context.resourceResolver(), SIMPLE_LIST_PATH);
        assertEquals("value3", actual.get("key1").getValueMap().get(CoreConstants.PN_VALUE));
    }

    @Test
    public void shouldRetrieveCustomResourceMap() {
        Map<String, Resource> actual = ListHelper.getResourceMap(
            context.resourceResolver(),
            CUSTOM_LIST_PATH,
            "textValue");
        assertEquals("Hello", actual.get("Hello").getValueMap().get("textValue"));
        assertEquals(true, actual.get("Hello").getValueMap().get("booleanValue"));

        assertEquals("World", actual.get("World").getValueMap().get("textValue"));
        assertEquals(false, actual.get("World").getValueMap().get("booleanValue"));
    }

    @Test
    public void shouldAdaptMapItemToModel() {
        Map<String, ItemModel> actual = ListHelper.getMap(
            context.resourceResolver(),
            CUSTOM_LIST_PATH,
            "textValue",
            ItemModel.class);
        assertEquals(new ItemModel("Hello", true), actual.get("Hello"));
        assertEquals(new ItemModel("World", false), actual.get("World"));
    }

    @Test
    public void shouldCreateListBasedOnMap() throws WCMException {
        Map<String, Object> listItems = new HashMap<>();
        listItems.put("first", "firstValue");
        listItems.put("second", "secondValue");

        Page listPage = ListHelper.createList(context.resourceResolver(), "/content/test", listItems);

        assertNotNull(listPage);
        Resource list = listPage.getContentResource("list");

        Resource firstListItem = IterableUtils.get(list.getChildren(), 0);
        assertEquals("first", firstListItem.getValueMap().get(JcrConstants.JCR_TITLE, StringUtils.EMPTY));
        assertEquals("firstValue", firstListItem.getValueMap().get(CoreConstants.PN_VALUE, StringUtils.EMPTY));

        Resource secondListItem = IterableUtils.get(list.getChildren(), 1);
        assertEquals("second", secondListItem.getValueMap().get(JcrConstants.JCR_TITLE, StringUtils.EMPTY));
        assertEquals("secondValue", secondListItem.getValueMap().get(CoreConstants.PN_VALUE, StringUtils.EMPTY));
    }

    @Test
    public void shouldCreateListWithoutItemsIfPassedResourceCollectionIsEmpty() throws WCMException {
        Page listPage = ListHelper.createList(context.resourceResolver(), "/content/test", Collections.emptyList());

        assertNotNull(listPage);
        Resource list = listPage.getContentResource().getChild(CoreConstants.NN_LIST);
        assertNotNull(list);
        assertFalse(list.hasChildren());
    }

    @Test
    public void shouldRecreateListIfAlreadyExists() throws WCMException {
        Map<String, Object> properties = new HashMap<>();
        properties.put(JcrConstants.JCR_TITLE, "first");
        properties.put(CoreConstants.PN_VALUE, "firstValue");
        Resource resource = context.create().resource("/testpath", properties);

        context.create().resource("/content/test");
        Page list = ListHelper.createResourceList(context.resourceResolver(), "/content/test", Collections.singleton(resource));

        assertNotNull(list);
        Resource listItem = list.getContentResource("list/listItem");
        assertEquals("first", listItem.getValueMap().get(JcrConstants.JCR_TITLE, StringUtils.EMPTY));
        assertEquals("firstValue", listItem.getValueMap().get(CoreConstants.PN_VALUE, StringUtils.EMPTY));
    }

    @Test
    public void shouldCreateListBasedOnSimpleListItem() throws WCMException {
        SimpleListItem simpleListItem = new SimpleListItemImpl("first", "firstValue");

        Page listPage = ListHelper.createList(context.resourceResolver(), "/content/test", Collections.singletonList(simpleListItem));

        assertNotNull(listPage);
        Resource listItem = listPage.getContentResource("list/listItem");
        assertEquals(simpleListItem.getTitle(), listItem.getValueMap().get(JcrConstants.JCR_TITLE, StringUtils.EMPTY));
        assertEquals(simpleListItem.getValue(), listItem.getValueMap().get(CoreConstants.PN_VALUE, StringUtils.EMPTY));
    }

    @Test
    public void shouldCreateListBasedOnArbitraryModel() throws WCMException {
        ItemModel itemModel = new ItemModel("someValue", false);

        Page listPage = ListHelper.createList(context.resourceResolver(), "/content/test", Collections.singletonList(itemModel));

        assertNotNull(listPage);
        Resource listItem = listPage.getContentResource("list/listItem");
        assertEquals(3, listItem.getValueMap().size());
        assertEquals("etoolbox-authoring-kit/lists/components/content/listItem", listItem.getResourceType());
        assertEquals(itemModel.textValue, listItem.getValueMap().get("textValue", StringUtils.EMPTY));
        assertEquals(itemModel.booleanValue, listItem.getValueMap().get("booleanValue", false));
    }

    @Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
    @SuppressWarnings("unused")
    public static class ItemModel {

        @ValueMapValue
        private String textValue;

        @ValueMapValue
        private boolean booleanValue;

        public final transient String ignoredStringValue = "ignored";

        public ItemModel() {
        }

        public ItemModel(String textValue, boolean booleanValue) {
            this.textValue = textValue;
            this.booleanValue = booleanValue;
        }

        public String getTextValue() {
            return textValue;
        }

        public boolean isBooleanValue() {
            return booleanValue;
        }

        @Transient
        public long getIgnoredLongValue() {
            return 42L;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ItemModel itemModel = (ItemModel) o;
            return booleanValue == itemModel.booleanValue && Objects.equals(textValue, itemModel.textValue);
        }

        @Override
        public int hashCode() {
            return Objects.hash(textValue, booleanValue);
        }
    }

    @Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
    private static class SimpleListItemImpl implements SimpleListItem {

        private final String title;
        private final String value;

        public SimpleListItemImpl(String title, String value) {
            this.title = title;
            this.value = value;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    public static class NonModel {
    }
}
