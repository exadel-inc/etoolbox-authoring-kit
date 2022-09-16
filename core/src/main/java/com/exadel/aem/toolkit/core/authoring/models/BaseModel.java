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
import javax.annotation.PostConstruct;
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
import com.adobe.granite.ui.components.Config;
import com.adobe.granite.ui.components.Value;
import com.adobe.granite.ui.components.htl.ComponentHelper;

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.core.CoreConstants;

/**
 * Presents the basic logic for rendering Granite UI components with the use of Sling models and HTL markup. This class
 * manages only the common component properties and is expected to be extended with component-specific Sling models for
 * all cases but the most basic ones
 */
@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
@AemComponent(
    path = "components/authoring/base",
    title = "Touch UI Component Base",
    resourceSuperType = "granite/ui/components/coral/foundation/form/field"
)
public class BaseModel {

    private static final String ATTRIBUTE_ARIA_REQUIRED = "aria-required";
    private static final String ATTRIBUTE_FOUNDATION_VALIDATION = "data-foundation-validation";

    @SlingObject
    private SlingHttpServletRequest request;

    @ValueMapValue
    private String name;

    private LocalComponentHelper componentHelper;

    /**
     * Performs post-inject model initialization
     */
    @PostConstruct
    private void init() {
        componentHelper = new LocalComponentHelper(request);
    }

    /**
     * Retrieves common attributes of a Granit UI component such as {@code required}, {@code disabled}, etc. These
     * attributes are typically rendered to the main container of the component's HTML markup
     * @return Map of string values
     */
    public Map<String, String> getCommonAttributes() {
        return componentHelper.getCommonAttributes();
    }

    /**
     * Retrieves the value of the {@code name} attribute of the editable component
     * @return String value; a non-blank string is expected
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves the value of the editable component
     * @return A nullable string value
     */
    public Object getValue() {
        if (request == null
            || request.getRequestPathInfo().getSuffixResource() == null
            || StringUtils.isBlank(name)) {
            return getDefaultValue();
        }

        Resource dataResource = getDataResource();
        Config config = new Config(dataResource);
        Value value = new Value(request, config);

        String propertyName = name.contains(CoreConstants.SEPARATOR_SLASH)
            ? StringUtils.substringAfterLast(name, CoreConstants.SEPARATOR_SLASH)
            : name;
        return value.get(CoreConstants.RELATIVE_PATH_PREFIX + propertyName);
    }

    /**
     * Retrieves the value which is rendered when no user-defined value is set for this editable component
     * @return A nullable string value
     */
    public Object getDefaultValue() {
        return null;
    }

    /**
     * Retrieves the {@link Resource} that contains data for the current component
     * @return {@code Resource} object
     */
    private Resource getDataResource() {
        Resource suffixResource = request.getRequestPathInfo().getSuffixResource();
        String relativePath = name.contains(CoreConstants.SEPARATOR_SLASH)
            ? StringUtils.substringBeforeLast(name, CoreConstants.SEPARATOR_SLASH)
            : StringUtils.EMPTY;

        return StringUtils.isNotBlank(relativePath)
            ? request.getResourceResolver().getResource(suffixResource, relativePath)
            : suffixResource;
    }

    /**
     * Implements {@link ComponentHelper} to provide common HTML attributes for the Granite UI components rendered with
     * Sling models and HTL
     */
    private static class LocalComponentHelper extends ComponentHelper {
        private SlingHttpServletRequest request;

        /**
         * Creates a new {@link ComponentHelper} instance
         * @param request {@code SlingHttpServletRequest} used to construct the current component
         */
        public LocalComponentHelper(SlingHttpServletRequest request) {
            if (request == null) {
                return;
            }
            this.request = request;
            init(new SimpleBindings((SlingBindings) request.getAttribute(SlingBindings.class.getName())));
        }

        /**
         * Retrieves common HTML attributes for the Granite UI components
         * @return A non-null {@code Map}
         */
        public Map<String, String> getCommonAttributes() {
            AttrBuilder attrBuilder = getInheritedAttrs();
            populateCommonAttrs(attrBuilder);
            Map<String, String> result = attrBuilder.getData();
            if (request == null) {
                return result;
            }
            if (request.getResource().getValueMap().get(CoreConstants.PN_DISABLED, false)) {
                result.put(CoreConstants.PN_DISABLED, Boolean.TRUE.toString());
            }
            if (request.getResource().getValueMap().get(CoreConstants.PN_REQUIRED, false)) {
                result.put(ATTRIBUTE_ARIA_REQUIRED, Boolean.TRUE.toString());
            }
            String validation = StringUtils.join(
                CoreConstants.SEPARATOR_COMMA,
                request.getResource().getValueMap().get(CoreConstants.PN_VALIDATION, String[].class));
            if (StringUtils.isNotBlank(validation)) {
                result.put(ATTRIBUTE_FOUNDATION_VALIDATION, validation);
            }
            return result;
        }

        /**
         * A required stub method according the {@code ComponentHelper} contract
         */
        @Override
        protected void activate() {
            //  Not Implemented
        }
    }
}
