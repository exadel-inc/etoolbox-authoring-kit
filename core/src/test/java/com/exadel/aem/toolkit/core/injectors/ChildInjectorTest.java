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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.day.cq.commons.jcr.JcrConstants;
import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.injectors.models.child.ExtendedListItem;
import com.exadel.aem.toolkit.core.injectors.models.child.InjectedByMemberName;
import com.exadel.aem.toolkit.core.injectors.models.child.InjectedByPath;
import com.exadel.aem.toolkit.core.injectors.models.child.InjectedWithPostfix;
import com.exadel.aem.toolkit.core.injectors.models.child.InjectedWithPrefix;
import com.exadel.aem.toolkit.core.injectors.models.child.InjectedWithPrefixPostfix;
import com.exadel.aem.toolkit.core.injectors.models.child.InjectedWithSelection;

public class ChildInjectorTest {

    private static final String MODELS_PACKAGE_NAME = "com.exadel.aem.toolkit.core.injectors.models.child";
    public static final String MODELS_RESOURCES_FOLDER = "/com/exadel/aem/toolkit/core/injectors/childInjector.json";
    public static final String ROOT_JCR_PATH = "/content";
    public static final String ROOT_PAGE_CONTENT_PATH = ROOT_JCR_PATH + "/jcr:content";

    private static final String EXPECTED_RESOURCE_TYPE = "etoolbox-authoring-kit/lists/components/content/listItem";

    private static final String PREFIXED_TITLE = "alt_key2";
    private static final String PREFIXED_VALUE = "alt_value2";
    private static final String POSTFIXED_TITLE = "key3_alt";
    private static final String POSTFIXED_VALUE = "value3_alt";
    private static final String PREFIXED_POSTFIXED_TITLE = "alt_key4_alt";
    private static final String PREFIXED_POSTFIXED_VALUE = "alt_value4_alt";

    private static final String STANDALONE_ITEM_TITLE = "Hello";
    private static final String STANDALONE_ITEM_VALUE = "World";
    public static final String NESTED_RESOURCE_TITLE = STANDALONE_ITEM_TITLE;
    static final long NESTED_RESOURCE_VALUE = 43L;

    @Rule
    public final AemContext context = new AemContext();

    /* -----------
       Preparation
       ----------- */

    @Before
    public void beforeTest() {
        context.addModelsForPackage(MODELS_PACKAGE_NAME);
        context.registerInjectActivateService(new ChildInjector());
        context.load().json(MODELS_RESOURCES_FOLDER, ROOT_JCR_PATH);
        context.request().setResource(context.resourceResolver().getResource(ROOT_PAGE_CONTENT_PATH));
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

        assertNotNull(model.getDefaultChild());
        assertEquals(STANDALONE_ITEM_TITLE, model.getDefaultChild().getTitle());
        assertEquals(STANDALONE_ITEM_VALUE, model.getDefaultChild().getValue());

        assertNotNull(model.getDefaultChildFromConstructor());
        assertEquals(
            model.getDefaultChild().getTitle(),
            model.getDefaultChildFromConstructor().getValueMap().get(JcrConstants.JCR_TITLE));
        assertEquals(
            model.getDefaultChild().getValue(),
            model.getDefaultChildFromConstructor().getValueMap().get(CoreConstants.PN_VALUE));
        assertEquals(
            EXPECTED_RESOURCE_TYPE,
            model.getDefaultChildFromConstructor().getValueMap().get(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY));

        assertNotNull(model.getSupplier());
        assertEquals(
            model.getDefaultChild().getTitle(),
            model.getSupplier().getDefaultChild().getTitle());
        assertEquals(
            model.getDefaultChild().getValue(),
            model.getSupplier().getDefaultChild().getValue());
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
        assertEquals("key1", model.getValue().getTitle());
        assertEquals("value1", model.getValue().getValue());

        assertNotNull(model.getSingularCollectionValue());
        assertEquals(STANDALONE_ITEM_TITLE, model.getSingularCollectionValue().get(0).getTitle());
        assertEquals(STANDALONE_ITEM_VALUE, model.getSingularCollectionValue().get(0).getValue());

        assertNotNull(model.getSingularArrayValue());
        assertEquals(STANDALONE_ITEM_TITLE, model.getSingularArrayValue()[0].getTitle());
        assertEquals(STANDALONE_ITEM_VALUE, model.getSingularArrayValue()[0].getValue());

        assertNotNull(model.getValueFromConstructor());
        assertEquals(
            model.getValue().getTitle(),
            model.getValueFromConstructor().getValueMap().get(JcrConstants.JCR_TITLE));
        assertEquals(
            model.getValue().getValue(),
            model.getValueFromConstructor().getValueMap().get(CoreConstants.PN_VALUE));
        assertEquals(
            EXPECTED_RESOURCE_TYPE,
            model.getValueFromConstructor().getValueMap().get(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY));

        assertNotNull(model.getSelfResource());
        assertEquals("Sample Page", model.getSelfResource().getValueMap().get(JcrConstants.JCR_TITLE));

        assertNotNull(model.getParentResource());
        assertEquals(
            model.getSelfResource().getValueMap().get(JcrConstants.JCR_TITLE),
            model.getParentResource().getValueMap().get(JcrConstants.JCR_TITLE));

        assertNotNull(model.getSupplier());
        assertEquals(
            model.getValue().getTitle(),
            model.getSupplier().getValue().getTitle());
        assertEquals(
            model.getValue().getValue(),
            model.getSupplier().getValue().getValue());
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
        assertEquals(PREFIXED_TITLE, model.getValue().getTitle());
        assertEquals(PREFIXED_VALUE, model.getValue().getValue());

        assertNotNull(model.getValueFromConstructor());
        assertEquals(
            model.getValue().getTitle(),
            model.getValueFromConstructor().getValueMap().get(JcrConstants.JCR_TITLE));
        assertEquals(
            model.getValue().getValue(),
            model.getValueFromConstructor().getValueMap().get(CoreConstants.PN_VALUE));

        assertNotNull(model.getSupplier());
        assertEquals(
            model.getValue().getTitle(),
            model.getSupplier().getValue().getTitle());
        assertEquals(
            model.getValue().getValue(),
            model.getSupplier().getValue().getValue());
    }

    @Test
    public void shouldInjectPrefixFilteredWithChildren() {
        String expectedPath = "/content/jcr:content/list/list_item_2/prefix_nested";
        shouldInjectFilteredWithChildren(
            context.request().adaptTo(InjectedWithPrefix.class),
            PREFIXED_TITLE,
            expectedPath);
        shouldInjectFilteredWithChildren(
            context.request().getResource().adaptTo(InjectedWithPrefix.class),
            PREFIXED_TITLE,
            expectedPath);
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
        assertEquals(POSTFIXED_TITLE, model.getValue().getTitle());
        assertEquals(POSTFIXED_VALUE, model.getValue().getValue());

        assertNotNull(model.getValueFromConstructor());
        assertEquals(
            model.getValue().getTitle(),
            model.getValueFromConstructor().getValueMap().get(JcrConstants.JCR_TITLE));
        assertEquals(
            model.getValue().getValue(),
            model.getValueFromConstructor().getValueMap().get(CoreConstants.PN_VALUE));

        assertNotNull(model.getSupplier());
        assertEquals(
            model.getValue().getTitle(),
            model.getSupplier().getValue().getTitle());
        assertEquals(
            model.getValue().getValue(),
            model.getSupplier().getValue().getValue());
    }

    @Test
    public void shouldInjectPostfixFilteredWithChildren() {
        String expectedPath = "/content/jcr:content/list/list_item_3/nested_postfix";
        shouldInjectFilteredWithChildren(
            context.request().adaptTo(InjectedWithPostfix.class),
            POSTFIXED_TITLE,
            expectedPath);
        shouldInjectFilteredWithChildren(
            context.request().getResource().adaptTo(InjectedWithPostfix.class),
            POSTFIXED_TITLE,
            expectedPath);
    }

    @Test
    public void shouldInjectPrefixAndPostfixFiltered() {
        shouldInjectPrefixAndPostfixFiltered(context.request());
        shouldInjectPrefixAndPostfixFiltered(context.request().getResource());
    }

    private void shouldInjectPrefixAndPostfixFiltered(Adaptable adaptable) {
        InjectedWithPrefixPostfix model = adaptable.adaptTo(InjectedWithPrefixPostfix.class);
        assertNotNull(model);

        assertNotNull(model.getValue());
        assertEquals(PREFIXED_POSTFIXED_TITLE, model.getValue().getTitle());
        assertEquals(PREFIXED_POSTFIXED_VALUE, model.getValue().getValue());

        assertNotNull(model.getValueFromConstructor());
        assertEquals(
            model.getValue().getTitle(),
            model.getValueFromConstructor().getValueMap().get(JcrConstants.JCR_TITLE));
        assertEquals(
            model.getValue().getValue(),
            model.getValueFromConstructor().getValueMap().get(CoreConstants.PN_VALUE));

        assertNotNull(model.getSupplier());
        assertEquals(
            model.getValue().getTitle(),
            model.getSupplier().getValue().getTitle());
        assertEquals(
            model.getValue().getValue(),
            model.getSupplier().getValue().getValue());
    }

    @Test
    public void shouldInjectPrefixAndPostfixFilteredWithChildren() {
        String expectedPath = "/content/jcr:content/list/list_item_4/prefix_nested_postfix";
        shouldInjectFilteredWithChildren(
            context.request().adaptTo(InjectedWithPrefixPostfix.class),
            PREFIXED_POSTFIXED_TITLE,
            expectedPath);
        shouldInjectFilteredWithChildren(
            context.request().getResource().adaptTo(InjectedWithPrefixPostfix.class),
            PREFIXED_POSTFIXED_TITLE,
            expectedPath);
    }

    private void shouldInjectFilteredWithChildren(
        InjectedWithSelection model,
        String expectedTitle,
        String expectedPath) {

        assertNotNull(model);
        assertNotNull(model.getValueFromConstructor());
        List<Resource> filteredChildren = StreamSupport.stream(model.getValueFromConstructor().getChildren().spliterator(),false)
            .collect(Collectors.toList());
        assertEquals(1, filteredChildren.size());
        assertEquals(NESTED_RESOURCE_VALUE, filteredChildren.get(0).getValueMap().get(CoreConstants.PN_VALUE));

        assertNotNull(model.getExtendedValue());
        assertEquals(expectedTitle, model.getExtendedValue().getTitle());

        ExtendedListItem.Nested nestedViaChild = model.getExtendedValue().getNestedViaChild();
        ExtendedListItem.Nested nestedViaChildResource = model.getExtendedValue().getNestedViaChildResource();

        assertNotNull(nestedViaChild);
        assertNotNull(nestedViaChildResource);

        assertEquals(NESTED_RESOURCE_TITLE, nestedViaChild.getNestedTitle());
        assertEquals(NESTED_RESOURCE_VALUE, nestedViaChild.getNestedValue());
        assertEquals(
            expectedPath,
            Objects.requireNonNull(nestedViaChild.getNestedResource()).getPath());
        assertEquals(
            ROOT_PAGE_CONTENT_PATH,
            Objects.requireNonNull(nestedViaChild.getAncestorResource()).getPath());

        assertEquals(nestedViaChild.getNestedTitle(), nestedViaChildResource.getNestedTitle());
        assertEquals(
            nestedViaChild.getNestedResource().getPath(),
            Objects.requireNonNull(nestedViaChildResource.getNestedResource()).getPath());
    }
}
