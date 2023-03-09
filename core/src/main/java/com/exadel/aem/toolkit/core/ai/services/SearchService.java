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
package com.exadel.aem.toolkit.core.ai.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
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
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.adobe.granite.omnisearch.api.core.OmniSearchService;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.ai.models.facility.Facility;
import com.exadel.aem.toolkit.core.ai.models.facility.SimpleFacility;
import com.exadel.aem.toolkit.core.ai.models.solution.Solution;

@Component(service = AiService.class, immediate = true, property = "service.ranking:Integer=102")
@Designate(ocd = SearchService.Config.class)
public class SearchService implements AiService {
    private static final Logger LOG = LoggerFactory.getLogger(SearchService.class);

    private static final String VENDOR_NAME = "Smart Search";

    private static final String LOCATION_ASSET = "asset";
    private static final String PREDICATE_PARAMETER_TEXT = "fulltext";
    private static final Map<String, String[]> ASSET_PREDICATE;
    static {
        Map<String, String[]> predicate = new HashMap<>();
        predicate.put("location", toArray(LOCATION_ASSET));
        predicate.put("mainasset", toArray(Boolean.TRUE.toString()));
        predicate.put("property", toArray("jcr:content/metadata/dc:format"));
        predicate.put("property.operation", toArray("like"));
        predicate.put("property.value", toArray("%image%"));
        ASSET_PREDICATE = predicate;
    }

    private int maxOptions;
    private List<Facility> facilities;

    @Activate
    @Modified
    private void init(Config config) {
        maxOptions = config.maxOptions();
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

    private static class FindText extends SimpleFacility {

        private static final Map<String, List<String>> ANSWERS;

        static {
            Map<String, List<String>> answers = new HashMap<>();
            answers.put("what is ai", Arrays.asList(
                "Artificial intelligence is the simulation of human intelligence processes by machines, especially computer systems. Specific applications of AI include expert systems, natural language processing, speech recognition and machine vision.",
                "At its simplest form, artificial intelligence is a field, which combines computer science and robust datasets, to enable problem-solving. It also encompasses sub-fields of machine learning and deep learning, which are frequently mentioned in conjunction with artificial intelligence. These disciplines are comprised of AI algorithms which seek to create expert systems which make predictions or classifications based on input data.",
                "Artificial intelligence (AI) is intelligence—perceiving, synthesizing, and inferring information—demonstrated by machines, as opposed to intelligence displayed by non-human animals and humans. Example tasks in which this is done include speech recognition, computer vision, translation between (natural) languages, as well as other mappings of inputs."
            ));
            answers.put("how does ai work", Arrays.asList(
                "AI works by combining large amounts of data with fast, iterative processing and intelligent algorithms, allowing the software to learn automatically from patterns or features in the data.",
                "In general, AI systems work by ingesting large amounts of labeled training data, analyzing the data for correlations and patterns, and using these patterns to make predictions about future states. In this way, a chatbot that is fed examples of text chats can learn to produce lifelike exchanges with people, or an image recognition tool can learn to identify and describe objects in images by reviewing millions of examples.",
                "Artificial Intelligence is a technology that allows machines and computer applications to mimic human intelligence, learning from experience via iterative processing and algorithmic training. You can think of AI as being a form of intelligence that is used to solve problems, come up with solutions, answer questions, make predictions, or offer strategic suggestions."
            ));
            ANSWERS = answers;
        }

        @Override
        public String getId() {
            return "text.expand.echo";
        }

        @Override
        public String getTitle() {
            return "Expand";
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
            Map<String, Object> args = getArguments(request);
            String text = args.getOrDefault(CoreConstants.PN_TEXT, StringUtils.EMPTY).toString();
            String normalizedText = text.replaceAll("\\s+", " ").replaceAll("(^\\W+)|(\\W+$)", StringUtils.EMPTY).toLowerCase();
            List<String> options = ANSWERS.get(normalizedText);
            return options != null
                ? Solution.from(args).withOptions(options)
                : Solution.from(args).withOptions(Collections.singletonList(text));
        }
    }

    private static String[] toArray(String value) {
        return new String[] {value};
    }

    private class FindImage extends SimpleFacility {

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
            ValueMap args = getArguments(request);
            Map<String, String[]> predicateParameters = new HashMap<>(ASSET_PREDICATE);
            predicateParameters.put(
                PREDICATE_PARAMETER_TEXT,
                toArray(args.get(CoreConstants.PN_TEXT, StringUtils.EMPTY)));
            Map<String, SearchResult> searchResults = omniSearchService.getSearchResults(
                request.getResourceResolver(),
                predicateParameters,
                maxOptions,
                0);
            SearchResult assets = searchResults.get(LOCATION_ASSET);
            List<String> assetPaths = new ArrayList<>();
            for (Hit hit : assets.getHits()) {
                try {
                    assetPaths.add(hit.getPath());
                } catch (RepositoryException e) {
                    LOG.error("Could not retrieve the link to an asset", e);
                }
            }
            return Solution.from(args).withOptions(assetPaths);
        }
    }

    /* --------
       Settings
       -------- */

    @ObjectClassDefinition(name = "EToolbox Authoring Kit - Assistant: Search Service")
    public @interface Config {

        @AttributeDefinition(name = "Max Number of Options")
        int maxOptions() default 10;
    }
}
