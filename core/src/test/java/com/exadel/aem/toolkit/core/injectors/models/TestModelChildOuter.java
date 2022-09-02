package com.exadel.aem.toolkit.core.injectors.models;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.exadel.aem.toolkit.api.annotations.injectors.Child;

@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class TestModelChildOuter {
    @ValueMapValue
    private String outerModelStringField;

    @Child(prefix = "prefix-", postfix = "-postfix")
    private TestModelChildInner testModelChildInner;

    @Child(name = "testName")
    private TestModelChildInner testModelChildInnerWithDifferentName;

    public String getOuterModelStringField() {
        return outerModelStringField;
    }
    public TestModelChildInner getTestModelChildInner() {
        return testModelChildInner;
    }

    public TestModelChildInner getTestModelChildInnerWithDifferentName() {
        return testModelChildInnerWithDifferentName;
    }
}
