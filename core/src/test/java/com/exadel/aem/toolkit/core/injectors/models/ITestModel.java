package com.exadel.aem.toolkit.core.injectors.models;

import com.exadel.aem.toolkit.core.injectors.annotations.RequestSelectors;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;

@Model(adaptables = SlingHttpServletRequest.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public interface ITestModel {

    @RequestSelectors
    String getSelectorsFromMethod();
}
