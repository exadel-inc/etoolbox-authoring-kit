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
package com.exadel.aem.toolkit.core.injectors;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.day.cq.commons.jcr.JcrConstants;

import com.exadel.aem.toolkit.core.injectors.models.ITestModelEToolboxList;
import com.exadel.aem.toolkit.core.injectors.models.TestModelEToolboxList;
import com.exadel.aem.toolkit.core.lists.models.SimpleListItem;
import com.exadel.aem.toolkit.core.lists.utils.ListHelper;

import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class EToolboxListInjectorTest {

    private static final String SIMPLE_LIST_PATH = "/content/etoolbox-lists/contentList";
    private static final String CUSTOM_LIST_PATH = "/content/etoolbox-lists/customContentList";

    @Rule
    public final AemContext context = new AemContext();

    private TestModelEToolboxList testModel;

    @Before
    public void beforeTest() {
        context.addModelsForClasses(TestModelEToolboxList.class);
        context.registerInjectActivateService(new EToolboxListInjector());
        context.load().json("/com/exadel/aem/toolkit/core/injectors/contentList.json", SIMPLE_LIST_PATH);
        context.load().json("/com/exadel/aem/toolkit/core/injectors/customContentList.json", CUSTOM_LIST_PATH);
    }

    @Test
    public void shouldInjectResourceIntoList() {
        testModel = context.request().adaptTo(TestModelEToolboxList.class);
        List<Resource> expected = ListHelper.getResourceList(context.resourceResolver(), SIMPLE_LIST_PATH);
        List<Resource> actual = testModel.getItemsListResource();

        assertNotNull(testModel);
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).getValueMap(), actual.get(i).getValueMap());
        }
    }

    @Test
    public void shouldInjectSimpleListItemIntoCollection() {
        testModel = context.request().adaptTo(TestModelEToolboxList.class);
        Collection<Resource> expected = ListHelper.getResourceList(context.resourceResolver(), SIMPLE_LIST_PATH);
        Collection<Resource> actual = testModel.getItemsCollectionResource();

        assertNotNull(testModel);
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(
                IteratorUtils.get(expected.iterator(), i).getValueMap(),
                IteratorUtils.get(actual.iterator(), i).getValueMap()
            );
        }
    }

    @Test
    public void shouldInjectSimpleListItemIntoList() {
        testModel = context.request().adaptTo(TestModelEToolboxList.class);
        List<SimpleListItem> expected = ListHelper.getList(context.resourceResolver(), SIMPLE_LIST_PATH);
        List<SimpleListItem> actual = testModel.getItemsListSimpleList();

        assertNotNull(testModel);
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).getTitle(), actual.get(i).getTitle());
            assertEquals(expected.get(i).getValue(), actual.get(i).getValue());
        }
    }

    @Test
    public void shouldInjectCustomModelIntoList() {
        testModel = context.request().adaptTo(TestModelEToolboxList.class);
        List<ListItemModel> expected = ListHelper.getList(
            context.resourceResolver(), CUSTOM_LIST_PATH, ListItemModel.class);
        List<ListItemModel> actual = testModel.getItemsListTestModel();

        assertNotNull(testModel);
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), actual.get(i));
        }
    }

    @Test
    public void shouldInjectResourceIntoMap() {
        testModel = context.request().adaptTo(TestModelEToolboxList.class);
        Map<String, Resource> expected = ListHelper.getMap(context.resourceResolver(),
            SIMPLE_LIST_PATH, JcrConstants.JCR_TITLE, Resource.class);
        Map<String, Resource> actual = testModel.getItemsMapResource();

        assertNotNull(testModel);
        assertEquals(expected.size(), actual.size());
        assertEquals(expected.keySet(), actual.keySet());
        assertTrue(expected.entrySet().stream()
            .allMatch(e -> e.getValue().getValueMap().equals(actual.get(e.getKey()).getValueMap())));
    }

    @Test
    public void shouldInjectStringTypeIntoMap() {
        testModel = context.request().adaptTo(TestModelEToolboxList.class);
        Map<String, String> expected = ListHelper.getMap(context.resourceResolver(), SIMPLE_LIST_PATH);
        Map<String, String> actual = testModel.getItemsMapString();

        assertNotNull(testModel);
        assertEquals(expected.size(), actual.size());
        assertEquals(expected, actual);
    }

    @Test
    public void shouldInjectCustomModelInMapWithKeyProperty() {
        testModel = context.request().adaptTo(TestModelEToolboxList.class);
        Map<String, ListItemModel> expected = ListHelper.getMap(context.resourceResolver(),
            CUSTOM_LIST_PATH, "textValue", ListItemModel.class);
        Map<String, ListItemModel> actual = testModel.getItemsMapTestModel();

        assertNotNull(testModel);
        assertEquals(expected.size(), actual.size());
        assertEquals(expected.keySet(), actual.keySet());
        assertTrue(expected.entrySet().stream().allMatch(e -> e.getValue().equals(actual.get(e.getKey()))));
    }

    @Test
    public void shouldInjectObjectTypeIntoMap() {
        testModel = context.request().adaptTo(TestModelEToolboxList.class);
        Map<String, String> expected = ListHelper.getMap(context.resourceResolver(), SIMPLE_LIST_PATH);
        Map<Object, Object> actual = testModel.getItemsMapObjects();

        assertNotNull(testModel);
        assertEquals(expected.size(), actual.size());
        assertEquals(expected, actual);
    }

    @Test
    public void shouldInjectIntoMethodParameter() {
        testModel = context.request().adaptTo(TestModelEToolboxList.class);
        List<Resource> expected = ListHelper.getResourceList(context.resourceResolver(), SIMPLE_LIST_PATH);
        List<Resource> actual = testModel.getItemsListResourceFromMethodParameter();

        assertNotNull(testModel);
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).getValueMap(), actual.get(i).getValueMap());
        }
    }

    @Test
    public void shouldInjectIntoMethod() {
        ITestModelEToolboxList testInterface = context.request().adaptTo(ITestModelEToolboxList.class);
        assertNotNull(testInterface);

        Map<String, String> expected = ListHelper.getMap(context.resourceResolver(), SIMPLE_LIST_PATH);
        Map<String, String> actual = testInterface.getItemsMapStringFromMethod();

        assertEquals(expected.size(), actual.size());
        assertEquals(expected, actual);
    }

    @Test
    public void shouldNotInjectIfAnnotationValueMissingOrWrongType() {
        testModel = context.request().adaptTo(TestModelEToolboxList.class);

        assertNotNull(testModel);
        assertNull(testModel.getItemsWithWrongCollectionType());
        assertNull(testModel.getItemsWithWrongFieldType());
    }

    @Test
    public void shouldInjectCustomModelItemsArray() {
        testModel = context.request().adaptTo(TestModelEToolboxList.class);
        ListItemModel[] expected = ListHelper.getList(
            context.resourceResolver(), CUSTOM_LIST_PATH, ListItemModel.class).toArray(new ListItemModel[0]);
        ListItemModel[] actual = testModel.getItemsArrayModel();

        assertNotNull(testModel);
        assertEquals(expected.length, testModel.getItemsArrayModel().length);
        assertArrayEquals(expected, actual);
    }

    @Model(adaptables = Resource.class,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
    )
    @SuppressWarnings("unused")
    public static class ListItemModel {

        @ValueMapValue
        private String textValue;

        @ValueMapValue
        private boolean booleanValue;

        @ValueMapValue
        private int intValue;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ListItemModel that = (ListItemModel) o;
            return booleanValue == that.booleanValue && intValue == that.intValue && Objects.equals(textValue, that.textValue);
        }

        @Override
        public int hashCode() {
            return Objects.hash(textValue, booleanValue, intValue);
        }
    }
}
