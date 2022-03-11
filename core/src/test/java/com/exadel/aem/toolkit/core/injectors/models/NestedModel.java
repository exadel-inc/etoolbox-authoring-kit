package com.exadel.aem.toolkit.core.injectors.models;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(adaptables = {SlingHttpServletRequest.class, Resource.class}, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class NestedModel {

    @SlingObject
    private SlingHttpServletRequest request;

    @ValueMapValue
    private String value;

    @ValueMapValue(name = "nested/property")
    private String nestedProperty;

    public SlingHttpServletRequest getRequest() {
        return request;
    }

    public String getValue() {
        return value;
    }

    public String getNestedProperty() {
        return nestedProperty;
    }
}
