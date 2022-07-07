package com.exadel.aem.toolkit.test.common;

import com.exadel.aem.toolkit.api.annotations.editconfig.ChildEditConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditorType;
import com.exadel.aem.toolkit.api.annotations.editconfig.InplaceEditingConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.listener.Listener;
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.policies.AllowedChildren;
import com.exadel.aem.toolkit.api.annotations.policies.PolicyMergeMode;
import com.exadel.aem.toolkit.api.annotations.policies.PolicyTarget;
import com.exadel.aem.toolkit.plugin.utils.TestConstants;

public class AllowedChildrenAnnotation {

    @AemComponent(
            title = TestConstants.DEFAULT_COMPONENT_TITLE,
            path = TestConstants.DEFAULT_COMPONENT_NAME
    )
    @AllowedChildren(
            value = {"res/Type1", "res/Type2", "res/Type3"},
            pagePaths = {"page/Path1, page/Path2"},
            pageResourceTypes = {"page/Res/Type1", "page/Res/Type2"},
            resourceNames = {"resource1, resource2", "resource3"},
            parents = {"parent/Res/Type1", "parent/Res/Type2"},
            targetContainer = PolicyTarget.CURRENT,
            mode = PolicyMergeMode.MERGE
    )
    @AllowedChildren(
            value = {"res/Type1", "res/Type2", "res/Type3"},
            pagePaths = {"page/Path1, page/Path2"},
            pageResourceTypes = {"page/Res/Type1", "page/Res/Type2"},
            templates = {"template1, template2"},
            resourceNames = {"resource1, resource2", "resource3"},
            mode = PolicyMergeMode.OVERRIDE
    )
    public static class Test1 {
    }

    @AemComponent(
            title = TestConstants.DEFAULT_COMPONENT_TITLE,
            path = TestConstants.DEFAULT_COMPONENT_NAME
    )
    @ChildEditConfig(
            listeners = {
                    @Listener(event = "event1", action = "action1"),
                    @Listener(event = "event2", action = "action2"),
                    @Listener(event = "event3", action = "action3")
            }
    )
    @AllowedChildren(
            value = {"res/Type1", "res/Type2", "res/Type3"},
            pagePaths = {"page/Path1, page/Path2"},
            pageResourceTypes = {"page/Res/Type1", "page/Res/Type2"},
            templates = {"template1, template2"},
            resourceNames = {"resource1, resource2", "resource3"},
            parents = {"parent/Res/Type1 group:'Containers and Buttons'", "parent/Res/Type2 group:`Containers`"},
            mode = PolicyMergeMode.MERGE
    )
    @AllowedChildren(
            value = "res/Type1",
            templates = {"template1, template2"},
            resourceNames = {"resource1, resource2", "resource3"}
    )
    @AllowedChildren(
            value = {}
    )
    public static class Test2 {
    }

    @AemComponent(
            title = TestConstants.DEFAULT_COMPONENT_TITLE,
            path = TestConstants.DEFAULT_COMPONENT_NAME
    )
    @EditConfig(
            inplaceEditing = @InplaceEditingConfig(
                    type = EditorType.TEXT,
                    editElementQuery = ".editable-header",
                    name = "header",
                    propertyName = "header"
            )
    )
    @AllowedChildren(
            value = {"res/Type1", "res/Type2", "res/Type3"},
            pagePaths = {"page/Path1, page/Path2"},
            pageResourceTypes = {"page/Res/Type1", "page/Res/Type2"},
            templates = {"template1, template2"},
            resourceNames = {"resource1, resource2", "resource3"},
            parents = {"parent/Res/Type1", "parent/Res/Type2"},
            targetContainer = PolicyTarget.CURRENT,
            mode = PolicyMergeMode.OVERRIDE
    )
    @AllowedChildren(
            value = "res/Type1",
            templates = {"template1, template2"},
            resourceNames = {"resource1, resource2", "resource3"},
            targetContainer = PolicyTarget.CURRENT
    )
    @AllowedChildren(
            value = {},
            targetContainer = PolicyTarget.CURRENT
    )
    public static class Test3 {
    }
}
