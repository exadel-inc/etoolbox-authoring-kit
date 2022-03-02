package com.exadel.aem.toolkit.core.injectors.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class NestedModel {

    @ValueMapValue
    private String rootProperty;

    @ValueMapValue(name = "nested/property")
    private String nested;

    public String getRootProperty() {
        return rootProperty;
    }

    public String getNested() {
        return nested;
    }
}
