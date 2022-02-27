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
package com.exadel.aem.toolkit.test.common;

import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditorType;
import com.exadel.aem.toolkit.api.annotations.editconfig.InplaceEditingConfig;
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.policies.AllowedChildren;
import com.exadel.aem.toolkit.plugin.utils.TestConstants;

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
        applyToCurrent = true
)
@AllowedChildren(
        value = "res/Type1",
        templates = {"template1, template2"},
        resourceNames = {"resource1, resource2", "resource3"},
        applyToCurrent = true
)
@AllowedChildren(
        value = {},
        applyToCurrent = true
)
public class AllowedChildrenAnnotation3 {
}
