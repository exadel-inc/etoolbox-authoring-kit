package com.exadel.aem.toolkit.core.injectors;

import org.apache.sling.testing.mock.sling.MockAdapterManagerImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.exadel.aem.toolkit.core.injectors.models.TestModelChildOuter;

import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertEquals;

public class ChildInjectorSubResourceTest {
    @Rule
    public final AemContext context = new AemContext();

    @Before
    public void beforeTest() {
        context.addModelsForClasses(TestModelChildOuter.class, TestModelChildOuter.class);
        context.registerInjectActivateService(new ChildInjector());
        context.registerInjectActivateService(new MockAdapterManagerImpl());
        context.load().json("/com/exadel/aem/toolkit/core/injectors/childinjectorsubresource.json", "/content");
    }

    @Test
    public void shouldInjectWithStandardFieldNames() {
        context.request().setResource(context.resourceResolver().getResource("/content/testModel"));

        TestModelChildOuter testModel = context.request().adaptTo(TestModelChildOuter.class);

        assertEquals("Outer model string field", testModel.getOuterModelStringField());
        assertEquals("Inner model text field", testModel.getTestModelChildInner().getInnerModelStringField());
        assertEquals("Inner model resource title", testModel.getTestModelChildInner().getInnerModelResource().getValueMap().get("title"));
        assertEquals("Second inner model text field", testModel.getTestModelChildInnerWithDifferentName().getInnerModelStringField());
        assertEquals("Second inner model resource title", testModel.getTestModelChildInnerWithDifferentName().getInnerModelResource().getValueMap().get("title"));
    }
}
