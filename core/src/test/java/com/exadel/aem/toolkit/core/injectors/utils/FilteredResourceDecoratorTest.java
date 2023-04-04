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
package com.exadel.aem.toolkit.core.injectors.utils;

import java.util.Objects;
import java.util.stream.StreamSupport;

import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import com.day.cq.commons.jcr.JcrConstants;
import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.injectors.ChildInjectorTest;
import com.exadel.aem.toolkit.core.injectors.models.child.InjectedWithSelection;

public class FilteredResourceDecoratorTest {

    @Rule
    public final AemContext context = new AemContext();

    @Before
    public void beforeTest() {
        context.load().json(ChildInjectorTest.MODELS_RESOURCES_FOLDER, ChildInjectorTest.ROOT_JCR_PATH);
        context.currentResource("/content/jcr:content/list/list_item_4");
    }

    @Test
    public void shouldFilterPropertiesByPrefix() {
        Resource decorated = new FilteredResourceDecorator(
            context.request().getResource(),
            InjectedWithSelection.PREFIX,
            null);
        assertTrue(decorated
            .getValueMap()
            .entrySet()
            .stream()
            .filter(entry -> !JcrConstants.JCR_PRIMARYTYPE.equals(entry.getKey()))
            .allMatch(entry -> entry.getValue().toString().startsWith("alt_")));
    }

    @Test
    public void shouldFilterPropertiesByPostfix() {
        Resource decorated = new FilteredResourceDecorator(
            context.request().getResource(),
            null,
            InjectedWithSelection.POSTFIX);
        assertTrue(decorated
            .getValueMap()
            .entrySet()
            .stream()
            .filter(entry -> !JcrConstants.JCR_PRIMARYTYPE.equals(entry.getKey()))
            .allMatch(entry -> entry.getValue().toString().endsWith("_alt")));
    }

    @Test
    public void shouldFilterPropertiesByPrefixAndPostfix() {
        Resource decorated = new FilteredResourceDecorator(
            context.request().getResource(),
            InjectedWithSelection.PREFIX,
            InjectedWithSelection.POSTFIX);
        assertTrue(decorated
            .getValueMap()
            .entrySet()
            .stream()
            .filter(entry -> !JcrConstants.JCR_PRIMARYTYPE.equals(entry.getKey()))
            .allMatch(entry -> entry.getValue().toString().startsWith("alt_") && entry.getValue().toString().endsWith("_alt")));
    }

    @Test
    public void shouldFilterChildrenByPrefixOrPostfix() {
        Resource decoratedWithPrefix = new FilteredResourceDecorator(
            context.request().getResource(),
            InjectedWithSelection.PREFIX,
            null);
        Resource decoratedWithPostfix = new FilteredResourceDecorator(
            context.request().getResource(),
            null,
            InjectedWithSelection.POSTFIX);
        Resource decoratedWithPrefixAndPostfix = new FilteredResourceDecorator(
            context.request().getResource(),
            InjectedWithSelection.PREFIX,
            InjectedWithSelection.POSTFIX);
        assertTrue(decoratedWithPrefix.hasChildren());
        assertEquals(2, StreamSupport.stream(decoratedWithPrefix.getChildren().spliterator(),false).count());
        assertTrue(decoratedWithPostfix.hasChildren());
        assertEquals(2, StreamSupport.stream(decoratedWithPostfix.getChildren().spliterator(),false).count());
        assertTrue(decoratedWithPrefixAndPostfix.hasChildren());
        assertEquals(1, StreamSupport.stream(decoratedWithPrefixAndPostfix.getChildren().spliterator(),false).count());
    }

    @Test
    public void shouldGetChildByInjectingPrefixAndPostfix() {
        Resource decorated = new FilteredResourceDecorator(
            context.request().getResource(),
            InjectedWithSelection.PREFIX,
            InjectedWithSelection.POSTFIX);
        assertEquals(
            "list",
            Objects.requireNonNull(decorated.getChild(CoreConstants.PARENT_PATH)).getName());
        assertEquals(
            JcrConstants.JCR_CONTENT,
            Objects.requireNonNull(decorated.getChild("../..")).getName());
        assertEquals(
            ChildInjectorTest.NESTED_RESOURCE_TITLE,
            Objects.requireNonNull(decorated.getChild("../../defaultChild")).getValueMap().get(JcrConstants.JCR_TITLE));
        assertEquals(
            ChildInjectorTest.NESTED_RESOURCE_TITLE,
            Objects.requireNonNull(decorated.getChild("nested")).getValueMap().get(CoreConstants.PN_TITLE));
        assertEquals(
            ChildInjectorTest.NESTED_RESOURCE_TITLE,
            Objects.requireNonNull(decorated.getChild("nested/deeper_nested")).getValueMap().get(CoreConstants.PN_TITLE));
        assertEquals(
            ChildInjectorTest.NESTED_RESOURCE_TITLE,
            Objects.requireNonNull(decorated.getChild("../list_item_4/nested/deeper_nested")).getValueMap().get(CoreConstants.PN_TITLE));
    }
}
