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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.relay.models.ChangeSample;
import com.exadel.aem.toolkit.core.relay.utils.RelayPathHelper;
import com.exadel.aem.toolkit.core.utils.ResolverUtil;

/**
 * Resolves a list of JCR paths or XPath expressions to concrete JCR paths and produces
 * {@link ResourceChange} notifications for use during relay provider start and stop
 */
class PathSampler {

    private static final Logger LOG = LoggerFactory.getLogger(PathSampler.class);

    private static final String PROPERTY_USER_ID = "userId";

    private ResourceResolverFactory resolverFactory;
    private Collection<ChangeSample> samples;
    private String source;
    private String target;

    /**
     * Default (instantiation-restricting) constructor
     */
    private PathSampler() {
    }

    /**
     * Resolves the configured path samples and produces a collection of {@link ResourceChange} notifications
     * @return A non-null, possibly empty collection of {@code ResourceChange} instances
     */
    Collection<ResourceChange> createChanges() {
        Set<String> paths = new HashSet<>();

        ResourceResolver resolver = null;
        try {
            for (ChangeSample sample : CollectionUtils.emptyIfNull(samples)) {
                if (isXpath(sample.getPath())) {
                    resolver = rotateResolver(resolver, sample.getUser());
                    if (resolver == null) {
                        // Exception is already logged
                        continue;
                    }
                    Session session = resolver.adaptTo(Session.class);
                    if (session == null) {
                        LOG.error("Failed to adapt a session from the resolver for user {}", resolver.getUserID());
                        continue;
                    }
                    paths.addAll(resolveXpath(sample, session));
                } else if (isJcrPath(sample.getPath())) {
                    paths.add(sample.getPath());
                }
            }
        } finally {
            if (resolver != null) {
                resolver.close();
            }
        }
        // All resolved paths must point to source instead of target
        return paths
            .stream()
            .map(p -> RelayPathHelper.isSubpath(p, target) ?  RelayPathHelper.replace(p, target, source) : p)
            .map(path -> new ResourceChange(ResourceChange.ChangeType.CHANGED, path, false))
            .collect(Collectors.toList());
    }

    /**
     * Gets whether the provided value represents a plain JCR path
     * @param value Arbitrary string to check
     * @return True or false
     */
    private static boolean isJcrPath(String value) {
        return StringUtils.startsWith(value, CoreConstants.SEPARATOR_SLASH);
    }

    /**
     * Gets whether the provided value represents an XPath expression
     * @param value Arbitrary string to check
     * @return True or false
     */
    private static boolean isXpath(String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        return StringUtils.startsWithAny(value.trim(), "/jcr:root", "//")
            || StringUtils.containsAny(value, CoreConstants.ARRAY_OPENING, CoreConstants.SEPARATOR_AT, "(*");
    }

    /**
     * Executes the provided XPath expression against the given session and collects the resulting JCR paths
     * @param sample  {@link ChangeSample} instance containing the XPath expression to execute and the optional limit of
     *                results
     * @param session JCR {@link Session} used for query execution
     * @return A non-null, possibly empty list of JCR path strings matching the expression
     */
    private static List<String> resolveXpath(ChangeSample sample, Session session) {
        try {
            @SuppressWarnings("deprecation")
            Query query = session.getWorkspace().getQueryManager().createQuery(sample.getPath(), Query.XPATH);
            if (sample.getLimit() > 0) {
                query.setLimit(sample.getLimit());
            }
            QueryResult queryResult = query.execute();
            List<String> result = new ArrayList<>();
            RowIterator rowIterator = queryResult.getRows();
            while (rowIterator.hasNext()) {
                Row next = rowIterator.nextRow();
                result.add(next.getPath());
            }
            return result;
        } catch (RepositoryException e) {
            LOG.error("Failed to execute the XPath expression {}", sample.getPath(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Called by {@link #createChanges()} to obtain a {@link ResourceResolver} for the provided user ID, closing the
     * preexisting resolver if the associated user ID differs
     * @param existing Existing resolver, or {@code null}
     * @param userId   User ID to obtain a resolver for; might be {@code null} or empty for the default resolver
     * @return A {@code ResourceResolver} instance for the provided user ID, a pre-existent resolver if still valid for
     * the current user, or {@code null} if the resolver cannot be created
     */
    private ResourceResolver rotateResolver(ResourceResolver existing, String userId) {
        if (existing != null
            && StringUtils.isNotEmpty(userId)
            && userId.equals(existing.getPropertyMap().get(PROPERTY_USER_ID))) {
            return existing;
        }
        try {
            ResourceResolver newResolver = ResolverUtil.newResolver(resolverFactory, userId);
            newResolver.getPropertyMap().put(PROPERTY_USER_ID, userId);
            if (existing != null) {
                existing.close();
            }
            return newResolver;
        } catch (LoginException e) {
            LOG.error("Failed to create a resource resolver for {}", userId, e);
            return null;
        }
    }

    /* -------
       Builder
       ------- */

    /**
     * Creates a new {@link Builder} for instantiating a {@link PathSampler}
     * @return A new {@code Builder} instance
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * Constructs {@link PathSampler} instances with the required configuration
     */
    @SuppressWarnings({"UnusedReturnValue"})
    static class Builder {

        private ResourceResolverFactory resolverFactory;
        private String source;
        private String target;
        private Collection<ChangeSample> samples;

        /**
         * Default (instantiation-restricting) constructor
         */
        private Builder() {
        }

        /**
         * Sets the {@link ResourceResolverFactory} used to create user-mapped resolvers
         * @param value {@code ResourceResolverFactory} instance
         * @return This builder
         */
        Builder resolverFactory(ResourceResolverFactory value) {
            this.resolverFactory = value;
            return this;
        }

        /**
         * Sets the collection of path samples to resolve and report as changed when the relay is enabled or disabled
         * @param value Collection of {@link ChangeSample} instances
         * @return This builder
         */
        Builder samples(Collection<ChangeSample> value) {
            samples = value;
            return this;
        }

        /**
         * Sets the JCR source path prefix to be replaced in the resolved paths
         * @param value Source JCR path prefix
         * @return This builder
         */
        Builder source(String value) {
            this.source = value;
            return this;
        }

        /**
         * Sets the JCR target path prefix to replace the source prefix with in the resolved paths
         * @param value Target JCR path prefix
         * @return This builder
         */
        Builder target(String value) {
            this.target = value;
            return this;
        }

        /**
         * Creates a configured {@link PathSampler} from the current builder state
         * @return A new {@link PathSampler} instance
         */
        PathSampler build() {
            PathSampler result = new PathSampler();
            result.resolverFactory = resolverFactory;
            result.source = source;
            result.target = target;
            result.samples = samples;
            return result;
        }
    }
}
