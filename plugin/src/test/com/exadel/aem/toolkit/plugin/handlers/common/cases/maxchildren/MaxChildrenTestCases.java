package com.exadel.aem.toolkit.plugin.handlers.common.cases.maxchildren;

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.policies.AllowedChildren;
import com.exadel.aem.toolkit.api.annotations.policies.MaxChildren;
import com.exadel.aem.toolkit.api.annotations.policies.PolicyMergeMode;
import com.exadel.aem.toolkit.plugin.handlers.common.cases.components.ComplexComponent1;
import com.exadel.aem.toolkit.plugin.handlers.common.cases.components.ComplexComponent2;
import com.exadel.aem.toolkit.plugin.maven.TestConstants;

public class MaxChildrenTestCases {
    @AemComponent(
        title = TestConstants.DEFAULT_COMPONENT_TITLE,
        path = TestConstants.DEFAULT_COMPONENT_NAME
    )
    @MaxChildren(5)
    public static class SimpleMaxLimitAnnotation {
    }

    @AemComponent(
        title = TestConstants.DEFAULT_COMPONENT_TITLE,
        path = TestConstants.DEFAULT_COMPONENT_NAME
    )
    @AllowedChildren(
        classes = {ComplexComponent1.class, ComplexComponent2.class},
        pagePaths = {"page/Path1, page/Path2"},
        mode = PolicyMergeMode.MERGE
    )
    @MaxChildren(1)
    public static class AllowedChildrenWithMaxLimit {
    }
}
