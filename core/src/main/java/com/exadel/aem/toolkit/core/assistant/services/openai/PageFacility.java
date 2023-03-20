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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.assistant.models.solutions.Solution;
import com.exadel.aem.toolkit.core.assistant.services.AssistantException;
import com.exadel.aem.toolkit.core.assistant.services.ImportService;
import com.exadel.aem.toolkit.core.utils.ObjectConversionUtil;
import com.exadel.aem.toolkit.core.utils.ThrowingBiConsumer;

class PageFacility extends OpenAiFacility {
    private static final Logger LOG = LoggerFactory.getLogger(PageFacility.class);

    private static final String PN_NEXT = "next";
    private static final String PN_SOURCE = "source";
    private static final String PN_STAGE = "stage";

    private static final String EXCEPTION_INVALID_PATH = "Invalid page path: %s";
    private static final String EXCEPTION_COULD_NOT_STORE = "Could not save changes to \"%s\": %s";

    private static final Pattern PATTERN_SPLIT_BY_NEWLINE = Pattern.compile("\\n+");
    private static final String PATTERN_LEADING_NUMBER = "^\\d+\\.\\s*";
    private static final String SEPARATOR_NEWLINE = "\n";
    private static final String SEPARATOR_DOT_SPACE = ". ";
    private static final String TERMINATOR_SPACE_QUOTE = " \"";

    private static final int ABSTRACT_WORDS_LIMIT = 20;

    private final ImportService importService;
    private final List<Stage> stages;

    public PageFacility(OpenAiService openAiService, ImportService importService) {
        super(openAiService);
        this.importService = importService;
        stages = Arrays.asList(
            new Stage(
                "summary",
                null,
                null,
                this::createSummary),
            new Stage(
                "titles",
                "Creating title",
                broker -> !broker.getTitleMembers().isEmpty(),
                this::createTitles),
            new Stage(
                "subtitles",
                "Creating subtitle",
                broker -> !broker.getSubtitleMembers().isEmpty(),
                this::createSubtitles),
            new Stage(
                "texts",
                "Creating text",
                broker -> !broker.getTextMembers().isEmpty(),
                this::createTexts),
            new Stage(
                "imageprompts",
                "Creating image descriptions",
                broker -> !broker.getImagePromptMembers().isEmpty(),
                this::createImagePrompts),
            new Stage(
                "images",
                "Creating images",
                broker -> !broker.getImageMembers().isEmpty(),
                this::createImages)
        );
    }

    /* ---------------
       Basic accessors
       --------------- */

    @Override
    public String getId() {
        return "page.create.oai";
    }

    @Override
    public boolean isAllowed(SlingHttpServletRequest request) {
        return request != null
            && CoreConstants.METHOD_POST.equals(request.getMethod());
    }

    /* ---------
       Execution
       --------- */

    @Override
    public Solution execute(SlingHttpServletRequest request) {
        if (StringUtils.isBlank(getService().getConfig().token())) {
            return Solution.from(EXCEPTION_TOKEN_MISSING);
        }
        ValueMap args = getArguments(request);
        String currentPath = decodeSilently(args.get(CoreConstants.PN_PATH, StringUtils.EMPTY));
        String currentPrompt = args.get(CoreConstants.PN_TEXT, String.class);
        Stage currentStage = stages
            .stream()
            .filter(stage -> stage.getId().equals(args.get(PN_STAGE, String.class)))
            .findFirst()
            .orElse(null);

        if (currentStage == null || StringUtils.isAnyBlank(currentPath, currentPrompt)) {
            return Solution.from(args).withMessage(HttpStatus.SC_BAD_REQUEST, EXCEPTION_INVALID_REQUEST);
        }

        try {
            PageFacilityBroker pageBroker = PageFacilityBroker.getInstance(
                request.getResourceResolver(),
                request.getResourceResolver().getResource(currentPath));
            if (!pageBroker.isValid()) {
                String exceptionMessage = String.format(EXCEPTION_INVALID_PATH, currentPath);
                return Solution.from(args).withMessage(HttpStatus.SC_BAD_REQUEST, exceptionMessage);
            }
            currentStage.getProcessing().accept(pageBroker, args);
            return getContinuation(pageBroker, currentStage, args);
        } catch (AssistantException e) {
            String exceptionMessage = String.format(EXCEPTION_COULD_NOT_STORE, currentPath, e.getMessage());
            LOG.error(exceptionMessage, e);
            return Solution.from(args).withMessage(HttpStatus.SC_INTERNAL_SERVER_ERROR, exceptionMessage);
        }
    }

    /* -----------
       Stage logic
       ----------- */

    private Solution getContinuation(PageFacilityBroker pageBroker, Stage currentStage, ValueMap args) {
        if (stages.indexOf(currentStage) == stages.size() - 1) {
            return Solution.empty();
        }

        Stage nextStage = findNextStage(currentStage, pageBroker);
        if (nextStage == null) {
            return Solution.empty();
        }

        Map<String, String> continuation = new HashMap<>();
        continuation.put(PN_STAGE, nextStage.getId());
        continuation.put(CoreConstants.PN_MESSAGE, nextStage.getStatusMessage());
        Map<String, Object> fullResponse = Collections.singletonMap(PN_NEXT, continuation);

        return Solution.from(args).withValueMap(fullResponse);
    }

    private Stage findNextStage(Stage currentStage, PageFacilityBroker pageBroker) {
        int position = stages.indexOf(currentStage);
        for (int i = position + 1; i < stages.size(); i++) {
            Stage next = stages.get(i);
            if (next.getCompliance().test(pageBroker)) {
                return next;
            }
        }
        return null;
    }

    /* --------------
       Stage routines
       -------------- */

    private void createSummary(
        PageFacilityBroker broker,
        ValueMap args) throws AssistantException {

        int phrasesCount = Math.max(broker.getTextMembers().size(), 1);
        ValueMap task = new ArgumentsVersion(args)
            .put(
                CoreConstants.PN_PROMPT,
                String.format("Generate as many as %d ideas for an article on the following topic", phrasesCount))
            .put(OpenAiConstants.PN_CHOICES_COUNT, 1)
            .get();
        Solution solution = getService().executeCompletion(task);

        String solutionText = solution.isSuccess() ? solution.asText() : StringUtils.EMPTY;
        List<String> summaryPoints = PATTERN_SPLIT_BY_NEWLINE.splitAsStream(solutionText)
            .map(String::trim)
            .map(point -> StringUtils.removePattern(point, PATTERN_LEADING_NUMBER))
            .filter(StringUtils::isNotEmpty)
            .collect(Collectors.toList());

        if (!summaryPoints.isEmpty()) {
            broker.setSummary(String.join(SEPARATOR_NEWLINE, summaryPoints));
        } else {
            throw new AssistantException("OpenAI did not produce a valid response");
        }
    }

    private void createTitles(PageFacilityBroker broker, ValueMap args) throws AssistantException {
        String summary = broker.getSummary();
        String normalizedSummary = normalizeSummary(summary);
        ValueMap task = new ArgumentsVersion(args)
            .put(CoreConstants.PN_PROMPT, "Create a title for the following text")
            .put(CoreConstants.PN_TEXT, normalizedSummary)
            .put(OpenAiConstants.PN_CHOICES_COUNT, 1)
            .get();
        Solution solution = getService().executeCompletion(task);
        if (solution.isSuccess()) {
            broker.setTitle(StringUtils.strip(solution.asText(), TERMINATOR_SPACE_QUOTE));
        }
    }

    private void createSubtitles(PageFacilityBroker broker, ValueMap args) throws AssistantException {
        String summary = broker.getSummary();
        String normalizedSummary = normalizeSummary(summary);
        ValueMap task = new ArgumentsVersion(args)
            .put(CoreConstants.PN_PROMPT, "Summarize the following text in " + ABSTRACT_WORDS_LIMIT + " words")
            .put(CoreConstants.PN_TEXT, normalizedSummary)
            .put(OpenAiConstants.PN_CHOICES_COUNT, 1)
            .get();
        Solution solution = getService().executeCompletion(task);
        if (solution.isSuccess()) {
            String text = solution.asText();
            StringTokenizer stringTokenizer = new StringTokenizer(text);
            if (stringTokenizer.countTokens() > ABSTRACT_WORDS_LIMIT * 1.3 && text.contains(SEPARATOR_DOT_SPACE)) {
                text = StringUtils.substringBefore(text, SEPARATOR_DOT_SPACE);
            }
            broker.setSubtitle(text);
        }
    }

    private void createTexts(PageFacilityBroker broker, ValueMap args) throws AssistantException {
        createStringValues(
            broker,
            args,
            ExpandFacility.PROMPT,
            PageFacilityBroker::getTextMembers,
            value -> value);
    }

    private void createImagePrompts(PageFacilityBroker broker, ValueMap args) throws AssistantException {
        createStringValues(
            broker,
            args,
            "Create a prompt for an image generator on the following topic. Must contain up to " + ABSTRACT_WORDS_LIMIT + " words",
            PageFacilityBroker::getImagePromptMembers,
            value -> ObjectConversionUtil.toJson(CoreConstants.PN_PROMPT, value));
    }

    private void createStringValues(
        PageFacilityBroker broker,
        ValueMap args,
        String prompt,
        Function<PageFacilityBroker, Collection<String>> memberCollectionFactory,
        UnaryOperator<String> valueFactory) throws AssistantException {

        List<String> summaryPhrases = getSummaryPhrases(broker.getSummary(), memberCollectionFactory.apply(broker));
        List<ValueMap> tasks = new ArrayList<>();
        Iterator<String> memberIterator = memberCollectionFactory.apply(broker).iterator();
        for (String phrase : summaryPhrases) {
            ValueMap newTask = new ArgumentsVersion(args)
                .put(PN_SOURCE, memberIterator.next())
                .put(CoreConstants.PN_PROMPT, prompt)
                .put(CoreConstants.PN_TEXT, phrase)
                .put(OpenAiConstants.PN_CHOICES_COUNT, 1)
                .get();
            tasks.add(newTask);
        }

        Map<String, String> membersToValues = new HashMap<>();
        Map<String, String> voidedMembers = getMapOfEmptyValues(memberCollectionFactory.apply(broker));

        List<Solution> solutions = getService().executeCompletion(tasks);

        for (Solution solution : solutions) {
            if (!solution.isSuccess()) {
                continue;
            }
            String currentMember = String.valueOf(solution.getArgs().get(PN_SOURCE));
            String currentValue = valueFactory.apply(solution.asText());
            membersToValues.put(currentMember, currentValue);
            voidedMembers.remove(currentMember);
        }
        membersToValues.putAll(voidedMembers);
        broker.commitValues(membersToValues);
    }

    private void createImages(PageFacilityBroker broker, ValueMap args) throws AssistantException {
        List<String> imagePrompts = broker.getImagePrompts();
        Set<String> imageMembers = broker.getImageMembers();

        Iterator<String> imagePromptsIterator = imagePrompts.iterator();
        Iterator<String> imageMembersIterator = imageMembers.iterator();
        List<ValueMap> tasks = new ArrayList<>();
        while (imagePromptsIterator.hasNext() && imageMembersIterator.hasNext()) {
            String prompt = ObjectConversionUtil.toOptionalNodeTree(imagePromptsIterator.next())
                .map(json -> json.get(CoreConstants.PN_PROMPT).asText())
                .orElse(null);
            if (StringUtils.isEmpty(prompt)) {
                continue;
            }
            String imageMember = imageMembersIterator.next();
            ValueMap newTask = new ArgumentsVersion(args)
                .put(PN_SOURCE, imageMember)
                .put(CoreConstants.PN_PROMPT, prompt)
                .put(OpenAiConstants.PN_CHOICES_COUNT, 1)
                .get();
            tasks.add(newTask);
        }

        Map<String, String> membersToDamUrls = new HashMap<>();
        Map<String, String> voidedMembers = getMapOfEmptyValues(imageMembers);

        List<Solution> solutions = getService().executeImageGeneration(tasks);
        Map<String, String> downloadTasks = new HashMap<>();
        for (Solution solution : solutions) {
            if (!solution.isSuccess()) {
                continue;
            }
            String currentUrl = solution.asText();
            String currentMember = String.valueOf(solution.getArgs().get(PN_SOURCE));
            downloadTasks.put(currentUrl, currentMember);
        }
        Map<String, String> urlsToDamAddresses = importService.downloadAssets(broker.getResourceResolver(), downloadTasks);
        for (Map.Entry<String, String> urlToDamAddress : urlsToDamAddresses.entrySet()) {
            String currentMember = downloadTasks.get(urlToDamAddress.getKey());
            membersToDamUrls.put(currentMember, urlToDamAddress.getValue());
            voidedMembers.remove(currentMember);
        }
        membersToDamUrls.putAll(voidedMembers);
        broker.commitValues(membersToDamUrls);
    }

    private static List<String> getSummaryPhrases(String summary, Collection<String> members) {
        List<String> summaryPhrases = PATTERN_SPLIT_BY_NEWLINE
            .splitAsStream(summary)
            .collect(Collectors.toList());
        int limit = Math.min(members.size(), summaryPhrases.size());
        return summaryPhrases.subList(0, limit);
    }


    /* ---------------
       Utility methods
       --------------- */

    private static String decodeSilently(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    private static String normalizeSummary(String value) {
        return PATTERN_SPLIT_BY_NEWLINE
            .splitAsStream(value)
            .map(String::trim)
            .map(line -> StringUtils.appendIfMissing(line, CoreConstants.SEPARATOR_DOT))
            .collect(Collectors.joining(StringUtils.SPACE));    }

    private static Map<String, String> getMapOfEmptyValues(Collection<String> keys) {
        return keys.stream().collect(Collectors.toMap(key -> key, key -> StringUtils.EMPTY));
    }

    /* ------------------
       Subsidiary classes
       ------------------ */

    private static class Stage {
        private final String id;
        private final String statusMessage;
        private final Predicate<PageFacilityBroker> compliance;
        private final ThrowingBiConsumer<PageFacilityBroker, ValueMap, AssistantException> processing;

        public Stage(
            String id,
            String statusMessage,
            Predicate<PageFacilityBroker> compliance,
            ThrowingBiConsumer<PageFacilityBroker, ValueMap, AssistantException> processing) {
            this.id = id;
            this.statusMessage = statusMessage;
            this.compliance = compliance;
            this.processing = processing;
        }

        public String getId() {
            return id;
        }

        public String getStatusMessage() {
            return statusMessage;
        }

        public Predicate<PageFacilityBroker> getCompliance() {
            return compliance;
        }

        public ThrowingBiConsumer<PageFacilityBroker, ValueMap, AssistantException> getProcessing() {
            return processing;
        }
    }
}
