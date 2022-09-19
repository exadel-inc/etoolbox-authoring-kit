package com.exadel.aem.toolkit.core.injectors.models;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.exadel.aem.toolkit.api.annotations.injectors.Child;

@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class TestModelChildRootResource {
    @ValueMapValue
    private String outerModelStringField;

    @Child(prefix = "prefix-", postfix = "-postfix")
    private TestModelChildSubResource testModelChildSubResource;

    @Child(name = "testName")
    private TestModelChildSubResource testModelChildSubResourceWithDifferentName;

    public String getTestModelChildRootResourceStringField() {
        return outerModelStringField;
    }
    public TestModelChildSubResource getTestModelChildSubResource() {
        return testModelChildSubResource;
    }

    public TestModelChildSubResource getTestModelChildSubResourceWithDifferentName() {
        return testModelChildSubResourceWithDifferentName;
    }
}
