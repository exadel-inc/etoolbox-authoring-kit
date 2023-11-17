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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.day.cq.commons.jcr.JcrConstants;
import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.exadel.aem.toolkit.core.AemContextFactory;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.injectors.models.lists.CustomListItem;
import com.exadel.aem.toolkit.core.injectors.models.lists.CustomListItems;
import com.exadel.aem.toolkit.core.injectors.models.lists.CustomListItemsMap;
import com.exadel.aem.toolkit.core.injectors.models.lists.Resources;
import com.exadel.aem.toolkit.core.injectors.models.lists.ResourcesMap;
import com.exadel.aem.toolkit.core.injectors.models.lists.SimpleListItems;
import com.exadel.aem.toolkit.core.injectors.models.lists.StringsMap;
import com.exadel.aem.toolkit.core.lists.models.SimpleListItem;
import com.exadel.aem.toolkit.core.lists.utils.ListHelper;

public class EToolboxListInjectorTest {

    private static final String MODELS_PACKAGE_NAME = CoreConstants.ROOT_PACKAGE + ".core.injectors.models.lists";

    public static final String SIMPLE_LIST_PATH = "/content/etoolbox-lists/contentList";
    public static final String CUSTOM_LIST_PATH = "/content/etoolbox-lists/customContentList";
    public static final String PN_TEXT_VALUE = "textValue";

    @Rule
    public final AemContext context = AemContextFactory.newInstance();

    /* -----------
       Preparation
       ----------- */

    @Before
    public void beforeTest() {
        context.addModelsForPackage(MODELS_PACKAGE_NAME);

        context.registerInjectActivateService(new EToolboxListInjector());
        context.load().json("/com/exadel/aem/toolkit/core/injectors/listInjector.json", SIMPLE_LIST_PATH);
        context.load().json("/com/exadel/aem/toolkit/core/injectors/customListInjector.json", CUSTOM_LIST_PATH);
    }

    /* -----
       Tests
       ----- */

    @Test
    public void shouldInjectIntoCollectionOfResources() {
        context.currentResource(SIMPLE_LIST_PATH);
        shouldInjectIntoCollectionOfResources(context.request());
        shouldInjectIntoCollectionOfResources(context.request().getResource());
    }

    private void shouldInjectIntoCollectionOfResources(Adaptable adaptable) {
        Resources model = adaptable.adaptTo(Resources.class);
        assertNotNull(model);

        List<Resource> expectedList = ListHelper.getResourceList(context.resourceResolver(), SIMPLE_LIST_PATH);

        ComparisonHelper.assertCollectionsEqual(
            expectedList,
            model.getValue(),
            EToolboxListInjectorTest::assertResourcesEqual);
        ComparisonHelper.assertCollectionsEqual(
            expectedList,
            model.getArrayValue(),
            EToolboxListInjectorTest::assertResourcesEqual);
        ComparisonHelper.assertCollectionsEqual(
            expectedList,
            model.getConstructorValue(),
            EToolboxListInjectorTest::assertResourcesEqual);
        ComparisonHelper.assertCollectionsEqual(
            expectedList,
            model.getValueSupplier().getValue(),
            EToolboxListInjectorTest::assertResourcesEqual);
        ComparisonHelper.assertCollectionsEqual(
            expectedList,
            (List<?>) model.getObjectValue(),
            EToolboxListInjectorTest::assertResourcesEqual);
    }

    @Test
    public void shouldInjectIntoCollectionOfSimpleItems() {
        context.currentResource(SIMPLE_LIST_PATH);
        shouldInjectIntoCollectionOfSimpleItems(context.request());
        shouldInjectIntoCollectionOfSimpleItems(context.request().getResource());
    }

    private void shouldInjectIntoCollectionOfSimpleItems(Adaptable adaptable) {
        SimpleListItems model = adaptable.adaptTo(SimpleListItems.class);
        assertNotNull(model);

        List<SimpleListItem> expectedList = ListHelper.getList(context.resourceResolver(), SIMPLE_LIST_PATH);

        ComparisonHelper.assertCollectionsEqual(
            expectedList,
            model.getValue(),
            EToolboxListInjectorTest::assertSimpleListItemsEqual);
        ComparisonHelper.assertCollectionsEqual(
            expectedList,
            model.getArrayValue(),
            EToolboxListInjectorTest::assertSimpleListItemsEqual);
        ComparisonHelper.assertCollectionsEqual(
            expectedList,
            model.getSetValue(),
            EToolboxListInjectorTest::assertSimpleListItemsEqual);
        ComparisonHelper.assertCollectionsEqual(
            expectedList,
            model.getConstructorValue(),
            EToolboxListInjectorTest::assertSimpleListItemsEqual);
        ComparisonHelper.assertCollectionsEqual(
            expectedList,
            model.getValueSupplier().getValue(),
            EToolboxListInjectorTest::assertSimpleListItemsEqual);
    }

    @Test
    public void shouldInjectIntoCollectionOfCustomModels() {
        context.currentResource(CUSTOM_LIST_PATH);
        shouldInjectIntoCollectionOfCustomModels(context.request());
        shouldInjectIntoCollectionOfCustomModels(context.request().getResource());
    }

    private void shouldInjectIntoCollectionOfCustomModels(Adaptable adaptable) {
        CustomListItems model = adaptable.adaptTo(CustomListItems.class);
        assertNotNull(model);

        List<CustomListItem> expectedList = ListHelper.getList(context.resourceResolver(), CUSTOM_LIST_PATH, CustomListItem.class);

        ComparisonHelper.assertCollectionsEqual(
            expectedList,
            model.getValue(),
            EToolboxListInjectorTest::assertEntitiesEqual);
        ComparisonHelper.assertCollectionsEqual(
            expectedList,
            model.getArrayValue(),
            EToolboxListInjectorTest::assertEntitiesEqual);
        ComparisonHelper.assertCollectionsEqual(
            expectedList,
            model.getConstructorValue(),
            EToolboxListInjectorTest::assertEntitiesEqual);
        ComparisonHelper.assertCollectionsEqual(
            expectedList,
            model.getValueSupplier().getValue(),
            EToolboxListInjectorTest::assertEntitiesEqual);
    }

    @Test
    public void shouldInjectResourcesIntoMap() {
        context.currentResource(SIMPLE_LIST_PATH);
        shouldInjectResourcesIntoMap(context.request());
        shouldInjectResourcesIntoMap(context.request().getResource());
    }

    private void shouldInjectResourcesIntoMap(Adaptable adaptable) {
        ResourcesMap model = adaptable.adaptTo(ResourcesMap.class);
        assertNotNull(model);

        Map<String, Resource> expected = ListHelper.getMap(
            context.resourceResolver(),
            SIMPLE_LIST_PATH,
            JcrConstants.JCR_TITLE,
            Resource.class);

        for (Map<String, Resource> actual :
            Arrays.asList(model.getValue(), model.getConstructorValue(), model.getValueSupplier().getValue())) {

            assertNotNull(actual);
            assertEquals(expected.size(), actual.size());
            ComparisonHelper.assertCollectionsEqual(
                expected.values(),
                actual.values(),
                EToolboxListInjectorTest::assertResourcesEqual);
        }

        Map<Object, Object> genericActual = model.getGenericValue();
        assertNotNull(genericActual);
        assertEquals(expected.size(), genericActual.size());
        ComparisonHelper.assertCollectionsEqual(
            expected.values(),
            genericActual.values().stream().map(Resource.class::cast).collect(Collectors.toList()),
            EToolboxListInjectorTest::assertResourcesEqual);
    }

    @Test
    public void shouldInjectItemMembersIntoMap() {
        context.currentResource(SIMPLE_LIST_PATH);
        shouldInjectItemMembersIntoMap(context.request());
        shouldInjectItemMembersIntoMap(context.request().getResource());
    }

    private void shouldInjectItemMembersIntoMap(Adaptable adaptable) {
        StringsMap model = adaptable.adaptTo(StringsMap.class);
        assertNotNull(model);

        Map<String, String> expected = ListHelper.getMap(context.resourceResolver(), SIMPLE_LIST_PATH);

        for (Map<String, String> actual :
            Arrays.asList(model.getValue(), model.getConstructorValue(), model.getValueSupplier().getValue())) {

            assertNotNull(actual);
            assertEquals(expected.size(), actual.size());
            ComparisonHelper.assertCollectionsEqual(
                expected.values(),
                actual.values(),
                EToolboxListInjectorTest::assertEntitiesEqual);
        }
    }

    @Test
    public void shouldInjectCustomItemsIntoMap() {
        context.currentResource(CUSTOM_LIST_PATH);
        shouldInjectCustomItemsIntoMap(context.request());
        shouldInjectCustomItemsIntoMap(context.request().getResource());
    }

    private void shouldInjectCustomItemsIntoMap(Adaptable adaptable) {
        CustomListItemsMap model = adaptable.adaptTo(CustomListItemsMap.class);
        assertNotNull(model);

        Map<String, CustomListItem> expected = ListHelper.getMap(
            context.resourceResolver(),
            CUSTOM_LIST_PATH,
            PN_TEXT_VALUE,
            CustomListItem.class);

        for (Map<String, CustomListItem> actual :
            Arrays.asList(model.getValue(), model.getConstructorValue(), model.getValueSupplier().getValue())) {

            assertNotNull(actual);
            assertEquals(expected.size(), actual.size());
            ComparisonHelper.assertCollectionsEqual(
                expected.values(),
                actual.values(),
                EToolboxListInjectorTest::assertEntitiesEqual);
        }
    }

    /* ---------------
       Service methods
       --------------- */

    private static void assertResourcesEqual(Resource expected, Object actual) {
        assertTrue(actual instanceof Resource);
        assertEquals(expected.getValueMap(), ((Resource) actual).getValueMap());
    }

    private static void assertSimpleListItemsEqual(SimpleListItem expected, SimpleListItem actual) {
        assertEquals(expected.getTitle(), actual.getTitle());
        assertEquals(expected.getValue(), actual.getValue());
    }

    private static <T> void assertEntitiesEqual(T expected, T actual) {
        assertEquals(expected, actual);
    }
}
