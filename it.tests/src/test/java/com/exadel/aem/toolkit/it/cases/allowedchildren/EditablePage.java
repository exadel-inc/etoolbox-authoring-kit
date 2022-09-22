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
package com.exadel.aem.toolkit.it.cases.allowedchildren;

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.policies.AllowedChildren;
import com.exadel.aem.toolkit.api.annotations.policies.PolicyMergeMode;

@AemComponent(
    path = "/apps/etoolbox-authoring-kit-test/components/pages/editable-template-page",
    title = "Dynamic Page",
    resourceSuperType = "core/wcm/components/page/v2/page"
)
@AllowedChildren(
    value = "/apps/etoolbox-authoring-kit-test/components/content/video",
    resourceNames = "grid_0"
)
@AllowedChildren(
    value = "/apps/etoolbox-authoring-kit-test/components/content/video",
    resourceNames = "grid_1",
    mode = PolicyMergeMode.MERGE
)
public class EditablePage {
}
