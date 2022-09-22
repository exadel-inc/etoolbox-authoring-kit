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
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.policies.AllowedChildren;

@AemComponent(
    path = "/apps/etoolbox-authoring-kit-test/components/content/container1",
    title = "Container Component 1",
    componentGroup="Components",
    isContainer = true,
    disableTargeting = true
)
@Dialog
@AllowedChildren(
    value = {
        "/apps/etoolbox-authoring-kit-test/components/content/video",
        "/apps/etoolbox-authoring-kit-test/components/content/audio"
    },
    pageResourceTypes = "etoolbox-authoring-kit-test/components/pages/editable-template-page-descendant"
)
@AllowedChildren(
    value = "/apps/etoolbox-authoring-kit-test/components/content/video",
    classes = ContainerComponent2.class
)
public class ContainerComponent1 {
}
