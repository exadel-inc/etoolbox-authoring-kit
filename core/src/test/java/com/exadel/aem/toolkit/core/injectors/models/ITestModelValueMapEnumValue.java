package com.exadel.aem.toolkit.core.injectors.models;

import com.exadel.aem.toolkit.api.annotations.injectors.EnumValue;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;

@Model(adaptables = SlingHttpServletRequest.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public interface ITestModelValueMapEnumValue {

    @EnumValue
    TestingNeededEnumFromInterface getEnumFromMethod();

    @EnumValue
    TestingNeededEnumFromInterface getEnumFromConstructor();

    enum TestingNeededEnumFromInterface {
        VAL1,VAL2,VAL3
    }
}
