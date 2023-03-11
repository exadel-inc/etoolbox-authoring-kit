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
import java.util.List;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ValueMap;

import com.exadel.aem.toolkit.core.assistant.models.solutions.Solution;

class SearchImagesFacility extends SmartSearchFacility {

    private static final String QUERY_EXPRESSION = "SELECT [jcr:path] FROM [dam:Asset] AS a "
        + "WHERE ISDESCENDANTNODE(a, '$path') AND [jcr:content/metadata/dc:format] LIKE '%image%' "
        + "AND CONTAINS(*, $text) "
        + "ORDER BY [jcr:score] DESC";

    SearchImagesFacility(SmartSearchService service) {
        super(service);
    }

    @Override
    public String getId() {
        return "image.produce.search";
    }

    @Override
    public String getTitle() {
        return "Search Image";
    }

    @Override
    public String getIcon() {
        return ICON_IMAGE_ADD;
    }

    @Override
    public int getRanking() {
        return 1001;
    }

    @Override
    String getExpression() {
        return QUERY_EXPRESSION;
    }

    @Override
    String getRootPath() {
        return getService().getConfig().imagesRoot();
    }

    @Override
    Solution processResult(
        SlingHttpServletRequest request,
        QueryResult value,
        ValueMap args) throws RepositoryException {

        List<String> assetPaths = new ArrayList<>();
        RowIterator rowIterator = value.getRows();
        while (rowIterator.hasNext()) {
            assetPaths.add(rowIterator.nextRow().getPath());
        }
        return Solution.from(args).withOptions(assetPaths);
    }
}
