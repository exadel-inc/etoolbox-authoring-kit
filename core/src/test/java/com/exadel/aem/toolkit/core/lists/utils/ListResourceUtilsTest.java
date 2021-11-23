package com.exadel.aem.toolkit.core.lists.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.lists.ListConstants;
import com.exadel.aem.toolkit.core.lists.models.SimpleListItem;

import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class ListResourceUtilsTest {

    @Rule
    public AemContext context = new AemContext(ResourceResolverType.RESOURCERESOLVER_MOCK);

    @Test
    public void shouldReturnNullIfResourceResolverIsNull() throws PersistenceException {
        Resource listResource = ListResourceUtils.createListResource(null, context.currentPage());

        assertNull(listResource);
    }

    @Test
    public void shouldReturnNullIfParentIsNull() throws PersistenceException {
        Resource listResource = ListResourceUtils.createListResource(context.resourceResolver(), null);

        assertNull(listResource);
    }

    @Test
    public void shouldCreateListResource() throws PersistenceException {
        Page listPage = context.create().page("/content/test");

        Resource listResource = ListResourceUtils.createListResource(context.resourceResolver(), listPage);

        assertEquals(ListConstants.NN_LIST, listResource.getName());
        assertEquals("wcm/foundation/components/responsivegrid", listResource.getResourceType());
    }

    @Test
    public void shouldReturnEmptyListIfSimpleListItemCollectionIsEmpty() {
        List<Resource> resources = ListResourceUtils.mapToValueMapResources(Collections.emptyList());

        assertEquals(Collections.emptyList(), resources);
    }

    @Test
    public void shouldTransformSimpleListItemCollectionToResource() {
        SimpleListItemImpl simpleListItem = new SimpleListItemImpl("first", "firstValue");

        List<Resource> resources = ListResourceUtils.mapToValueMapResources(Collections.singleton(simpleListItem));

        Resource resource = resources.get(0);
        assertNotNull(resource);
        assertEquals("first", resource.getValueMap().get(JcrConstants.JCR_TITLE, StringUtils.EMPTY));
        assertEquals("firstValue", resource.getValueMap().get(CoreConstants.PN_VALUE, StringUtils.EMPTY));
    }

    @Test
    public void shouldTransformKeyValuePairMapToResource() {
        Map<String, Object> properties = Collections.singletonMap("first", "firstValue");

        List<Resource> resources = ListResourceUtils.mapToValueMapResources(properties);

        Resource resource = resources.get(0);
        assertNotNull(resource);
        assertEquals("first", resource.getValueMap().get(JcrConstants.JCR_TITLE, StringUtils.EMPTY));
        assertEquals("firstValue", resource.getValueMap().get(CoreConstants.PN_VALUE, StringUtils.EMPTY));
    }

    @Test
    public void shouldCreateListItemWithoutSystemProperties() throws PersistenceException {
        Page listPage = context.create().page("/content/test");
        Resource listResource = ListResourceUtils.createListResource(context.resourceResolver(), listPage);

        Map<String, Object> actualProperties = new HashMap<>();
        actualProperties.put("first", "firstValue");
        actualProperties.put("jcr:createdBy", "admin");

        ListResourceUtils.createListItem(context.resourceResolver(), listResource, new ValueMapDecorator(actualProperties));

        Resource listItem = listResource.getChild(CoreConstants.PN_LIST_ITEM);
        assertNotNull(listItem);

        Map<String, Object> expectedProperties = new HashMap<>();
        expectedProperties.put("first", "firstValue");
        expectedProperties.put(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, ListConstants.LIST_ITEM_RESOURCE_TYPE);

        assertEquals(expectedProperties, listItem.getValueMap());
    }

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
}
