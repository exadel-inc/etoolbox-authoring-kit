package com.exadel.aem.toolkit.core.injectors.models;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;

import com.exadel.aem.toolkit.core.injectors.annotations.RequestSuffix;

@Model(adaptables = SlingHttpServletRequest.class)
public interface ITestModelSuffix {

    @RequestSuffix
    String getSuffixFromMethod();
}
