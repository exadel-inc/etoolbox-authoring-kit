package com.exadel.aem.toolkit.core.injectors.models;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.exadel.aem.toolkit.api.annotations.injectors.Child;

@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class TestModelChildSubResource {
    @Child
    private Resource subResource;

    @ValueMapValue(name = "stringFieldResource/stringField")
    private String subResourceStringField;

    public Resource getSubResource() {
        return subResource;
    }
    public String getSubResourceStringField() {
        return subResourceStringField;
    }
}