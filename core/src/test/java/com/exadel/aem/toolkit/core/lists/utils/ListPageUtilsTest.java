package com.exadel.aem.toolkit.core.lists.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.WCMException;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.lists.ListConstants;

import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class ListPageUtilsTest {

    @Rule
    public AemContext context = new AemContext(ResourceResolverType.RESOURCERESOLVER_MOCK);

    @Test
    public void shouldNotCreatePageIfPathIsBlank() throws WCMException {
        Page listPage = ListPageUtils.createListPage(context.resourceResolver(), "");

        assertNull(listPage);
    }

    @Test
    public void shouldNotCreatePageIfCannotGetPageManager() throws WCMException {
        // needed to emulate a case when pageManager is null
        ResourceResolver mockedResourceResolver = mock(ResourceResolver.class);

        Page listPage = ListPageUtils.createListPage(mockedResourceResolver, "/test/path");

        assertNull(listPage);
    }

    @Test
    public void shouldThrowWCMExceptionIfParentDoesNotExist() {
        assertThrows(WCMException.class, () -> ListPageUtils.createListPage(context.resourceResolver(), "/content/test"));
    }

    @Test
    public void shouldCreateListPageUnderGivenPath() throws WCMException {
        context.create().page("/content");

        Page listPage = ListPageUtils.createListPage(context.resourceResolver(), "/content/test");

        assertNotNull(listPage);
        ValueMap properties = listPage.getProperties();
        assertEquals(ListConstants.LIST_TEMPLATE_NAME, properties.get(NameConstants.NN_TEMPLATE, StringUtils.EMPTY));
        assertEquals(ListConstants.SIMPLE_LIST_ITEM_RESOURCE_TYPE, properties.get(CoreConstants.PN_ITEM_RESOURCE_TYPE, StringUtils.EMPTY));
    }
}
