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

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import com.adobe.granite.ui.components.ExpressionResolver;

import com.exadel.aem.toolkit.api.annotations.injectors.RequestAttribute;

/**
 * Provides Granite EL expression resolver for using in HTL scripts
 */
@Model(adaptables = SlingHttpServletRequest.class)
public class ExpressionSupport {

    @SlingObject
    private SlingHttpServletRequest request;

    @RequestAttribute
    private String property;

    @OSGiService
    private ExpressionResolver expressionResolver;

    /**
     * Gets the resolved value of the property specified in the {@code property} request attribute
     * @return The resolved string value
     */
    public String getValue() {
        if (StringUtils.isEmpty(property) || expressionResolver == null) {
            return StringUtils.EMPTY;
        }
        return expressionResolver.resolve(
            request.getResource().getValueMap().get(property, String.class),
            Locale.getDefault(),
            String.class,
            request);
    }
}
