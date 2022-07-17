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
package com.exadel.aem.toolkit.core.authoring.models;

import java.util.Map;
import javax.script.SimpleBindings;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import com.adobe.granite.ui.components.AttrBuilder;
import com.adobe.granite.ui.components.htl.ComponentHelper;

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.core.CoreConstants;

@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
@AemComponent(
    path = "components/authoring/base",
    title = "Touch UI Component Base",
    resourceSuperType = "granite/ui/components/coral/foundation/form/field"
)
public class BaseModel {

    @SlingObject
    private SlingHttpServletRequest request;

    @ValueMapValue
    private String name;

    private LocalComponentHelper componentHelper;

    public Map<String, String> getCommonAttributes() {
        return getComponentHelper().getCommonAttributes();
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        if (request == null
            || request.getRequestPathInfo().getSuffixResource() == null
            || StringUtils.isBlank(name)) {
            return getDefaultValue();
        }

        Resource suffixResource = request.getRequestPathInfo().getSuffixResource();
        String relativePath = name.contains(CoreConstants.SEPARATOR_SLASH)
            ? StringUtils.substringBeforeLast(name, CoreConstants.SEPARATOR_SLASH)
            : StringUtils.EMPTY;
        String propertyName = name.contains(CoreConstants.SEPARATOR_SLASH)
            ? StringUtils.substringAfterLast(name, CoreConstants.SEPARATOR_SLASH)
            : name;

        Resource endResource = StringUtils.isNotBlank(relativePath)
            ? request.getResourceResolver().getResource(suffixResource, relativePath)
            : suffixResource;
        if (endResource == null) {
            return getDefaultValue();
        }

        return endResource.getValueMap().get(propertyName);
    }


    public Object getDefaultValue() {
        return null;
    }

    private LocalComponentHelper getComponentHelper() {
        if (componentHelper == null) {
            componentHelper = new LocalComponentHelper(request);
        }
        return componentHelper;
    }

    private static class LocalComponentHelper extends ComponentHelper {

        public LocalComponentHelper(SlingHttpServletRequest request) {
            if (request == null) {
                return;
            }
            init(new SimpleBindings((SlingBindings) request.getAttribute(SlingBindings.class.getName())));
        }

        public Map<String, String> getCommonAttributes() {
            AttrBuilder attrBuilder = getInheritedAttrs();
            populateCommonAttrs(attrBuilder);
            return attrBuilder.getData();
        }

        @Override
        protected void activate() {
            //  Not implemented
        }
    }
}
