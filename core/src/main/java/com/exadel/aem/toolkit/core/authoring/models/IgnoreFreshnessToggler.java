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

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import com.adobe.granite.ui.components.FormData;

import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.core.CoreConstants;

/**
 * Represents the back-end part of the {@code IgnoreFreshnessToggler} component for Granite UI dialogs. This Sling model
 * is responsible for setting and unsetting the {@code forceIgnoreFreshness} flag to the Sling HTTP request when needed
 * @see Dialog#forceIgnoreFreshness()
 */
@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class IgnoreFreshnessToggler {

    @SlingObject
    private SlingHttpServletRequest request;

    /**
     * Sets or unsets the {@code forceIgnoreFreshness} flag to the Sling HTTP requests upon this Sling model initialization
     */
    @PostConstruct
    private void init() {
        FormData formData = FormData.from(request);
        if (formData == null) {
            return;
        }
        boolean ignoreFreshnessTurnedOn = formData.getValueMap() instanceof RelativePathValueMapDecorator;

        if (!ignoreFreshnessTurnedOn) {
            RelativePathValueMapDecorator syntheticValueMap = new RelativePathValueMapDecorator(formData.getValueMap());
            FormData.push(request, syntheticValueMap, FormData.NameNotFoundMode.IGNORE_FRESHNESS);

        } else {
            FormData.pop(request);
        }
    }

    /**
     * Inherits {@link ValueMapDecorator} to provide a {@code ValueMap} that manages map keys containing the relative
     * path prefix ({@code ./}) in the same way the out-of-the-box {@code JcrValueMap} does
     */
    private static class RelativePathValueMapDecorator extends ValueMapDecorator {

        /**
         * Initializes a new decorator instance
         * @param base {@code Map} containing data for the {@code ValueMap} presentation
         */
        RelativePathValueMapDecorator(Map<String, Object> base) {
            super(base);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> T get(String name, Class<T> type) {
            if (StringUtils.startsWith(name, CoreConstants.PATH_RELATIVE_PREFIX)) {
                return super.get(name.substring(CoreConstants.PATH_RELATIVE_PREFIX.length()), type);
            }
            return super.get(name, type);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean containsKey(Object key) {
            if (key != null && StringUtils.startsWith(key.toString(), CoreConstants.PATH_RELATIVE_PREFIX)) {
                return super.containsKey(key.toString().substring(CoreConstants.PATH_RELATIVE_PREFIX.length()));
            }
            return super.containsKey(key);
        }
    }
}
