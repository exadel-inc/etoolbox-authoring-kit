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
package com.exadel.aem.toolkit.core.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.junit.Rule;
import org.junit.Test;
import com.day.cq.commons.jcr.JcrConstants;
import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.core.AemContextFactory;
import com.exadel.aem.toolkit.core.CoreConstants;

public class ResourceFactoryTest {

    @Rule
    public final AemContext context = AemContextFactory.newInstance();

    @Test
    public void shouldBuildSimpleResource() {
        Resource resource = ResourceFactory.newResource(context.resourceResolver())
            .path("content/test")
            .resourceType("acme/components/test")
            .property("title", "Hello")
            .property("count", 42)
            .build();

        assertNotNull(resource);
        assertEquals("content/test", resource.getPath());
        assertEquals("acme/components/test", resource.getResourceType());
        assertEquals("Hello", resource.getValueMap().get("title", String.class));
        assertEquals(42, (int) resource.getValueMap().get("count", 0));
    }

    @Test
    public void shouldUseDefaultResourceTypeWhenNoneSet() {
        Resource resource = ResourceFactory.newResource(context.resourceResolver())
            .path("content/test")
            .build();

        assertNotNull(resource);
        assertEquals(JcrConstants.NT_UNSTRUCTURED, resource.getResourceType());
    }

    @Test
    public void shouldBuildResourceWithMultipleChildren() {
        Resource child1 = ResourceFactory.newResource(context.resourceResolver())
            .path("child1").property("key", "val1").build();
        Resource child2 = ResourceFactory.newResource(context.resourceResolver())
            .path("child2").property("key", "val2").build();

        Resource parent = ResourceFactory.newResource(context.resourceResolver())
            .path("parent")
            .child(child1)
            .children(Collections.singletonList(child2))
            .build();

        assertNotNull(parent);
        Iterator<Resource> children = parent.listChildren();
        assertNotNull(children);
        assertTrue(children.hasNext());
        Resource firstChild = children.next();
        assertNotNull(firstChild);
        assertEquals("val1", firstChild.getValueMap().get("key", String.class));
        assertTrue(children.hasNext());
        assertEquals("val2", children.next().getValueMap().get("key", String.class));
        assertFalse(children.hasNext());
    }

    @Test
    public void shouldBuildResourceWithNestedProperties() {
        Resource resource = ResourceFactory.newResource(context.resourceResolver())
            .path("content/composite")
            .property("title", "Main")
            .property("alpha/text", "Nested text")
            .property("alpha/value", "Nested value")
            .property("beta/text", "More nested text")
            .build();

        assertNotNull(resource);
        assertEquals("Main", resource.getValueMap().get("title", String.class));

        Iterator<Resource> children = resource.listChildren();
        assertNotNull(children);
        assertTrue(children.hasNext());

        Resource alphaChild = children.next();
        assertNotNull(alphaChild);
        assertEquals("alpha", alphaChild.getName());
        assertEquals("Nested text", alphaChild.getValueMap().get("text", String.class));
        assertEquals("Nested value", alphaChild.getValueMap().get("value", String.class));

        Resource betaChild = children.next();
        assertNotNull(betaChild);
        assertEquals("More nested text", betaChild.getValueMap().get("text", String.class));
    }

    @Test
    public void shouldBuildResourceWithDeeplyNestedProperties() {
        Resource resource = ResourceFactory.newResource(context.resourceResolver())
            .path("content/node")
            .property("title", "Root")
            .property("l1/l2/l3/prop", "deep")
            .build();

        assertNotNull(resource);
        assertEquals("Root", resource.getValueMap().get("title", String.class));

        Iterator<Resource> children = resource.listChildren();
        assertNotNull(children);
        assertTrue(children.hasNext());
        Resource level1Child = children.next();
        assertNotNull(level1Child);
        assertEquals(JcrConstants.NT_UNSTRUCTURED, level1Child.getResourceType());

        Iterator<Resource> l1Children = level1Child.listChildren();
        assertNotNull(l1Children);
        assertTrue(l1Children.hasNext());
        Resource level2Child = l1Children.next();
        assertNotNull(level2Child);
        assertEquals(JcrConstants.NT_UNSTRUCTURED, level2Child.getResourceType());

        Iterator<Resource> l2Children = level2Child.listChildren();
        assertNotNull(l2Children);
        assertTrue(l2Children.hasNext());
        Resource level3Child = l2Children.next();
        assertNotNull(level3Child);
        assertEquals("deep", level3Child.getValueMap().get("prop", String.class));
    }

    @Test
    public void shouldNormalizePathSegments() {
        Resource resource = ResourceFactory.newResource(context.resourceResolver())
            .path("/content/", "/my-page/", "/node")
            .build();

        assertNotNull(resource);
        assertEquals("content/my-page/node", resource.getPath());

        ResourceFactory.Builder<?> builder = ResourceFactory.newResource(context.resourceResolver());
        builder.path("content", "", "page");
        assertEquals("content/page", builder.getPath());

        ResourceFactory.Builder<?> allBlankBuilder = ResourceFactory.newResource(context.resourceResolver());
        allBlankBuilder.path("/", "/");
        assertNull(allBlankBuilder.getPath());
    }

    @Test
    public void shouldIncludeExternalChildren() {
        Resource externalChild = ResourceFactory.newResource(context.resourceResolver())
            .path("extra").property("from", "external").build();

        Resource resource = ResourceFactory.newResource(context.resourceResolver())
            .path("content/composite")
            .property("title", "Main")
            .property("items/text", "NestedText")
            .child(externalChild)
            .build();

        assertNotNull(resource);
        Iterator<Resource> children = resource.listChildren();
        assertNotNull(children);
        assertTrue(children.hasNext());
        children.next();
        assertTrue(children.hasNext());
        children.next();
        assertFalse(children.hasNext());
    }

    @Test
    public void shouldIgnoreNullOrEmptyInputsInBuilder() {
        ResourceFactory.Builder<?> builder = ResourceFactory.newResource(context.resourceResolver());

        builder.child(null);
        builder.children(null);
        builder.children(Collections.emptyList());
        assertNull(builder.getChildren());

        builder.path();
        assertNull(builder.getPath());

        builder.property(null, "val");
        builder.property(StringUtils.EMPTY, "val");
        builder.property("key", null);
        builder.properties(null);
        builder.properties(Collections.emptyMap());
        assertNull(builder.getProperties());

        builder.resourceType(null);
        builder.resourceType("");
        assertEquals(JcrConstants.NT_UNSTRUCTURED, builder.getResourceType());
    }

    @Test
    public void shouldBuildGraniteFieldWithIncrementingPaths() {
        context.create().resource("/content/myForm");
        context.request().setResource(context.resourceResolver().getResource("/content/myForm"));

        ResourceFactory.FieldBuilder builder1 = ResourceFactory.newGraniteField(context.request());
        ResourceFactory.FieldBuilder builder2 = ResourceFactory.newGraniteField(context.request());

        assertEquals("content/myForm/field0", builder1.getPath());
        assertEquals("content/myForm/field1", builder2.getPath());
    }

    @Test
    public void shouldBuildMultifieldWrapper() {
        ResourceFactory.FieldBuilder builder = new ResourceFactory.FieldBuilder(context.resourceResolver());
        builder.path("content/form/myField")
               .resourceType("acme/components/field")
               .property(CoreConstants.PN_FIELD_LABEL, "Multi Label")
               .property("name", "./myField")
               .multi(true);

        Resource wrapper = builder.build();

        assertNotNull(wrapper);
        assertEquals(ResourceTypes.MULTIFIELD, wrapper.getResourceType());
        assertEquals("Multi Label", wrapper.getValueMap().get(CoreConstants.PN_FIELD_LABEL, String.class));

        Iterator<Resource> children = wrapper.listChildren();
        assertNotNull(children);
        assertTrue(children.hasNext());
        Resource nestedField = children.next();
        assertNotNull(nestedField);
        assertEquals("acme/components/field", nestedField.getResourceType());
    }

    @Test
    public void shouldBuildCompositeMultifieldWrapper() {
        ResourceFactory.FieldBuilder builder = new ResourceFactory.FieldBuilder(context.resourceResolver());
        builder.path("content/form/compositeField")
               .resourceType(ResourceTypes.CONTAINER)
               .property(CoreConstants.PN_FIELD_LABEL, "Composite Label")
               .multi(true);

        Resource wrapper = builder.build();

        assertNotNull(wrapper);
        assertEquals(ResourceTypes.MULTIFIELD, wrapper.getResourceType());
        boolean composite = wrapper.getValueMap().get("composite", false);
        assertTrue(composite);
    }

    @Test
    public void shouldPassPropertiesMapToBuilder() {
        Map<String, Object> props = new HashMap<>();
        props.put("alpha", "a");
        props.put("beta", "b");

        Resource resource = ResourceFactory.newResource(context.resourceResolver())
            .path("content/test")
            .properties(props)
            .build();

        assertNotNull(resource);
        assertEquals("a", resource.getValueMap().get("alpha", String.class));
        assertEquals("b", resource.getValueMap().get("beta", String.class));
    }

    @Test
    public void shouldBuildChildrenFromCollection() {
        Resource child1 = ResourceFactory.newResource(context.resourceResolver())
            .path("c1").property("v", "one").build();
        Resource child2 = ResourceFactory.newResource(context.resourceResolver())
            .path("c2").property("v", "two").build();

        Resource parent = ResourceFactory.newResource(context.resourceResolver())
            .path("parent")
            .children(Arrays.asList(child1, child2))
            .build();

        assertNotNull(parent);
        Iterator<Resource> it = parent.listChildren();
        assertNotNull(it);
        assertTrue(it.hasNext());
        it.next();
        assertTrue(it.hasNext());
        it.next();
        assertFalse(it.hasNext());
    }
}
