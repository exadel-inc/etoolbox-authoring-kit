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
import com.exadel.aem.toolkit.it.cases.Constants;

@AemComponent(
    path = Constants.JCR_PAGES_ROOT + "/static-template-page",
    title = "Static Page",
    resourceSuperType = "wcm/foundation/components/page"
)
@AllowedChildren(
    value = Constants.JCR_COMPONENTS_ROOT + "/video",
    classes = ContainerComponent1.class,
    pagePaths = "*/allowedChildren/*",
    resourceNames = "parsys_0"
)
@AllowedChildren(
    value = Constants.JCR_COMPONENTS_ROOT + "/video",
    classes = ContainerComponent2.class,
    templates = "*/other-template",
    resourceNames = "parsys_1"
)
@AllowedChildren(
    value = Constants.JCR_COMPONENTS_ROOT + "/video",
    resourceNames = "parsys_0"
)
@AllowedChildren(
    value = Constants.JCR_COMPONENTS_ROOT + "/video",
    resourceNames = "parsys_1",
    mode = PolicyMergeMode.MERGE
)
public class StaticPage {
}
