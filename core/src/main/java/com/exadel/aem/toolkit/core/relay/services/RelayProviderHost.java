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

import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.observation.ExternalResourceChangeListener;
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
import com.exadel.aem.toolkit.core.relay.models.RelayInfo;
import com.exadel.aem.toolkit.core.relay.models.RelayMapping;
import com.exadel.aem.toolkit.core.relay.utils.RelayPathHelper;
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

    private final Map<ServiceRegistration<?>, RelayProvider> registrations = new HashMap<>();

    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Called by the OSGi container when this component is activated or its configuration is updated to register
     * {@link RelayProvider} instances for each valid path mapping
     * @param context OSGi {@link BundleContext} used to register provider services
     * @param config  {@link RelayConfig} instance containing the current configuration
     */
    @Activate
    private void activate(BundleContext context, RelayConfig config) {
        List<RelayInfo> relays = parseConfig(config);
        lock.lock();
        try {
            Iterator<ServiceRegistration<?>> iterator = registrations.keySet().iterator();
            while (iterator.hasNext()) {
                ServiceRegistration<?> registration = iterator.next();
                String existingRoot = (String) registration
                    .getReference()
                    .getProperty(ResourceProvider.PROPERTY_ROOT);
                RelayInfo matchingRelay = relays.stream()
                    .filter(relay -> StringUtils.equals(relay.getSource(), existingRoot))
                    .findFirst()
                    .orElse(null);
                boolean shouldUnregister = false;
                if (matchingRelay != null) {
                    String[] existingObservedPaths = (String[]) registration.getReference().getProperty(ResourceChangeListener.PATHS);
                    String existingTarget = ArrayUtils.isNotEmpty(existingObservedPaths) ? existingObservedPaths[0] : null;
                    if (StringUtils.equals(existingTarget, matchingRelay.getTarget())) {
                        RelayProvider provider = registrations.get(registration);
                        provider.announce();
                        provider.update(matchingRelay);
                        provider.announce();
                        relays.remove(matchingRelay);
                    } else {
                        shouldUnregister = true;
                    }
                } else {
                    shouldUnregister = true;
                }
                if (shouldUnregister) {
                    try {
                        registration.unregister();
                        iterator.remove();
                    } catch (IllegalStateException e) {
                        LOG.warn("Could not unregister relay provider for path {}", existingRoot, e);
                    }
                }
            }
            if (relays.isEmpty()) {
                return;
            }

            // Register providers for the remaining new relays that didn't match any existing ones
            Map<String, String> providedPaths = getAlreadyProvidedPaths(context);
            for (RelayInfo relay : relays) {
                String shadowedEntries = providedPaths.entrySet().stream()
                    .filter(e -> RelayPathHelper.isSamePathOrSubpath(e.getKey(), relay.getSource()))
                    .map(e -> e.getKey() + " by " + e.getValue())
                    .collect(Collectors.joining(CoreConstants.SEPARATOR_COMMA + StringUtils.SPACE));
                if (!shadowedEntries.isEmpty()) {
                    LOG.warn(
                        "Skipping relay registration for path {} to avoid shadowing {}",
                        relay.getSource(),
                        shadowedEntries);
                    continue;
                }
                RelayProvider provider = new RelayProvider(resolverFactory, relay);
                Dictionary<String, Object> properties = new Hashtable<>();
                properties.put(ResourceProvider.PROPERTY_ROOT, relay.getSource());
                properties.put(ResourceChangeListener.PATHS, new String[]{relay.getTarget()});
                ServiceRegistration<?> registration = context.registerService(
                    new String[]{
                        ResourceProvider.class.getName(),
                        ResourceChangeListener.class.getName(),
                        ExternalResourceChangeListener.class.getName()},
                    provider,
                    properties);
                registrations.put(registration, provider);
                // Add newly registered path to providedPaths to prevent overlap with remaining relays
                providedPaths.put(StringUtils.stripEnd(relay.getSource(), CoreConstants.SEPARATOR_SLASH), "this config");
            }
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
            Iterator<ServiceRegistration<?>> iterator = registrations.keySet().iterator();
            while (iterator.hasNext()) {
                ServiceRegistration<?> registration = iterator.next();
                try {
                    registration.unregister();
                    iterator.remove();
                } catch (IllegalStateException e) {
                    LOG.warn("Could not unregister relay provider", e);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Parses the given {@link RelayConfig} instance to create a list of {@link RelayInfo} models for valid path mappings
     * @param config A {@code RelayConfig} instance; expected to be non-null
     * @return A list of {@code Relay} models; might be empty but never null
     */
    private static List<RelayInfo> parseConfig(RelayConfig config) {
        if (!config.enabled()) {
            return Collections.emptyList();
        }

        Set<RelayMapping> pathMappings = new LinkedHashSet<>();
        for (String mappingSource : ArrayUtils.nullToEmpty(config.pathMappings())) {
            RelayMapping mapping = ObjectConversionUtil.toObject(mappingSource, RelayMapping.class);
            if  (mapping != null
                && mapping.isValid()
                && StringUtils.startsWith(mapping.getFrom(), CoreConstants.SEPARATOR_SLASH)
                && StringUtils.startsWith(mapping.getTo(), CoreConstants.SEPARATOR_SLASH)) {
                if (pathMappings.contains(mapping)) {
                    LOG.warn("Skipping duplicate path mapping with source {} and target {}", mapping.getFrom(), mapping.getTo());
                    continue;
                }
                pathMappings.add(mapping);
            }
        }
        if (pathMappings.isEmpty()) {
            return Collections.emptyList();
        }

        Set<RelayMapping> userMappings = new HashSet<>();
        for (String mappingSource : ArrayUtils.nullToEmpty(config.userMappings())) {
            RelayMapping mapping = ObjectConversionUtil.toObject(mappingSource, RelayMapping.class);
            if (mapping != null && mapping.isValid()) {
                if (userMappings.contains(mapping)) {
                    LOG.warn("Skipping duplicate user mapping with source {} and target {}", mapping.getFrom(), mapping.getTo());
                    continue;
                }
                userMappings.add(mapping);
            }
        }

        Set<ChangeSample> samples = new HashSet<>();
        for (String sampleSource : ArrayUtils.nullToEmpty(config.announcedPaths())) {
            ChangeSample changeSample = ObjectConversionUtil.toObject(sampleSource, ChangeSample.class);
            if (changeSample != null && StringUtils.isNotBlank(changeSample.getPath())) {
                samples.add(changeSample);
            }
        }

        return pathMappings.stream()
            .sorted(Comparator.comparingInt(m -> m.getFrom().length()))
            .map(m -> new RelayInfo(m, userMappings, samples))
            .collect(Collectors.toList());
    }

    /**
     * Collects the JCR paths already provided by already registered {@link ResourceProvider} services to avoid
     * "shadowing" external providers
     * @param context OSGi {@link BundleContext} used to query registered provider services
     * @return A map of JCR path-to-provider name entries; might be empty but never null
     */
    private static Map<String, String> getAlreadyProvidedPaths(BundleContext context) {
        Map<String, String> result = new HashMap<>();
        ServiceReference<?>[] serviceReferences = null;
        try {
            serviceReferences = context.getServiceReferences(ResourceProvider.class.getName(), null);
        } catch (InvalidSyntaxException e) {
            LOG.warn("Could not collect info on predefined resource providers", e);
        }
        if (serviceReferences == null) {
            return result;
        }
        for (ServiceReference<?> ref : serviceReferences) {
            Object providedPath = ref.getProperty(ResourceProvider.PROPERTY_ROOT);
            Object providerId = ref.getProperty(ResourceProvider.PROPERTY_NAME);
            if (providerId == null || StringUtils.isEmpty(providerId.toString())) {
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

