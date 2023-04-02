package com.exadel.aem.toolkit.core.injectors;

import org.apache.sling.testing.mock.sling.MockAdapterManagerImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.exadel.aem.toolkit.core.injectors.models.TestModelChildRootResource;

import io.wcm.testing.mock.aem.junit.AemContext;
import static org.junit.Assert.assertEquals;

public class ChildInjectorSubResourceTest {
    @Rule
    public final AemContext context = new AemContext();

    public TestModelChildRootResource testModel;

    @Before
    public void beforeTest() {
        context.addModelsForClasses(TestModelChildRootResource.class, TestModelChildRootResource.class);
        context.registerInjectActivateService(new ChildInjector());
        context.registerInjectActivateService(new MockAdapterManagerImpl());
        context.load().json("/com/exadel/aem/toolkit/core/injectors/childinjectorsubresource.json", "/content");
        context.request().setResource(context.resourceResolver().getResource("/content/testModel"));
        testModel = context.request().adaptTo(TestModelChildRootResource.class);
    }

    @Test
    public void checkTestModelChildRootStringField() {
        assertEquals("Outer model string field", testModel.getTestModelChildRootResourceStringField());
    }

    @Test
    public void checkTestModelChildSubResourceWithPrefixPostfixStringField() {
        assertEquals("Inner model text field", testModel.getTestModelChildSubResource().getSubResourceStringField());
    }

    @Test
    public void checkTestModelChildSubResourceWithPrefixPostfixResource() {
        assertEquals("Inner model resource title", testModel.getTestModelChildSubResource().getSubResource().getValueMap().get("title"));
    }

    @Test
    public void checkTestModelChildSubResourceWithRelativePathStringField() {
        assertEquals("Second inner model text field", testModel.getTestModelChildSubResourceWithDifferentName().getSubResourceStringField());
    }

    @Test
    public void checkTestModelChildSubResourceWithRelativePathResource() {
        assertEquals("Second inner model resource title", testModel.getTestModelChildSubResourceWithDifferentName().getSubResource().getValueMap().get("title"));
    }
}