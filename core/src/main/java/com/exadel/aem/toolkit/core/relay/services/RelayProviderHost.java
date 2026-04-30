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
package com.exadel.aem.toolkit.core.relay.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.observation.ResourceChangeListener;
import org.apache.sling.spi.resource.provider.ResourceProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.relay.models.ChangeSample;
import com.exadel.aem.toolkit.core.relay.models.RelayMapping;
import com.exadel.aem.toolkit.core.relay.utils.PathHelper;
import com.exadel.aem.toolkit.core.utils.ObjectConversionUtil;

/**
 * Handles {@link RelayConfig} factory configurations to create and register {@link RelayProvider} instances at the
 * configured source paths.
 * <p><u>Note</u>: This class is not a part of the public API and is subject to change. Do not use it in your own
 * code</p>
 */
@Component(immediate = true, service = RelayProviderHost.class)
@Designate(ocd = RelayConfig.class, factory = true)
public class RelayProviderHost {

    private static final Logger LOG = LoggerFactory.getLogger(RelayProviderHost.class);

    @Reference
    private transient ResourceResolverFactory resolverFactory;

    private final List<ServiceRegistration<?>> registrations = new ArrayList<>();

    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Called by the OSGi container when this component is activated or its configuration is updated to register
     * {@link RelayProvider} instances for each valid path mapping
     * @param context OSGi {@link BundleContext} used to register provider services
     * @param config  {@link RelayConfig} instance containing the current configuration
     */
    @Activate
    private void activate(BundleContext context, RelayConfig config) {
        lock.lock();
        try {
            deactivate();
            if (!config.enabled()) {
                return;
            }

            Set<RelayMapping> pathMappings = new HashSet<>();
            for (String mappingSource : config.pathMappings()) {
                RelayMapping mapping = ObjectConversionUtil.toObject(mappingSource, RelayMapping.class);
                if  (mapping != null && mapping.isValid()) {
                    pathMappings.add(mapping);
                }
            }
            if (pathMappings.isEmpty()) {
                return;
            }

            Set<RelayMapping> userMappings = new HashSet<>();
            for (String mappingSource : config.userMappings()) {
                RelayMapping mapping = ObjectConversionUtil.toObject(mappingSource, RelayMapping.class);
                if (mapping != null && mapping.isValid()) {
                    userMappings.add(mapping);
                }
            }

            Set<ChangeSample> samples = new HashSet<>();
            for (String sampleSource : config.announcedPaths()) {
                ChangeSample announcement = ObjectConversionUtil.toObject(sampleSource, ChangeSample.class);
                if (announcement != null && StringUtils.isNotBlank(announcement.getPath())) {
                    samples.add(announcement);
                }
            }

            createRelays(
                context,
                pathMappings,
                userMappings,
                samples);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Discards all previously registered {@link RelayProvider} service instances
     */
    @Deactivate
    private void deactivate() {
        lock.lock();
        try {
            for (ServiceRegistration<?> registration : registrations) {
                registration.unregister();
            }
            registrations.clear();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Creates and registers a {@link RelayProvider} service instance for each entry in the provided path mappings
     * collection
     * @param context      OSGi {@link BundleContext} used to register provider services
     * @param pathMappings Collection of path mapping rules defining source-to-target path redirections
     * @param userMappings Collection of user mapping rules defining source-to-target user identity redirections
     * @param samples      Collection of rules defining JCR paths or XPath expressions to report as changed when the
     *                     relay is enabled or disabled
     */
    private void createRelays(
        BundleContext context,
        Collection<RelayMapping> pathMappings,
        Collection<RelayMapping> userMappings,
        Collection<ChangeSample> samples) {

        Map<String, String> providedPaths = getExternallyProvidedPaths(context);

        for (RelayMapping pathMapping : pathMappings) {
            String from = pathMapping.getFrom();
            String shadowedEntries = providedPaths.entrySet().stream()
                .filter(e -> PathHelper.isSubpath(e.getKey(), from))
                .map(e -> e.getKey() + " by " + e.getValue())
                .collect(Collectors.joining(CoreConstants.SEPARATOR_COMMA + StringUtils.SPACE));
            if (!shadowedEntries.isEmpty()) {
                LOG.warn(
                    "Skipping relay registration for path {} to avoid shadowing {}",
                    from,
                    shadowedEntries);
                continue;
            }
            RelayProvider provider = RelayProvider
                .builder()
                .resolverFactory(resolverFactory)
                .source(pathMapping.getFrom())
                .target(pathMapping.getTo())
                .userMappings(userMappings)
                .samples(samples)
                .build();

            Dictionary<String, Object> properties = new Hashtable<>();
            properties.put(ResourceProvider.PROPERTY_ROOT, pathMapping.getFrom());
            properties.put(ResourceChangeListener.PATHS, new String[]{pathMapping.getTo()});

            ServiceRegistration<?> registration = context.registerService(
                new String[]{ResourceProvider.class.getName(), ResourceChangeListener.class.getName()},
                provider,
                properties);
            registrations.add(registration);
        }
    }

    /**
     * Collects the JCR paths already provided by registered {@link ResourceProvider} services to avoid "shadowing"
     * external providers
     * @param context OSGi {@link BundleContext} used to query registered provider services
     * @return A map of JCR path-to-provider name entries; might be empty but never null
     */
    private static Map<String, String> getExternallyProvidedPaths(BundleContext context) {
        ServiceReference<?>[] serviceReferences = null;
        try {
            serviceReferences = context.getServiceReferences(ResourceProvider.class.getName(), null);
        } catch (InvalidSyntaxException e) {
            LOG.warn("Could not collect info on predefined resource providers", e);
        }
        if (serviceReferences == null) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>();
        for (ServiceReference<?> ref : serviceReferences) {
            if (ref.toString().startsWith(CoreConstants.ROOT_PACKAGE)) {
                continue;
            }
            Object providedPath = ref.getProperty(ResourceProvider.PROPERTY_ROOT);
            Object providerId = ref.getProperty(ResourceProvider.PROPERTY_NAME);
            if (providerId == null || providerId.toString().isEmpty()) {
                 providerId = ref.getBundle().getSymbolicName();
            }
            String providedPathString = providedPath instanceof String ? (String) providedPath : null;
            if (StringUtils.isNotEmpty(providedPathString)) {
                result.put(StringUtils.stripEnd(providedPathString, CoreConstants.SEPARATOR_SLASH), providerId.toString());
            }
        }
        return result;
    }
}

