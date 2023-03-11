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

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.assistant.models.facilities.SimpleFacility;
import com.exadel.aem.toolkit.core.assistant.models.solutions.Solution;

abstract class SmartSearchFacility extends SimpleFacility {

    private static final Logger LOG = LoggerFactory.getLogger(SmartSearchFacility.class);

    private static final String VARIABLE_PATH = "$path";

    private static final String EXCEPTION_QUERY_FAILED = "Could not complete query";
    private static final String EXCEPTION_QUERY_FAILED_TEMPLATE = EXCEPTION_QUERY_FAILED + " \"{}\"";
    private static final String EXCEPTION_QUERY_MANAGER_MISSING = "Could not obtain a query manager";

    private final SmartSearchService service;

    SmartSearchFacility(SmartSearchService service) {
        this.service = service;
    }

    @Override
    public String getVendorName() {
        return getService().getVendorName();
    }

    @Override
    public Solution execute(SlingHttpServletRequest request) {
        ValueMap args = getArguments(request);
        String text = args.get(CoreConstants.PN_TEXT, String.class);
        if (StringUtils.isBlank(text)) {
            return Solution.from(EXCEPTION_INVALID_REQUEST);
        }
        try {
            QueryResult queryResult = execute(
                request,
                getExpression(),
                getRootPath(),
                text,
                args.get(CoreConstants.PN_OFFSET, 0));
            return processResult(request, queryResult, args);
        } catch (RepositoryException e) {
            LOG.error(EXCEPTION_QUERY_FAILED_TEMPLATE, getExpression(), e);
            return Solution.from(EXCEPTION_QUERY_FAILED);
        }
    }

    private QueryResult execute(
        SlingHttpServletRequest request,
        String expression,
        String path,
        String text,
        int offset) throws RepositoryException {

        Session session = request.getResourceResolver().adaptTo(Session.class);
        Workspace workspace = session != null ? session.getWorkspace() : null;
        QueryManager queryManager = workspace != null ? workspace.getQueryManager() : null;
        if (queryManager == null) {
            throw new RepositoryException(EXCEPTION_QUERY_MANAGER_MISSING);
        }

        String effectiveExpression = StringUtils.replace(expression, VARIABLE_PATH, path);
        Query query = queryManager.createQuery(effectiveExpression, Query.JCR_SQL2);
        query.bindValue(CoreConstants.PN_TEXT, session.getValueFactory().createValue(text));
        query.setOffset(offset);
        query.setLimit(service.getConfig().maxOptions());
        return query.execute();
    }

    SmartSearchService getService() {
        return service;
    }

    abstract String getExpression();

    abstract String getRootPath();

    abstract Solution processResult(
        SlingHttpServletRequest request,
        QueryResult result,
        ValueMap args) throws RepositoryException;

}
