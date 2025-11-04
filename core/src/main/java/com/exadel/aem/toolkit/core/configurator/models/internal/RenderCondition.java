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
package com.exadel.aem.toolkit.core.configurator.models.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adobe.granite.ui.components.rendercondition.SimpleRenderCondition;

import com.exadel.aem.toolkit.core.configurator.utils.PermissionUtil;
import com.exadel.aem.toolkit.core.configurator.utils.RequestUtil;

/**
 * Provides render conditions for the Configurator interface components based on the current user's permissions and the
 * request context
 * <p><u>Note</u>: This class is not a part of the public API and is subject to change. Do not use it in your own
 */
@Model(adaptables = SlingHttpServletRequest.class)
public class RenderCondition {

    private static final Logger LOG = LoggerFactory.getLogger(RenderCondition.class);

    private static final Map<String, Predicate<SlingHttpServletRequest>> FEATURE_PREDICATES;

    static {
        FEATURE_PREDICATES = new HashMap<>();

        FEATURE_PREDICATES.put(
            "eak.configurator.canBrowse",
            req -> {
                ConfigAccess configAccess = ConfigAccess.from(req);
                boolean configIsAvailable = configAccess.isGranted()
                    && ConfigDefinition.from(req).isValid();
                return req.getRequestURI().contains("/etoolbox/config")
                    && (configIsAvailable || configAccess == ConfigAccess.INVALID_CONFIG);
            }
        );

        FEATURE_PREDICATES.put(
            "eak.configurator.canModify",
            req -> ConfigAccess.from(req).isGranted()
                && ConfigDefinition.from(req).isValid()
        );

        FEATURE_PREDICATES.put(
            "eak.configurator.canReplicate",
            req -> !req.getRequestURI().contains("/etoolbox/localsettings")
                && FEATURE_PREDICATES.get("eak.configurator.canModify").test(req)
                && PermissionUtil.hasReplicatePermission(req)
        );

        FEATURE_PREDICATES.put(
            "eak.configurator.showForm",
            req -> ConfigAccess.from(req).isGranted()
                && !StringUtils.isEmpty(RequestUtil.getConfigPid(req))
        );

        FEATURE_PREDICATES.put(
            "eak.configurator.showList",
            req -> ConfigAccess.from(req).isGranted()
                && StringUtils.isEmpty(RequestUtil.getConfigPid(req))
        );
    }

    @SlingObject
    private SlingHttpServletRequest request;

    @ValueMapValue
    private String feature;

    /**
     * Initializes the render condition and attaches it to the request attributes
     */
    @PostConstruct
    private void init() {
        Predicate<SlingHttpServletRequest> predicate = FEATURE_PREDICATES.get(feature);
        if (predicate != null) {
            request.setAttribute(
                com.adobe.granite.ui.components.rendercondition.RenderCondition.class.getName(),
                new SimpleRenderCondition(predicate.test(request)));
        } else {
            LOG.warn("Unknown feature: {}", feature);
        }
    }
}
