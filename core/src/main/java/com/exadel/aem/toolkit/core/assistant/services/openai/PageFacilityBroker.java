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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.AbstractResourceVisitor;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.day.cq.commons.jcr.JcrConstants;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.assistant.services.AssistantException;

class PageFacilityBroker {
    private static final Logger LOG = LoggerFactory.getLogger(PageFacilityBroker.class);

    private static final String NN_METADATA = "eakAssistantMetadata";
    private static final String PN_METADATA_MEMBER = "eak-assistant-%s-member";
    private static final String NODE_APPS = "/apps/";
    private static final String NODE_CQ_DIALOG = "/cq:dialog";
    private static final String NODE_GRANITE_DATA = "granite:data";

    private static final String KEY_FINDINGS = "findings";
    private static final String KEY_IMAGE = "image";
    private static final String KEY_IMAGE_PROMPT = "imagePrompt";
    private static final String KEY_QUOTE= "quote";
    private static final String KEY_TEXT = "text";
    private static final String KEY_TITLE = "title";
    private static final String KEY_SUBTITLE = "subtitle";
    private static final String KEY_SUMMARY = "summary";

    private static final String IMAGE_SETTINGS_POSTFIX = "_eakAssistantSettings";
    private static final String METADATA_KEY_POSTFIX = "Members";

    private static final Pattern PATH_SPLITTER = Pattern.compile(CoreConstants.SEPARATOR_SLASH);

    private static final String EXCEPTION_COULD_NOT_COMMIT = "Could not commit to \"%s\"";
    private static final String EXCEPTION_COULD_NOT_MODIFY = "Could not retrieve a modifiable resource at \"%s\"";
    private static final String EXCEPTION_INVALID_ADDRESS = "Invalid property address";

    private SlingHttpServletRequest request;
    private Resource pageContentResource;
    private final Map<String, Set<String>> mappings;

    private String detachedSummary;

    private PageFacilityBroker() {
        mappings = new HashMap<>();
    }

    /* ---------------
       Basic accessors
       --------------- */

    public boolean isValid() {
        return pageContentResource != null;
    }

    public SlingHttpServletRequest getRequest() {
        return request;
    }

    public ResourceResolver getResourceResolver() {
        return request.getResourceResolver();
    }

    // Summary

    public String getSummary() {
        if (detachedSummary == null) {
            detachedSummary = readValue(getMembers(KEY_SUMMARY));
        }
        return detachedSummary;
    }

    public void setSummary(String summary) throws AssistantException {
        this.detachedSummary = summary;
        if (summary == null) {
            return;
        }
        Collection<String> summaryMembers = getMembers(KEY_SUMMARY);
        if (CollectionUtils.isEmpty(summaryMembers)) {
            commitMetadata(KEY_SUMMARY, summary);
        } else {
            commitValues(getMembers(KEY_SUMMARY), summary);
        }
    }

    // Titles

    public Set<String> getTitleMembers() {
        return getMembers(KEY_TITLE);
    }

    public void setTitle(String value) throws AssistantException {
        commitValues(getMembers(KEY_TITLE), value);
    }

    public Set<String> getSubtitleMembers() {
        return getMembers(KEY_SUBTITLE);
    }

    public void setSubtitle(String value) throws AssistantException {
        commitValues(getMembers(KEY_SUBTITLE), value);
    }

    // Texts

    public Set<String> getTextMembers() {
        return getMembers(KEY_TEXT);
    }

    public Set<String> getQuoteMembers() {
        return getMembers(KEY_QUOTE);
    }

    public Set<String> getFindingsMembers() {
        return getMembers(KEY_FINDINGS);
    }

    // Images

    public Set<String> getImagePromptMembers() {
        return getMembers(KEY_IMAGE_PROMPT);
    }

    public List<String> getImagePrompts() {
        return getMembers(KEY_IMAGE_PROMPT).stream().map(this::readValue).collect(Collectors.toList());
    }

    public Set<String> getImageMembers() {
        return getMembers(KEY_IMAGE);
    }

    // Common

    private Set<String> getMembers(String key) {
        return mappings.getOrDefault(key, Collections.emptySet());
    }

    /* -------------------
       Metadata operations
       ------------------- */

    private void readMetadata(boolean skipExisting) {
        Resource metadataResource = pageContentResource.getChild(NN_METADATA);
        if (metadataResource != null && !skipExisting) {
            ValueMap metadataValueMap = metadataResource.getValueMap();
            Stream.of(KEY_SUMMARY, KEY_TITLE, KEY_SUBTITLE, KEY_TEXT, KEY_QUOTE, KEY_IMAGE_PROMPT, KEY_IMAGE, KEY_FINDINGS)
                .forEach(key -> putIfNotNull(key, metadataValueMap.get(key + METADATA_KEY_POSTFIX, String[].class)));
            detachedSummary = metadataValueMap.get(KEY_SUMMARY, String.class);
        } else {
            new PageContentVisitor().accept(pageContentResource);
        }
    }

    private void putIfNotNull(String key, String[] value) {
        if (ArrayUtils.isEmpty(value)) {
            return;
        }
        mappings.computeIfAbsent(key, k -> new LinkedHashSet<>()).addAll(Arrays.asList(value));
    }

    private void commitMetadata() throws AssistantException {
        cleanMetadata();
        Map<String, Object> metadataProperties = new HashMap<>();
        if (!mappings.isEmpty()) {
            mappings.forEach((key, value) -> metadataProperties.put(key + METADATA_KEY_POSTFIX, value.toArray(new String[0])));
        }
        if (detachedSummary != null) {
            metadataProperties.put(KEY_SUMMARY, detachedSummary);
        }
        if (!isValid() || metadataProperties.isEmpty()) {
            return;
        }
        metadataProperties.put(JcrConstants.JCR_PRIMARYTYPE, JcrConstants.NT_UNSTRUCTURED);
        try {
            getResourceResolver().create(pageContentResource, NN_METADATA, metadataProperties);
            getResourceResolver().commit();
        } catch (PersistenceException e) {
            throw new AssistantException(e);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void commitMetadata(String key, String value) throws AssistantException {
        if (!isValid()) {
            return;
        }
        ModifiableValueMap modifiableValueMap = Optional.ofNullable(pageContentResource)
            .map(res -> res.getChild(NN_METADATA))
            .map(res -> res.adaptTo(ModifiableValueMap.class))
            .orElse(null);
        if (modifiableValueMap == null) {
            String pageContentResourcePath = Optional.ofNullable(pageContentResource).map(Resource::getPath).orElse(StringUtils.EMPTY);
            throw new AssistantException(String.format(EXCEPTION_COULD_NOT_MODIFY, pageContentResourcePath));
        }
        modifiableValueMap.put(key, value);
        commit();
    }

    public void cleanMetadata() throws AssistantException {
        if (!isValid()) {
            return;
        }
        Resource metadataResource = pageContentResource.getChild(NN_METADATA);
        if (metadataResource == null) {
            return;
        }
        try {
            getResourceResolver().delete(metadataResource);
            getResourceResolver().commit();
        } catch (PersistenceException e) {
            throw new AssistantException(e);
        }
    }

    /* ----------------
       Value operations
       ---------------- */

    private String readValue(Collection<String> addresses) {
        for (String address : addresses) {
            String value = readValue(address);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String readValue(String address) {
        if (!StringUtils.contains(address, CoreConstants.SEPARATOR_SLASH)) {
            return null;
        }
        String nodeAddress = StringUtils.substringBeforeLast(address, CoreConstants.SEPARATOR_SLASH);
        String property = StringUtils.substringAfterLast(address, CoreConstants.SEPARATOR_SLASH);
        Resource resource = getResourceResolver().getResource(nodeAddress);
        if (resource != null) {
            return resource.getValueMap().get(property, String.class);
        }
        return null;
    }

    public void commitValues(Map<String, String> values) throws AssistantException {
        for (Map.Entry<String, String> entry : values.entrySet()) {
            saveValue(entry.getKey(), entry.getValue());
        }
        commit();
    }

    private void commitValues(Collection<String> addresses, String value) throws AssistantException {
        for (String address : addresses) {
            saveValue(address, value);
        }
        commit();
    }

    public void saveValue(String address, String value) throws AssistantException {
        if (StringUtils.isEmpty(address) || !address.contains(CoreConstants.SEPARATOR_SLASH)) {
            LOG.warn(EXCEPTION_INVALID_ADDRESS);
            return;
        }
        String nodeAddress = StringUtils.substringBeforeLast(address, CoreConstants.SEPARATOR_SLASH);
        String property = StringUtils.substringAfterLast(address, CoreConstants.SEPARATOR_SLASH);
        Resource resource = getResourceResolver().getResource(nodeAddress);
        ModifiableValueMap modifiableValueMap = resource != null ? resource.adaptTo(ModifiableValueMap.class) : null;
        if (modifiableValueMap == null) {
            String pageContentResourcePath = Optional.ofNullable(pageContentResource).map(Resource::getPath).orElse(StringUtils.EMPTY);
            throw new AssistantException(String.format(EXCEPTION_COULD_NOT_MODIFY, pageContentResourcePath));
        }
        try {
            modifiableValueMap.put(property, value);
        } catch (RuntimeException e) {
            throw new AssistantException(String.format(EXCEPTION_COULD_NOT_COMMIT, nodeAddress), e);
        }
    }

    private void commit() throws AssistantException {
        try {
            getResourceResolver().commit();
        } catch (PersistenceException e) {
            throw new AssistantException(e);
        }
    }

    /* ---------------
       Factory methods
       --------------- */

    public static PageFacilityBroker getInstance(
        SlingHttpServletRequest request,
        Resource pageResource,
        boolean skipExistingMetadata) throws AssistantException {

        PageFacilityBroker result = new PageFacilityBroker();
        result.request = request;
        result.pageContentResource = pageResource != null ? pageResource.getChild(JcrConstants.JCR_CONTENT) : null;
        result.readMetadata(skipExistingMetadata);
        if (result.pageContentResource.getChild(NN_METADATA) == null || skipExistingMetadata) {
            result.commitMetadata();
        }
        return result;
    }

    /* ------------------
       Subsidiary classes
       ------------------ */

    private class PageContentVisitor extends AbstractResourceVisitor {
        @Override
        protected void visit(@Nonnull Resource resource) {
            String resourceType = resource.getValueMap().get(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, String.class);
            if (StringUtils.isEmpty(resourceType)) {
                return;
            }
            if (!resourceType.startsWith(CoreConstants.SEPARATOR_SLASH)) {
                resourceType = NODE_APPS + resourceType;
            }
            resourceType += NODE_CQ_DIALOG;
            Resource componentDialogResource = getResourceResolver().getResource(resourceType);
            if (componentDialogResource != null) {
                new ComponentDialogVisitor(resource.getPath()).accept(componentDialogResource);
            }
        }
    }

    private class ComponentDialogVisitor extends AbstractResourceVisitor {
        private final List<String> pathChunks;

        public ComponentDialogVisitor(String basePath) {
            pathChunks = Arrays.asList(PATH_SPLITTER.split(StringUtils.strip(basePath, CoreConstants.SEPARATOR_SLASH)));
        }

        @Override
        protected void visit(@Nonnull Resource resource) {
            if (!NODE_GRANITE_DATA.equals(resource.getName())) {
                return;
            }
            Stream.of(KEY_TITLE, KEY_SUBTITLE, KEY_SUMMARY, KEY_TEXT, KEY_QUOTE, KEY_IMAGE, KEY_FINDINGS)
                .forEach(key -> storeMember(resource, key));
        }

        private void storeMember(Resource resource, String key) {
            String value = resource.getValueMap().get(String.format(PN_METADATA_MEMBER, key), String.class);
            if (StringUtils.isEmpty(value)) {
                return;
            }
            mappings.computeIfAbsent(key, k -> new LinkedHashSet<>()).add(resolve(value));
            if (KEY_IMAGE.equals(key)) {
                storeImagePromptMember(resource, value);
            }
        }

        private void storeImagePromptMember(Resource resource, String imageMember) {
            String value = StringUtils.defaultIfEmpty(
                resource.getValueMap().get(String.format(PN_METADATA_MEMBER, KEY_IMAGE_PROMPT), String.class),
                imageMember);
            String finalValue = StringUtils.appendIfMissing(value, IMAGE_SETTINGS_POSTFIX);
            mappings.computeIfAbsent(KEY_IMAGE_PROMPT, k -> new LinkedHashSet<>()).add(resolve(finalValue));
        }

        private String resolve(String value) {
            if (value == null) {
                return null;
            }
            String[] valueChunks = PATH_SPLITTER.split(value);
            Deque<String> result = new LinkedList<>(pathChunks);
            for (String valueChunk : valueChunks) {
                if (StringUtils.isBlank(valueChunk) || CoreConstants.SEPARATOR_DOT.equals(valueChunk)) {
                    continue;
                }
                if (CoreConstants.PATH_PARENT.equals(valueChunk)) {
                    result.removeLast();
                } else {
                    result.add(valueChunk);
                }
            }
            return CoreConstants.SEPARATOR_SLASH + String.join(CoreConstants.SEPARATOR_SLASH, result);
        }
    }
}
