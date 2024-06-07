/*
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.exadel.aem.toolkit.plugin.handlers.common.cases.policies;

import com.exadel.aem.toolkit.api.annotations.editconfig.ChildEditConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditorType;
import com.exadel.aem.toolkit.api.annotations.editconfig.InplaceEditingConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.listener.Listener;
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.policies.AllowedChildren;
import com.exadel.aem.toolkit.api.annotations.policies.PolicyMergeMode;
import com.exadel.aem.toolkit.api.annotations.policies.PolicyTarget;
import com.exadel.aem.toolkit.plugin.handlers.common.cases.components.ComplexComponent1;
import com.exadel.aem.toolkit.plugin.handlers.common.cases.components.ComplexComponent2;
import com.exadel.aem.toolkit.plugin.maven.TestConstants;

public class AllowedChildrenTestCases {

    private static final String RESOURCE_TYPES_ARRAY = "res/Type1, " +
        "res/Type2, " +
        "res/Type3";

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
    public static class SimpleContainer {
    }

    @AemComponent(
        title = TestConstants.DEFAULT_COMPONENT_TITLE,
        path = TestConstants.DEFAULT_COMPONENT_NAME
    )
    @AllowedChildren(
        classes = {ComplexComponent1.class, ComplexComponent2.class},
        pagePaths = {"page/Path1, page/Path2"},
        targetContainer = PolicyTarget.CURRENT,
        mode = PolicyMergeMode.MERGE
    )
    public static class ClassBasedContainer {
    }

    @AemComponent(
        title = TestConstants.DEFAULT_COMPONENT_TITLE,
        path = TestConstants.DEFAULT_COMPONENT_NAME
    )
    @AllowedChildren(
        value = "restype1",
        classes = {ComplexComponent1.class, ComplexComponent2.class},
        pagePaths = {"page/Path1, page/Path2"},
        targetContainer = PolicyTarget.CURRENT,
        mode = PolicyMergeMode.MERGE
    )
    public static class MixedContainer {
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
    public static class ContainerChildEditConfig {
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
            value = RESOURCE_TYPES_ARRAY,
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
    public static class ContainerEditConfig {
    }

    @AemComponent(
        title = TestConstants.DEFAULT_COMPONENT_TITLE,
        path = TestConstants.DEFAULT_COMPONENT_NAME,
        views = View2.class
    )
    @AllowedChildren(
        classes = ComplexComponent1.class,
        pagePaths = {"page/Path1"},
        targetContainer = PolicyTarget.CURRENT,
        mode = PolicyMergeMode.MERGE
    )
    public static class ContainerWithViews {
    }

    @AllowedChildren(
        value = "restype1",
        classes = ComplexComponent2.class,
        pagePaths = {"page/Path2"},
        targetContainer = PolicyTarget.CURRENT,
        mode = PolicyMergeMode.MERGE
    )
    static class View2 {
    }
}
