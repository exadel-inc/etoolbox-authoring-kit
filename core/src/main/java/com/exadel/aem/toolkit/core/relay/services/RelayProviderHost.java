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
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.observation.ResourceChangeListener;
import org.apache.sling.spi.resource.provider.ResourceProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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
            Set<Mapping> pathMappings = new HashSet<>();
            Set<Mapping> userMappings = new HashSet<>();
            for (String mappingSource : config.pathMappings()) {
                Mapping mapping = ObjectConversionUtil.toObject(mappingSource, Mapping.class);
                if  (Mapping.isValid(mapping)) {
                    pathMappings.add(mapping);
                }
            }
            if (pathMappings.isEmpty()) {
                return;
            }
            for (String mappingSource : config.userMappings()) {
                Mapping mapping = ObjectConversionUtil.toObject(mappingSource, Mapping.class);
                if (Mapping.isValid(mapping)) {
                    userMappings.add(mapping);
                }
            }
            createRelays(
                context,
                pathMappings,
                userMappings,
                ArrayUtils.nullToEmpty(config.reportedPaths()));
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
     * @param context       OSGi {@link BundleContext} used to register provider services
     * @param pathMappings  Collection of path mapping rules defining source-to-target path redirections
     * @param userMappings  Collection of user mapping rules defining source-to-target user identity redirections
     * @param reportedPaths Array of JCR paths to report as changed when a provider starts or stops
     */
    private void createRelays(
        BundleContext context,
        Collection<Mapping> pathMappings,
        Collection<Mapping> userMappings,
        String[] reportedPaths) {

        for (Mapping pathMapping : pathMappings) {

            RelayProvider.Builder builder = RelayProvider
                .builder()
                .resolverFactory(resolverFactory)
                .source(pathMapping.getFrom())
                .target(pathMapping.getFrom());
            userMappings.forEach(mapping -> builder.userMapping(mapping.getFrom(), mapping.getTo()));
            Arrays.stream(reportedPaths).forEach(builder::reportedPath);
            RelayProvider provider = builder.build();

            Dictionary<String, Object> properties = new Hashtable<>();
            properties.put(ResourceProvider.PROPERTY_ROOT, pathMapping.getFrom());
            properties.put(ResourceChangeListener.PATHS, new String[]{pathMapping.getFrom()});

            ServiceRegistration<?> registration = context.registerService(
                new String[]{ResourceProvider.class.getName(), ResourceChangeListener.class.getName()},
                provider,
                properties);
            registrations.add(registration);
        }
    }

    /**
     * Represents a source-to-target mapping entry used for path or user identity translation
     */
    private static class Mapping {
        private final String from;
        private final String to;

        /**
         * Creates a new {@code Mapping} with the provided source and target values
         * @param from Source value
         * @param to   Target value
         */
        @JsonCreator
        Mapping(@JsonProperty("from") String from, @JsonProperty("to") String to) {
            this.from = from;
            this.to = to;
        }

        /**
         * Gets the source value of this mapping
         * @return A nullable source string
         */
        String getFrom() {
            return from;
        }

        /**
         * Gets the target value of this mapping
         * @return A nullable target string
         */
        String getTo() {
            return to;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final boolean equals(Object other) {
            if (!(other instanceof Mapping)) {
                return false;
            }
            Mapping mapping = (Mapping) other;
            return Objects.equals(from, mapping.from) && Objects.equals(to, mapping.to);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            int result = Objects.hashCode(from);
            result = 31 * result + Objects.hashCode(to);
            return result;
        }

        /**
         * Gets whether the provided mapping is non-null and has non-blank source and target values
         * @param value Nullable {@code Mapping} instance to validate
         * @return True or false
         */
        static boolean isValid(Mapping value) {
            return value != null && StringUtils.isNoneBlank(value.getFrom(), value.getTo());
        }
    }
}

