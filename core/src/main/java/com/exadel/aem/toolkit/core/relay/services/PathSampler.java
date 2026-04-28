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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.core.CoreConstants;

/**
 * Resolves a list of JCR paths or XPath expressions to concrete JCR paths and produces
 * {@link ResourceChange} notifications for use during relay provider start and stop
 */
class PathSampler {

    private static final Logger LOG = LoggerFactory.getLogger(PathSampler.class);

    private static final Pattern LIMIT_PREDICATE = Pattern.compile("\\s+limit\\s+(\\d+)$", Pattern.CASE_INSENSITIVE);

    private final List<String> samples;
    private Collection<String> paths;

    /**
     * Creates a new {@code PathSampler} with the provided list of path samples
     * @param samples Nullable list of JCR paths or XPath expressions
     */
    PathSampler(List<String> samples) {
        this.samples = samples;
    }

    /**
     * Resolves the configured path samples against the provided JCR session and produces a collection of
     * {@link ResourceChange} notifications
     * @param session JCR {@link Session} used to execute XPath queries
     * @return A non-null, possibly empty collection of {@link ResourceChange} instances
     */
    Collection<ResourceChange> createChanges(Session session) {
        if (paths == null) {
            paths = CollectionUtils.emptyIfNull(samples)
                .stream()
                .flatMap(s -> resolve(s, session).stream())
                .collect(Collectors.toSet());
        }
        return createChanges();
    }

    /**
     * Creates a collection of {@link ResourceChange} notifications from the previously resolved paths
     * @return A non-null, possibly empty collection of {@link ResourceChange} instances
     */
    Collection<ResourceChange> createChanges() {
        return CollectionUtils.emptyIfNull(paths)
            .stream()
            .map(path -> new ResourceChange(ResourceChange.ChangeType.CHANGED, path, false))
            .collect(Collectors.toList());
    }

    /**
     * Gets whether this sampler has no configured path samples
     * @return True or false
     */
    public boolean isEmpty() {
        return CollectionUtils.isEmpty(samples);
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
    private static boolean isXPath(String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        return StringUtils.startsWithAny(value.trim(), "/jcr:root", "//")
            || StringUtils.containsAny(value, CoreConstants.ARRAY_OPENING, CoreConstants.SEPARATOR_AT, "(*");
    }

    /**
     * Resolves the provided path or XPath expression to a list of concrete JCR paths
     * @param pathOrExpression JCR path or XPath expression to resolve
     * @param session          JCR {@link Session} used for XPath query execution
     * @return A non-null, possibly empty list of resolved JCR path strings
     */
    private static List<String> resolve(String pathOrExpression, Session session) {
        if (isXPath(pathOrExpression)) {
            return resolveXPath(pathOrExpression, session);
        } else if (isJcrPath(pathOrExpression)) {
            return Collections.singletonList(pathOrExpression);
        }
        return Collections.emptyList();
    }

    /**
     * Executes the provided XPath expression against the given session and collects the resulting JCR paths.
     * Supports an optional {@code limit N} suffix to cap the number of results
     * @param expression XPath expression to execute
     * @param session    JCR {@link Session} used for query execution
     * @return A non-null, possibly empty list of JCR path strings matching the expression
     */
    private static List<String> resolveXPath(String expression, Session session) {
        String effectiveExpression = expression.trim();
        Matcher matcher = LIMIT_PREDICATE.matcher(effectiveExpression);
        int limit = -1;
        if (matcher.find()) {
            limit = Integer.parseInt(matcher.group(1));
            effectiveExpression = effectiveExpression.replace(matcher.group(), StringUtils.EMPTY).trim();
        }
        try {
            @SuppressWarnings("deprecation")
            Query query = session.getWorkspace().getQueryManager().createQuery(effectiveExpression, Query.XPATH);
            if (limit > 0) {
                query.setLimit(limit);
            }
            QueryResult queryResult = query.execute();
            List<String> result = new ArrayList<>();
            RowIterator rowIterator = queryResult.getRows();
            while (rowIterator.hasNext()) {
                Row next = rowIterator.nextRow();
                result.add(next.getPath());
            }
            return result;
        } catch (RepositoryException | NullPointerException e) {
            LOG.error("Failed to parse the path sample expression {}", expression, e);
            return Collections.emptyList();
        }
    }
}
