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

package com.exadel.aem.toolkit.bundle.lists.util;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.exadel.aem.toolkit.bundle.lists.models.GenericItem;

import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertEquals;

public class ListsHelperTest {

    private static final String SIMPLE_LIST_PATH = "/content/aat-lists/simpleList";
    private static final String CUSTOM_LIST_PATH = "/content/aat-lists/customList";
    
    @Rule
    public AemContext context = new AemContext(ResourceResolverType.RESOURCERESOLVER_MOCK);
    private ResourceResolver resolver;

    @Before
    public void setUp() {
        resolver = context.resourceResolver();
        context.load().json("/com/exadel/aem/toolkit/bundle/lists/util/simpleList.json", SIMPLE_LIST_PATH);
        context.load().json("/com/exadel/aem/toolkit/bundle/lists/util/customList.json", CUSTOM_LIST_PATH);
        context.addModelsForClasses(ItemModel.class);
    }

    @Test
    public void shouldReturnEmptyListForInvalidInputs() {
        List<Resource> actual = ListsHelper.getResourceList(null, CUSTOM_LIST_PATH);
        assertEquals(0, actual.size());

        List<Resource> actual2 = ListsHelper.getResourceList(resolver, "non-existing-path");
        assertEquals(0, actual2.size());
    }

    @Test
    public void getList() {
        List<GenericItem> actual = ListsHelper.getList(resolver, SIMPLE_LIST_PATH);
        assertEquals("key1", actual.get(0).getTitle());
        assertEquals("value1", actual.get(0).getValue());

        assertEquals("key2", actual.get(1).getTitle());
        assertEquals("value2", actual.get(1).getValue());

        assertEquals("key1", actual.get(2).getTitle());
        assertEquals("value3", actual.get(2).getValue());
    }

    @Test
    public void getResourcesList() {
        List<Resource> actual = ListsHelper.getResourceList(resolver, CUSTOM_LIST_PATH);
        assertEquals("Hello", actual.get(0).getValueMap().get("textValue"));
        assertEquals(true, actual.get(0).getValueMap().get("booleanValue"));

        assertEquals("World", actual.get(1).getValueMap().get("textValue"));
        assertEquals(false, actual.get(1).getValueMap().get("booleanValue"));
    }

    @Test
    public void getListModels() {
        List<ItemModel> actual = ListsHelper.getList(resolver, CUSTOM_LIST_PATH, ItemModel.class);
        assertEquals(new ItemModel("Hello", true), actual.get(0));
        assertEquals(new ItemModel("World", false), actual.get(1));
    }

    @Test
    public void getMap() {
        Map<String, String> actual = ListsHelper.getMap(resolver, SIMPLE_LIST_PATH);
        assertEquals("value3", actual.get("key1"));
        assertEquals("value2", actual.get("key2"));
    }

    @Test
    public void getResourceMap() {
        Map<String, Resource> actual = ListsHelper.getResourceMap(resolver, CUSTOM_LIST_PATH, "textValue");
        assertEquals("Hello", actual.get("Hello").getValueMap().get("textValue"));
        assertEquals(true, actual.get("Hello").getValueMap().get("booleanValue"));

        assertEquals("World", actual.get("World").getValueMap().get("textValue"));
        assertEquals(false, actual.get("World").getValueMap().get("booleanValue"));
    }

    @Test
    public void getMapModels() {
        Map<String, ItemModel> actual = ListsHelper.getMap(resolver, CUSTOM_LIST_PATH, "textValue", ItemModel.class);
        assertEquals(new ItemModel("Hello", true), actual.get("Hello"));
        assertEquals(new ItemModel("World", false), actual.get("World"));
    }

    @Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
    public static class ItemModel {

        @ValueMapValue
        private String textValue;

        @ValueMapValue
        private boolean booleanValue;

        @SuppressWarnings("unused") //used by Sling injectors
        public ItemModel() {
        }

        public ItemModel(String textValue, boolean booleanValue) {
            this.textValue = textValue;
            this.booleanValue = booleanValue;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ItemModel itemModel = (ItemModel) o;
            return booleanValue == itemModel.booleanValue && Objects.equals(textValue, itemModel.textValue);
        }

        @Override
        public int hashCode() {
            return Objects.hash(textValue, booleanValue);
        }
    }
}