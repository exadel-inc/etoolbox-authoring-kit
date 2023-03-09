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
package com.exadel.aem.toolkit.core.assistant.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.adobe.granite.omnisearch.api.core.OmniSearchService;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.assistant.models.facilities.Facility;
import com.exadel.aem.toolkit.core.assistant.models.facilities.SimpleFacility;
import com.exadel.aem.toolkit.core.assistant.models.solutions.Solution;

@Component(service = AssistantService.class, immediate = true, property = "service.ranking:Integer=102")
@Designate(ocd = SmartSearchService.Config.class)
public class SmartSearchService implements AssistantService {
    private static final Logger LOG = LoggerFactory.getLogger(SmartSearchService.class);

    private static final String VENDOR_NAME = "Smart Search";

    private static final String PREDICATE_PARAMETER_TEXT = "fulltext";
    private static final String PREDICATE_PARAMETER_LOCATION = "location";

    private static final String LOCATION_ASSET = "asset";
    private static final String LOCATION_SITE = "site";

    private static final String PN_TITLE = "title";
    private static final String PN_DESCRIPTION = "description";

    private int maxOptions;
    private String textRoot;
    private List<Facility> facilities;

    @Activate
    @Modified
    private void init(Config config) {
        maxOptions = config.maxOptions();
        textRoot = config.textRoot();
        facilities = Arrays.asList(new FindText(), new FindImage());
    }

    @Reference
    private OmniSearchService omniSearchService;

    @Override
    public String getVendorName() {
        return VENDOR_NAME;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public List<Facility> getFacilities() {
        return facilities;
    }

    private abstract class Find extends SimpleFacility {
        SearchResult execute(SlingHttpServletRequest request, String location) {
            Map<String, SearchResult> searchResults = omniSearchService.getSearchResults(
                request.getResourceResolver(),
                getPredicateParameters(request, location),
                maxOptions,
                0);
            return searchResults.get(location);
        }

        Map<String, String[]> getPredicateParameters(SlingHttpServletRequest request, String location) {
            ValueMap args = getArguments(request);
            Map<String, String[]> predicateParameters = new HashMap<>();
            predicateParameters.put(
                PREDICATE_PARAMETER_LOCATION,
                toArray(location));
            predicateParameters.put(
                PREDICATE_PARAMETER_TEXT,
                toArray(args.get(CoreConstants.PN_TEXT, StringUtils.EMPTY)));
            return predicateParameters;
        }
    }

    private class FindText extends Find {

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
        public String getVendorName() {
            return VENDOR_NAME;
        }

        @Override
        public Solution execute(SlingHttpServletRequest request) {
            SearchResult searchResult = execute(request, LOCATION_SITE);
            List<String> textHits = new ArrayList<>();
            for (Hit hit : searchResult.getHits()) {
                try {
                    String path = hit.getPath();
                    Resource resource = request.getResourceResolver().getResource(path);
                    if (resource != null && !resource.getPath().endsWith(JcrConstants.JCR_CONTENT)) {
                        resource = resource.getChild(JcrConstants.JCR_CONTENT);
                    }
                    if (resource != null) {
                        ValueMap valueMap = resource.getValueMap();
                        Stream.of(
                                valueMap.get(JcrConstants.JCR_DESCRIPTION, String.class),
                                valueMap.get(PN_DESCRIPTION, String.class),
                                valueMap.get(JcrConstants.JCR_TITLE, String.class),
                                valueMap.get(PN_TITLE, String.class))
                            .filter(text -> StringUtils.length(text) > 50)
                            .findFirst()
                            .ifPresent(textHits::add);
                    }
                } catch (RepositoryException e) {
                    LOG.warn("Could not retrieve text from the resource", e);
                }
            }
            return Solution.from(getArguments(request)).withOptions(textHits);
        }

        @Override
        Map<String, String[]> getPredicateParameters(SlingHttpServletRequest request, String location) {
            Map<String, String[]> predicateParameters = super.getPredicateParameters(request, location);
            if (StringUtils.isNotBlank(textRoot)) {
                predicateParameters.put(CoreConstants.PN_PATH, toArray(textRoot));
            }
            return predicateParameters;
        }
    }

    private static String[] toArray(String value) {
        return new String[] {value};
    }

    private class FindImage extends Find {

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
        public String getVendorName() {
            return VENDOR_NAME;
        }

        @Override
        public int getRanking() {
            return 1001;
        }

        @Override
        public Solution execute(SlingHttpServletRequest request) {
            SearchResult searchResult = execute(request, LOCATION_ASSET);
            List<String> assetPaths = new ArrayList<>();
            for (Hit hit : searchResult.getHits()) {
                try {
                    assetPaths.add(hit.getPath());
                } catch (RepositoryException e) {
                    LOG.warn("Could not retrieve the link to an asset", e);
                }
            }
            return Solution.from(getArguments(request)).withOptions(assetPaths);
        }

        @Override
        Map<String, String[]> getPredicateParameters(SlingHttpServletRequest request, String location) {
            Map<String, String[]> predicateParameters = super.getPredicateParameters(request, location);
            predicateParameters.put("mainasset", toArray(Boolean.TRUE.toString()));
            predicateParameters.put("property", toArray("jcr:content/metadata/dc:format"));
            predicateParameters.put("property.operation", toArray("like"));
            predicateParameters.put("property.value", toArray("%image%"));
            return predicateParameters;
        }
    }

    /* --------
       Settings
       -------- */

    @ObjectClassDefinition(name = "EToolbox Authoring Kit - Assistant: SmartSearch Service")
    public @interface Config {

        @AttributeDefinition(name = "Text Search Root")
        String textRoot() default "";

        @AttributeDefinition(name = "Max Number of Options")
        int maxOptions() default 10;
    }
}
