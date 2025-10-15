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
package com.exadel.aem.toolkit.core.configurator.servlets;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.featureflags.ExecutionContext;
import org.apache.sling.featureflags.Feature;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import com.exadel.aem.toolkit.core.configurator.models.ConfigAccess;
import com.exadel.aem.toolkit.core.configurator.models.ConfigDefinition;
import com.exadel.aem.toolkit.core.configurator.utils.PermissionUtil;
import com.exadel.aem.toolkit.core.configurator.utils.RequestUtil;

/**
 * Provides feature flag services that determine whether certain UI elements of the {@code EToolbox Configurator}
 * are to be rendered in the current request context
 */
@Component(service = RenderFeatures.class, immediate = true)
public class RenderFeatures {

    private final List<ServiceRegistration<Feature>> registrations = new ArrayList<>();
    private BundleContext context;

    /**
     * Processes OSGi component activation by registering feature flag services
     * @param context The current bundle context
     */
    @Activate
    private void activate(BundleContext context) {
        this.context = context;
        addFeature(
            "eak.configurator.canBrowse",
            ec -> ec.getRequest() != null
                && ec.getRequest().getRequestURI().contains("/etoolbox/config.html")
                && ConfigAccess.from(ec.getRequest()).isGranted()
                && ConfigDefinition.from(ec.getRequest()).isValid()
        );
        addFeature(
            "eak.configurator.canModify",
            ec -> ec.getRequest() != null
                && ConfigAccess.from(ec.getRequest()).isGranted()
                && ConfigDefinition.from(ec.getRequest()).isValid()
        );
        addFeature(
            "eak.configurator.canReplicate",
            ec -> ec.getRequest() != null
                && !ec.getRequest().getRequestURI().contains("/etoolbox/localsettings.html")
                && ec.getFeatures().isEnabled("eak.configurator.canModify")
                && PermissionUtil.hasReplicatePermission(ec.getRequest())
        );
        addFeature(
            "eak.configurator.showForm",
            ec -> ConfigAccess.from(ec.getRequest()).isGranted()
                && !StringUtils.isEmpty(RequestUtil.getConfigPid(ec.getRequest()))
        );
        addFeature(
            "eak.configurator.showList",
            ec -> ConfigAccess.from(ec.getRequest()).isGranted()
                && StringUtils.isEmpty(RequestUtil.getConfigPid(ec.getRequest()))
        );
    }

    /**
     * Processes OSGi component deactivation by unregistering feature flag services
     */
    @Deactivate
    private void deactivate() {
        registrations.forEach(ServiceRegistration::unregister);
        registrations.clear();
    }

    /**
     * Creates and registers a feature flag service with the specified name and enabling predicate
     * @param name      The name of the feature flag
     * @param predicate The routine that determines whether the feature is enabled in the current execution context
     */
    private void addFeature(
        String name,
        Predicate<ExecutionContext> predicate) {
        registrations.add(context.registerService(Feature.class, newFeature(name, predicate), null));
    }

    /**
     * Creates a feature flag instance with the specified name and enabling predicate
     * @param name      The name of the feature flag
     * @param predicate The routine that determines whether the feature is enabled in the current execution context
     * @return The feature flag instance
     */
    private static Feature newFeature(String name, Predicate<ExecutionContext> predicate) {
        return new Feature() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getDescription() {
                return StringUtils.EMPTY;
            }

            @Override
            public boolean isEnabled(ExecutionContext executionContext) {
                return predicate.test(executionContext);
            }
        };
    }
}
