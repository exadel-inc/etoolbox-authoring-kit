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

import javax.annotation.PostConstruct;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import com.adobe.granite.ui.components.FormData;

import com.exadel.aem.toolkit.api.annotations.main.Dialog;

/**
 * Represents the back-end part of the {@code IgnoreFreshnessToggler} component for Granite UI dialogs. This Sling model
 * is responsible for setting and unsetting the {@code forceIgnoreFreshness} flag to the Sling HTTP request, as needed
 * @see Dialog#forceIgnoreFreshness()
 */
@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class IgnoreFreshnessToggler {

    private static final String ATTRIBUTE_IGNORE_FRESHNESS = "toolkit.ignoreFreshness";

    @SlingObject
    private SlingHttpServletRequest request;

    @SlingObject
    private Resource resource;

    /**
     * Sets or unsets the {@code forceIgnoreFreshness} flag to the Sling HTTP request upon this Sling model initialization
     */
    @PostConstruct
    private void init() {
        if (isIgnoreFreshnessTurnedOn()) {
            FormData.push(request, resource.getValueMap(), FormData.NameNotFoundMode.CHECK_FRESHNESS);
            request.setAttribute(ATTRIBUTE_IGNORE_FRESHNESS, Boolean.FALSE.toString());
        } else {
            FormData.push(request, resource.getValueMap(), FormData.NameNotFoundMode.IGNORE_FRESHNESS);
            request.setAttribute(ATTRIBUTE_IGNORE_FRESHNESS, Boolean.TRUE.toString());
        }
    }

    /**
     * Retrieves whether the {@code forceIgnoreFreshness} flag has been set for the current Sling HTTP request
     * @return True or false
     */
    private boolean isIgnoreFreshnessTurnedOn() {
        Object attributeValue = request.getAttribute(ATTRIBUTE_IGNORE_FRESHNESS);
        return attributeValue != null && Boolean.parseBoolean(attributeValue.toString());
    }
}
