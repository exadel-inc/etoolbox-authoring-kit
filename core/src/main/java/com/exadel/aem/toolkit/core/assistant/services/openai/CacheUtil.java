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
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterators;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

    private static final String PN_REQUEST_ARGS = "requestArgs";
    private static final String PN_SOLUTION = "solution";

    private static final int NODE_NAME_PREFIX_LENGTH = 50;
    private static final int NODE_NAME_LENGTH = 200;
    private static final int NODE_NAME_SUFFIX_LENGTH = 8;
    private static final int RANDOM_START_CHARCODE = 48;
    private static final int RANDOM_END_CHARCODE = 122;

    private static final String PATTERN_NON_ALPHANUMERIC = "\\W+";
    private static final String PATTERN_NON_ALPHANUMERIC_LEADING_TRAILING = "(^\\W+)|(\\W+$)";

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
            newValueMap.put(PN_REQUEST_ARGS, ObjectConversionUtil.toJson(value.getArgs()));
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
        String argsText = ObjectConversionUtil.toJson(args);
        if (cacheResource == null || argsText == null) {
            return null;
        }
        for (Resource child : cacheResource.getChildren()) {
            String childArgsText = child.getValueMap().get(PN_REQUEST_ARGS, String.class);

            if (StringUtils.equals(argsText, childArgsText)) {
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
        String instruction = Stream.of(
            args.get(CoreConstants.PN_PROMPT),
            args.get(OpenAiConstants.PN_INSTRUCTION))
            .filter(Objects::nonNull)
            .map(Object::toString)
            .map(String::trim)
            .findFirst()
            .orElse(StringUtils.EMPTY);
        String text = args.getOrDefault(CoreConstants.PN_TEXT, StringUtils.EMPTY).toString().trim();
        String commandPart = args
            .getOrDefault(CoreConstants.PN_COMMAND, StringUtils.EMPTY)
            .toString()
            .trim()
            .replaceAll(PATTERN_NON_ALPHANUMERIC, CoreConstants.SEPARATOR_HYPHEN);
        if (!commandPart.isEmpty()) {
            commandPart += CoreConstants.SEPARATOR_HYPHEN;
        }
        String instructionPart = getNodeNamePart(instruction, NODE_NAME_PREFIX_LENGTH);
        if (!instructionPart.isEmpty()) {
            instructionPart += CoreConstants.SEPARATOR_HYPHEN;
        }
        String textPart = getNodeNamePart(text, NODE_NAME_LENGTH - commandPart.length() - instructionPart.length());
        return StringUtils.strip(commandPart + instructionPart + textPart, CoreConstants.SEPARATOR_HYPHEN);
    }

    private static String getNodeNamePart(String source, int limit) {
        if (StringUtils.isEmpty(source)) {
            return StringUtils.EMPTY;
        }
        StringBuilder result = new StringBuilder();
        List<String> chunks = Pattern.compile("\\s+")
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
}
