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
package com.exadel.aem.toolkit.core.assistant.services.openai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.assistant.models.facilities.Setting;
import com.exadel.aem.toolkit.core.assistant.models.solutions.Solution;
import com.exadel.aem.toolkit.core.assistant.services.AssistantException;
import com.exadel.aem.toolkit.core.assistant.utils.VersionableValueMap;

class TaggingFacility extends OpenAiFacility {

    private static final int TAG_LENGTH_THRESHOLD = 50;

    private static final Pattern PATTERN_TAG_SPLIT = Pattern.compile("\\n|<br>|,\\s+");
    private static final String PATTERN_NUMBERED_TAG = "^\\d+\\.\\s*";
    private static final String PATTERN_BULLETED_TAG = " -";

    private static final String HASH = "#";

    private static final String PN_PAGE_PATH = "pagePath";

    private static final Map<String, Object> PROPERTIES;

    static {
        PROPERTIES = new HashMap<>();
        PROPERTIES.put("allowEmptyPayload", true);
    }

    public TaggingFacility(OpenAiService service) {
        super(service);
    }

    @Override
    public String getId() {
        return "tags.text.oai";
    }

    @Override
    public String getTitle() {
        return "Create Tags";
    }

    @Override
    public String getIcon() {
        return "tags";
    }

    @Override
    public int getRanking() {
        return 1100;
    }

    @Override
    public Map<String, Object> getProperties() {
        return PROPERTIES;
    }

    @Override
    public List<Setting> getSettings() {
        return COMPLETION_SETTINGS;
    }

    @Override
    public Solution execute(SlingHttpServletRequest request) {
        String pagePath = request.getParameter(PN_PAGE_PATH);
        Resource pageResource = request.getResourceResolver().getResource(pagePath);
        try {
            PageFacilityBroker pageBroker = PageFacilityBroker.getInstance(request, pageResource,true);
            List<ValueMap> tasks = new ArrayList<>();
            for (String textMember : pageBroker.getTextMembers()) {
                String resourceAddress = StringUtils.substringBeforeLast(textMember, CoreConstants.SEPARATOR_SLASH);
                String fieldName = StringUtils.substringAfterLast(textMember, CoreConstants.SEPARATOR_SLASH);
                Resource resource = request.getResourceResolver().getResource(resourceAddress);
                if (resource == null) {
                    continue;
                }
                String text = resource.getValueMap().get(fieldName, String.class);
                ValueMap task = ((VersionableValueMap) getArguments(request))
                    .put(CoreConstants.PN_TEXT, text)
                    .put(CoreConstants.PN_PROMPT, "Create no more than 10 keywords that describe the following text")
                    .put(OpenAiConstants.PN_CHOICES_COUNT, 1)
                    .putIfMissing(OpenAiConstants.PN_MODEL, OpenAiServiceConfig.DEFAULT_COMPLETION_MODEL);
                tasks.add(task);
            }
            List<Solution> solutions = getService().executeCompletion(tasks);
            String keywords = solutions
                .stream()
                .filter(Solution::isSuccess)
                .map(Solution::asText)
                .flatMap(PATTERN_TAG_SPLIT::splitAsStream)
                .map(text -> StringUtils.strip(text, PATTERN_BULLETED_TAG))
                .map(text -> StringUtils.removePattern(text, PATTERN_NUMBERED_TAG))
                .filter(StringUtils::isNotEmpty)
                .filter(text -> text.length() <= TAG_LENGTH_THRESHOLD)
                .distinct()
                .sorted()
                .map(keyword -> !keyword.startsWith(HASH) ? HASH + keyword : keyword)
                .collect(Collectors.joining(StringUtils.SPACE));
            return Solution.from(getArguments(request)).withOptions(Collections.singletonList(keywords));
        } catch (AssistantException e) {
            return Solution.from(getArguments(request)).withMessage(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
