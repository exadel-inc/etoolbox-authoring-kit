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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.apache.sling.api.resource.observation.ResourceChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.relay.utils.PathHelper;

class ScriptSampler {

    private static final Logger LOG = LoggerFactory.getLogger(ScriptSampler.class);

    private static final List<String> SCRIPT_EXTENSIONS = Arrays.asList(".java", ".jsp", ".js", ".html");

    private static final String QUERY_TEMPLATE = "SELECT * FROM [nt:file] AS file WHERE ISDESCENDANTNODE(file, '%s')";

    private final Map<String, String> samples = new HashMap<>();

    public Collection<ResourceChange> generateChanges() {
        return samples.values()
            .stream()
            .map(path -> new ResourceChange(ResourceChange.ChangeType.CHANGED, path, false))
            .collect(Collectors.toList());
    }

    public boolean isEmpty() {
        return samples.isEmpty();
    }

    public static ScriptSampler from(Session session, String source, String target) {
        ScriptSampler result = new ScriptSampler();
        try {
            Query query = session
                .getWorkspace()
                .getQueryManager()
                .createQuery(String.format(QUERY_TEMPLATE, Text.escape(target,  '%', true)), Query.JCR_SQL2);
            QueryResult queryResult = query.execute();
            NodeIterator nodes = queryResult.getNodes();
            while (nodes.hasNext()) {
                String path = nodes.nextNode().getPath();
                if (SCRIPT_EXTENSIONS.stream().anyMatch(path::endsWith)) {
                    result.samples.put(
                        StringUtils.substringAfterLast(path, CoreConstants.SEPARATOR_DOT),
                        PathHelper.replace(path, target, source));
                }
                if (result.samples.size() >= SCRIPT_EXTENSIONS.size()) {
                    break;
                }
            }
        } catch (RepositoryException e) {
            LOG.error("Failed to collect script samples at {}", target, e);
        }
        return result;
    }
}
