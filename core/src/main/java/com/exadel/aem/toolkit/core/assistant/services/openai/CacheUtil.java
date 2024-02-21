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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterators;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.day.cq.commons.jcr.JcrConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.assistant.models.solutions.Solution;
import com.exadel.aem.toolkit.core.utils.ObjectConversionUtil;

class CacheUtil {

    private static final Logger LOG = LoggerFactory.getLogger(CacheUtil.class);

    private static final String CACHE_ADDRESS = "/var/etoolbox-authoring-kit/assistant/openai/cache";

    private static final String PN_SOLUTION = "solution";

    private static final int NODE_NAME_PREFIX_LENGTH = 50;
    private static final int NODE_NAME_LENGTH = 200;
    private static final int NODE_NAME_SUFFIX_LENGTH = 8;
    private static final int RANDOM_START_CHARCODE = 48;
    private static final int RANDOM_END_CHARCODE = 122;

    private static final String PATTERN_NON_ALPHANUMERIC = "\\W+";
    private static final String PATTERN_NON_ALPHANUMERIC_LEADING_TRAILING = "(^\\W+)|(\\W+$)";
    private static final Pattern PATTERN_SPACES = Pattern.compile("\\s+");

    private CacheUtil() {
    }

    static Solution getSolution(ResourceResolver resourceResolver, ValueMap args) {
        Resource solutionNode = findExistingSolutionNode(resourceResolver, args);
        String solutionText = Optional.ofNullable(solutionNode)
            .map(Resource::getValueMap)
            .map(valueMap -> valueMap.get(PN_SOLUTION, String.class))
            .orElse(null);
        if (solutionText == null) {
            return null;
        }
        JsonNode jsonOptionsNode = ObjectConversionUtil.toOptionalNodeTree(solutionText)
            .map(n -> n.get(CoreConstants.PN_OPTIONS))
            .orElse(null);
        if (!(jsonOptionsNode instanceof ArrayNode)) {
            return null;
        }
        List<String> options = StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(jsonOptionsNode.iterator(), 0), false)
            .map(JsonNode::asText)
            .collect(Collectors.toList());
        return Solution.from(args).withOptions(options);
    }

    static void saveSolution(ResourceResolver resourceResolver, Solution value) {
        try {
            Resource solutionNode = findExistingSolutionNode(resourceResolver, value.getArgs());
            if (solutionNode != null) {
                ModifiableValueMap existingValueMap = solutionNode.adaptTo(ModifiableValueMap.class);
                if (existingValueMap == null) {
                    throw new PersistenceException("Could not modify node at " + solutionNode.getPath());
                }
                existingValueMap.put(PN_SOLUTION, ObjectConversionUtil.toJson(value));
                resourceResolver.commit();
                return;
            }
            Resource cacheResource = provideCacheResource(resourceResolver);
            String nodeName = getNodeName(value.getArgs());
            String randomSuffix = CoreConstants.SEPARATOR_HYPHEN + RandomStringUtils.random(
                NODE_NAME_SUFFIX_LENGTH,
                RANDOM_START_CHARCODE,
                RANDOM_END_CHARCODE,
                true,
                true);
            while (cacheResource.getChild(nodeName + randomSuffix) != null) {
                randomSuffix = CoreConstants.SEPARATOR_HYPHEN + RandomStringUtils.random(
                    NODE_NAME_SUFFIX_LENGTH,
                    RANDOM_START_CHARCODE,
                    RANDOM_END_CHARCODE,
                    true,
                    true);
            }
            Map<String, Object> newValueMap = new HashMap<>();
            newValueMap.put(JcrConstants.JCR_PRIMARYTYPE, JcrConstants.NT_UNSTRUCTURED);
            newValueMap.put("cmd", value.getArgs().get("cmd"));
            if (value.getArgs().get("prompt") != null) {
                newValueMap.put("prompt", value.getArgs().get("prompt"));
            }
            if (value.getArgs().get("text") != null) {
                newValueMap.put("text", value.getArgs().get("text"));
            }
            newValueMap.put(PN_SOLUTION, ObjectConversionUtil.toJson(value));
            resourceResolver.create(cacheResource, nodeName + randomSuffix, newValueMap);
            resourceResolver.commit();

        } catch (PersistenceException e) {
            LOG.error("Could not store solution in cache", e);
        }
    }

    private static Resource findExistingSolutionNode(
        ResourceResolver resourceResolver,
        Map<String, Object> args) {

        Resource cacheResource = resourceResolver.getResource(CACHE_ADDRESS);
        if (cacheResource == null) {
            return null;
        }
        Object cmd = args.get(CoreConstants.PN_COMMAND);
        Object stage = args.get(OpenAiConstants.PN_STAGE);
        Object prompt = args.get(CoreConstants.PN_PROMPT);
        Object text = args.get(CoreConstants.PN_TEXT);

        for (Resource child : cacheResource.getChildren()) {
            ValueMap valueMap = child.getValueMap();
            String storedCmd = valueMap.get(CoreConstants.PN_COMMAND, String.class);
            String storedStage =  valueMap.get(OpenAiConstants.PN_STAGE, StringUtils.EMPTY);
            String storedPrompt = valueMap.get(CoreConstants.PN_PROMPT, StringUtils.EMPTY);
            String storedText = valueMap.get(CoreConstants.PN_TEXT, StringUtils.EMPTY);
            if (stringsEqual(cmd, storedCmd)
                && stringsEqual(stage, storedStage)
                && stringsEqual(prompt, storedPrompt)
                && stringsEqual(text, storedText)) {
                return child;
            }
        }
        return null;
    }

    private static Resource provideCacheResource(ResourceResolver resourceResolver) throws PersistenceException {
        Resource cacheResource = resourceResolver.getResource(CACHE_ADDRESS);
        if (cacheResource != null) {
            return cacheResource;
        }
        String[] pathChunks = StringUtils.split(CACHE_ADDRESS, CoreConstants.SEPARATOR_SLASH);
        cacheResource = resourceResolver.getResource(CoreConstants.SEPARATOR_SLASH + pathChunks[0]);
        assert cacheResource != null;
        for (int i = 1; i < pathChunks.length; i++) {
            if (cacheResource.getChild(pathChunks[i]) == null) {
                cacheResource = resourceResolver.create(
                    cacheResource,
                    pathChunks[i],
                    Collections.singletonMap(JcrConstants.JCR_PRIMARYTYPE, JcrConstants.NT_UNSTRUCTURED));
            }
        }
        resourceResolver.commit();
        return cacheResource;
    }

    private static String getNodeName(Map<String, Object> args) {
        String prompt = args.getOrDefault(CoreConstants.PN_PROMPT, StringUtils.EMPTY).toString().trim();
        String text = args.getOrDefault(CoreConstants.PN_TEXT, StringUtils.EMPTY).toString().trim();
        String commandPart = args
            .getOrDefault(CoreConstants.PN_COMMAND, StringUtils.EMPTY)
            .toString()
            .trim()
            .replaceAll(PATTERN_NON_ALPHANUMERIC, CoreConstants.SEPARATOR_HYPHEN);
        if (!commandPart.isEmpty()) {
            commandPart += CoreConstants.SEPARATOR_HYPHEN;
        }
        String promptPart = getNodeNamePart(prompt, NODE_NAME_PREFIX_LENGTH);
        if (!promptPart.isEmpty()) {
            promptPart += CoreConstants.SEPARATOR_HYPHEN;
        }
        String textPart = getNodeNamePart(text, NODE_NAME_LENGTH - commandPart.length() - promptPart.length());
        return StringUtils.strip(commandPart + promptPart + textPart, CoreConstants.SEPARATOR_HYPHEN);
    }

    private static String getNodeNamePart(String source, int limit) {
        if (StringUtils.isEmpty(source)) {
            return StringUtils.EMPTY;
        }
        StringBuilder result = new StringBuilder();
        List<String> chunks = PATTERN_SPACES
            .splitAsStream(source)
            .collect(Collectors.toList());
        for (String chunk : chunks) {
            String normalized = chunk
                .replaceAll(PATTERN_NON_ALPHANUMERIC_LEADING_TRAILING, StringUtils.EMPTY)
                .replaceAll(PATTERN_NON_ALPHANUMERIC, CoreConstants.SEPARATOR_HYPHEN);
            result.append(normalized).append(CoreConstants.SEPARATOR_HYPHEN);
            if (result.length() > limit) {
                break;
            }
        }
        return result.toString();
    }

    private static boolean stringsEqual(Object left, Object right) {
        String stringifiedLeft = left != null ? left.toString() : StringUtils.EMPTY;
        String stringifiedRight = right != null ? right.toString() : StringUtils.EMPTY;
        return StringUtils.equals(stringifiedLeft, stringifiedRight);
    }
}
