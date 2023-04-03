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
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
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

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.injectors.models.child.ExtendedListItem;
import com.exadel.aem.toolkit.core.injectors.models.children.InjectedByLoopbackPath;
import com.exadel.aem.toolkit.core.injectors.models.children.InjectedByMemberName;
import com.exadel.aem.toolkit.core.injectors.models.children.InjectedByPath;
import com.exadel.aem.toolkit.core.injectors.models.children.InjectedWithFilters;
import com.exadel.aem.toolkit.core.injectors.models.children.InjectedWithPostfix;
import com.exadel.aem.toolkit.core.injectors.models.children.InjectedWithPrefix;
import com.exadel.aem.toolkit.core.injectors.models.children.InjectedWithPrefixPostfix;
import com.exadel.aem.toolkit.core.lists.models.SimpleListItem;

public class ChildrenInjectorTest {

    private static final String MODELS_PACKAGE_NAME = "com.exadel.aem.toolkit.core.injectors.models.children";

    private static final List<String> EXPECTED_VALUE_SEQUENCE = Arrays.asList("value1", "value2", "value3", "value4");
    public static final String EXPECTED_TITLE = "key1";

    @Rule
    public final AemContext context = new AemContext();

    /* -----------
       Preparation
       ----------- */

    @Before
    public void beforeTest() {
        context.addModelsForPackage(MODELS_PACKAGE_NAME);
        context.registerInjectActivateService(new ChildrenInjector());
        context.registerInjectActivateService(new ChildInjector());
        context.load().json(ChildInjectorTest.MODELS_RESOURCES_FOLDER, ChildInjectorTest.ROOT_JCR_PATH);
        context.request().setResource(context.resourceResolver().getResource(ChildInjectorTest.ROOT_PAGE_CONTENT_PATH));
    }

    /* -----
       Tests
       ----- */

    @Test
    public void shouldInjectByFieldName() {
        shouldInjectByFieldName(context.request());
        shouldInjectByFieldName(context.request().getResource());
    }

    private void shouldInjectByFieldName(Adaptable adaptable) {
        InjectedByMemberName model = adaptable.adaptTo(InjectedByMemberName.class);
        assertNotNull(model);

        assertNotNull(model.getList());
        assertTrue(CollectionUtils.isEqualCollection(
            EXPECTED_VALUE_SEQUENCE,
            model.getList().stream().map(SimpleListItem::getValue).collect(Collectors.toList())));

        assertNotNull(model.getListFromConstructor());
        ComparisonHelper.assertCollectionsEqual(
            model.getList(),
            model.getListFromConstructor(),
            ChildrenInjectorTest::assertTitleAndValueEqual);

        assertNotNull(model.getSupplier());
        assertNotNull(model.getSupplier().getList());
        ComparisonHelper.assertCollectionsEqual(
            model.getList(),
            model.getSupplier().getList(),
            (first, second) -> {
                assertEquals(first.getTitle(), second.getTitle());
                assertEquals(first.getValue(), second.getValue());
            });
    }

    @Test
    public void shouldInjectByPath() {
        shouldInjectByPath(context.request());
        shouldInjectByPath(context.request().getResource());
    }

    private void shouldInjectByPath(Adaptable adaptable) {
        InjectedByPath model = adaptable.adaptTo(InjectedByPath.class);
        assertNotNull(model);

        assertNotNull(model.getValue());
        assertTrue(CollectionUtils.isEqualCollection(
            EXPECTED_VALUE_SEQUENCE,
            model.getValue().stream().map(SimpleListItem::getValue).collect(Collectors.toList())));

        assertNotNull(model.getValueFromConstructor());
        ComparisonHelper.assertCollectionsEqual(
            model.getValue(),
            model.getValueFromConstructor(),
            ChildrenInjectorTest::assertTitleAndValueEqual);

        assertNotNull(model.getSupplier());
        assertNotNull(model.getSupplier().getValue());
        ComparisonHelper.assertCollectionsEqual(
            model.getValue(),
            model.getSupplier().getValue(),
            ChildrenInjectorTest::assertTitleAndValueEqual);

        assertNotNull(model.getArrayValue());
        ComparisonHelper.assertCollectionsEqual(
            model.getValue(),
            model.getArrayValue(),
            ChildrenInjectorTest::assertTitleAndValueEqual);
    }

    @Test
    public void shouldInjectByLoopbackPath() {
        context.currentResource(ChildInjectorTest.ROOT_PAGE_CONTENT_PATH + "/list");
        shouldInjectByLoopbackPath(context.request());
        shouldInjectByLoopbackPath(context.request().getResource());
    }

    private void shouldInjectByLoopbackPath(Adaptable adaptable) {
        InjectedByLoopbackPath model = adaptable.adaptTo(InjectedByLoopbackPath.class);
        assertNotNull(model);

        assertNotNull(model.getValue());
        assertTrue(CollectionUtils.isEqualCollection(
            EXPECTED_VALUE_SEQUENCE,
            model.getValue().stream().map(SimpleListItem::getValue).collect(Collectors.toList())));

        assertNotNull(model.getValueFromConstructor());
        ComparisonHelper.assertCollectionsEqual(
            model.getValue(),
            model.getValueFromConstructor(),
            ChildrenInjectorTest::assertTitleAndValueEqual);


    }

    @Test
    public void shouldProcessNestedChildren() {
        context.currentResource(ChildInjectorTest.ROOT_PAGE_CONTENT_PATH + "/list");
        shouldProcessNestedChildren(context.request());
        shouldProcessNestedChildren(context.request().getResource());
    }

    private void shouldProcessNestedChildren(Adaptable adaptable) {
        InjectedByLoopbackPath model = adaptable.adaptTo(InjectedByLoopbackPath.class);
        assertNotNull(model);
        assertNotNull(model.getValue());
        assertEquals(3, model.getValue().stream().filter(item -> item.getNestedViaChild() != null).count());
    }

    @Test
    public void shouldInjectPrefixFiltered() {
        shouldInjectPrefixFiltered(context.request());
        shouldInjectPrefixFiltered(context.request().getResource());
    }

    private void shouldInjectPrefixFiltered(Adaptable adaptable) {
        InjectedWithPrefix model = adaptable.adaptTo(InjectedWithPrefix.class);
        assertNotNull(model);

        assertNotNull(model.getValue());
        assertTrue(CollectionUtils.isEqualCollection(
            Arrays.asList("alt_value2", "alt_value4"),
            model.getValue().stream().map(ExtendedListItem::getValue).collect(Collectors.toList())));

        assertEquals(
            ChildInjectorTest.NESTED_RESOURCE_TITLE,
            Objects.requireNonNull(model.getValue().get(0).getNestedViaChildResource()).getNestedTitle());
        assertEquals(
            ChildInjectorTest.NESTED_RESOURCE_VALUE,
            Objects.requireNonNull(model.getValue().get(0).getNestedViaChildResource()).getNestedValue());

        assertNotNull(model.getValueFromConstructor());
        ComparisonHelper.assertCollectionsEqual(
            model.getValue(),
            model.getValueFromConstructor(),
            ChildrenInjectorTest::assertTitleAndValueEqual);
    }

    @Test
    public void shouldInjectPostfixFiltered() {
        shouldInjectPostfixFiltered(context.request());
        shouldInjectPostfixFiltered(context.request().getResource());
    }

    private void shouldInjectPostfixFiltered(Adaptable adaptable) {
        InjectedWithPostfix model = adaptable.adaptTo(InjectedWithPostfix.class);
        assertNotNull(model);

        assertNotNull(model.getValue());
        assertTrue(CollectionUtils.isEqualCollection(
            Arrays.asList("value3_alt", "value4_alt"),
            model.getValue().stream().map(SimpleListItem::getValue).collect(Collectors.toList())));

        assertNotNull(model.getValueFromConstructor());
        ComparisonHelper.assertCollectionsEqual(
            model.getValue(),
            model.getValueFromConstructor(),
            ChildrenInjectorTest::assertTitleAndValueEqual);
    }

    @Test
    public void shouldInjectPrefixPostfixFiltered() {
        shouldInjectPrefixPostfixFiltered(context.request());
        shouldInjectPrefixPostfixFiltered(context.request().getResource());
    }

    private void shouldInjectPrefixPostfixFiltered(Adaptable adaptable) {
        InjectedWithPrefixPostfix model = adaptable.adaptTo(InjectedWithPrefixPostfix.class);
        assertNotNull(model);

        assertNotNull(model.getValue());
        assertEquals(1, model.getValue().size());
        assertEquals("alt_key4_alt", model.getValue().get(0).getTitle());
        assertEquals("alt_value4_alt", model.getValue().get(0).getValue());

        assertNotNull(model.getValueFromConstructor());
        ComparisonHelper.assertCollectionsEqual(
            model.getValue(),
            model.getValueFromConstructor(),
            ChildrenInjectorTest::assertTitleAndValueEqual);
    }

    @Test
    public void shouldInjectUsingFilters() {
        shouldInjectUsingFilters(context.request());
        shouldInjectUsingFilters(context.request().getResource());
    }

    private void shouldInjectUsingFilters(Adaptable adaptable) {
        InjectedWithFilters model = adaptable.adaptTo(InjectedWithFilters.class);
        assertNotNull(model);

        assertNotNull(model.getNonGhosts());
        assertEquals(2, model.getNonGhosts().size());
        assertEquals("Only used with ChildrenInjector + NonGhostFilter", model.getNonGhosts().get(0).getNestedTitle());
        assertEquals(42, model.getNonGhosts().get(0).getNestedValue());

        assertNotNull(model.getValueFromConstructor());
        ComparisonHelper.assertCollectionsEqual(
            model.getNonGhosts(),
            model.getValueFromConstructor(),
            ChildrenInjectorTest::assertTitleAndValueEqual);

        assertNotNull(model.getByNameFilter());
        assertEquals(1, model.getByNameFilter().length);
        assertEquals(EXPECTED_TITLE, model.getByNameFilter()[0].getTitle());
        assertEquals(EXPECTED_VALUE_SEQUENCE.get(0), model.getByNameFilter()[0].getValue());

        assertNotNull(model.getSupplier());
        assertNotNull(model.getSupplier().getByRestypeFilter());
        assertEquals(3, model.getSupplier().getByRestypeFilter().size());
        assertEquals(EXPECTED_TITLE, model.getSupplier().getByRestypeFilter().get(0).getTitle());
        assertEquals(EXPECTED_VALUE_SEQUENCE.get(0), model.getSupplier().getByRestypeFilter().get(0).getValue());
    }

    /* ---------------
       Service methods
       --------------- */

    private static void assertTitleAndValueEqual(SimpleListItem item, Resource resource) {
        assertEquals(item.getTitle(), resource.getValueMap().get(JcrConstants.JCR_TITLE, String.class));
        assertEquals(item.getValue(), resource.getValueMap().get(CoreConstants.PN_VALUE, String.class));
    }

    private static void assertTitleAndValueEqual(ExtendedListItem.Nested item, Resource resource) {
        assertEquals(item.getNestedTitle(), resource.getValueMap().get(CoreConstants.PN_TITLE, String.class));
        assertEquals(item.getNestedValue(), (long) resource.getValueMap().get(CoreConstants.PN_VALUE, 0));
    }

    private static void assertTitleAndValueEqual(SimpleListItem first, SimpleListItem second) {
        assertEquals(first.getTitle(), second.getTitle());
        assertEquals(first.getValue(), second.getValue());
    }
}
