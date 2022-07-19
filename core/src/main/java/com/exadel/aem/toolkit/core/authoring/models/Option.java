package com.exadel.aem.toolkit.core.authoring.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class Option {

    @ValueMapValue
    private String text;

    @ValueMapValue
    private String value;

    public String getText() {
        return text;
    }

    public String getValue() {
        return value;
    }
}
