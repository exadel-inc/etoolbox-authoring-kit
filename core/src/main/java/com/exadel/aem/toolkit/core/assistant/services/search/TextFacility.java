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
package com.exadel.aem.toolkit.core.assistant.services.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import com.exadel.aem.toolkit.core.assistant.models.solutions.Solution;

class TextFacility extends SmartSearchFacility {

    private static final String QUERY_TEMPLATE = "SELECT * FROM [nt:unstructured] AS a "
        + "WHERE ISDESCENDANTNODE(a, '$path') "
        + "AND (%s) AND (%s) ORDER BY [jcr:score] DESC";

    private static final String QUERY_CONTAINS_TEMPLATE = "CONTAINS(a.[%s], $text)";
    private static final String QUERY_NOT_EMPTY_TEMPLATE = "a.[%s] <> ''";
    private static final String OPERATOR_OR = " OR ";

    private final String queryExpression;

    public TextFacility(SmartSearchService service) {
        super(service);
        String containsConstraint = ArrayUtils.isNotEmpty(service.getConfig().textSearchFields())
            ? Arrays.stream(service.getConfig().textSearchFields())
                .map(item -> String.format(QUERY_CONTAINS_TEMPLATE, item))
                .collect(Collectors.joining(OPERATOR_OR))
            : StringUtils.EMPTY;
        String notEmptyConstraint = ArrayUtils.isNotEmpty(service.getConfig().textOutputFields())
            ? Arrays.stream(service.getConfig().textOutputFields())
                .map(item -> String.format(QUERY_NOT_EMPTY_TEMPLATE, item))
                .collect(Collectors.joining(OPERATOR_OR))
            : StringUtils.EMPTY;
        queryExpression = String.format(QUERY_TEMPLATE, containsConstraint, notEmptyConstraint);
    }

    @Override
    public String getId() {
        return "text.expand.search";
    }

    @Override
    public String getTitle() {
        return "Search Text";
    }

    @Override
    public String getIcon() {
        return ICON_TEXT_ADD;
    }

    @Override
    String getExpression() {
        return queryExpression;
    }

    @Override
    String getRootPath() {
        return getService().getConfig().textsRoot();
    }

    @Override
    Solution processResult(
        SlingHttpServletRequest request,
        QueryResult result,
        ValueMap args) throws RepositoryException {

        List<String> options = new ArrayList<>();
        RowIterator rowIterator = result.getRows();
        while (rowIterator.hasNext()) {
            Resource resource = request.getResourceResolver().getResource(rowIterator.nextRow().getPath());
            if (resource == null) {
                continue;
            }
            ValueMap valueMap = resource.getValueMap();
            Arrays.stream(getService().getConfig().textOutputFields())
                .map(field -> valueMap.get(field, String.class))
                .filter(StringUtils::isNotBlank)
                .findFirst()
                .ifPresent(options::add);
        }
        return Solution.from(args).withOptions(options, options.size() >= getService().getConfig().maxOptions());
    }
}
