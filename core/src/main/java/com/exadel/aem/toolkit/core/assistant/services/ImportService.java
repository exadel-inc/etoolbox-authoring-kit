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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.AssetManager;
import com.day.cq.dam.commons.util.DamUtil;

import com.exadel.aem.toolkit.api.annotations.widgets.imageupload.ImageUploadConstants;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.assistant.models.facilities.Facility;
import com.exadel.aem.toolkit.core.assistant.models.facilities.SimpleFacility;
import com.exadel.aem.toolkit.core.assistant.models.solutions.Solution;
import com.exadel.aem.toolkit.core.utils.ExecutorFactory;
import com.exadel.aem.toolkit.core.utils.HttpClientFactory;
import com.exadel.aem.toolkit.core.utils.ThrowingConsumer;

@Component(service = AssistantService.class, immediate = true)
public class ImportService implements AssistantService {
    private static final Logger LOG = LoggerFactory.getLogger(ImportService.class);

    private static final String QUERY_PARAMETER_FROM = "from";
    private static final String QUERY_PARAMETER_TO = "to";

    private static final int RANDOM_LENGTH = 8;
    private static final int RANDOM_START_CHARCODE = 48;
    private static final int RANDOM_END_CHARCODE = 122;

    private static final String EXCEPTION_COULD_NOT_IMPORT = "Could not import asset from \"%s\" to \"%s\"";
    private static final String EXCEPTION_COULD_NOT_COMPLETE_ASYNC = "Could not complete download";
    private static final String EXCEPTION_INVALID_CONTENT = "Content is missing or invalid";
    private static final String EXCEPTION_MISSING_ASSET_MANAGER = "Could not obtain Asset Manager";
    private static final String EXCEPTION_MISSING_PARAMS = "Either \"from\" or \"to\" parameter is invalid";
    private static final String EXCEPTION_NO_RESPONSE = "Did not get a response";
    private static final String EXCEPTION_TIMEOUT = "Connection to {} timed out";

    private static final String SEPARATOR_COLON = ": ";
    private static final Pattern LOCALHOST_PATTERN = Pattern.compile("//localhost[.:]", Pattern.CASE_INSENSITIVE);

    private List<Facility> facilities;
    private ExecutorService threadPoolExecutor;

    @Activate
    private void init() {
        if (facilities == null) {
            facilities = Arrays.asList(new Import(), new BatchImport());
        }
        threadPoolExecutor = ExecutorFactory.newCachedThreadPoolExecutor();
    }

    @Deactivate
    private void destroy() {
        if (threadPoolExecutor != null) {
            threadPoolExecutor.shutdownNow();
        }
    }

    @Override
    public List<Facility> getFacilities() {
        return facilities;
    }

    @SuppressWarnings("java:S3398") // All "download" methods are kept together for better readability
    private Map<String, Object> downloadAssets(
        ResourceResolver resourceResolver,
        Map<?, ?> targets,
        boolean useCache) throws AssistantException {

        if (MapUtils.isEmpty(targets)) {
            return Collections.emptyMap();
        }
        Map<String, Object> result = new HashMap<>();
        if (targets.size() == 1) {
            Map.Entry<?, ?> nextTarget = targets.entrySet().iterator().next();
            DownloadAssetResult downloadAssetResult = downloadAsset(
                resourceResolver,
                String.valueOf(nextTarget.getKey()),
                String.valueOf(nextTarget.getValue()),
                useCache);
            result.put(String.valueOf(nextTarget.getKey()), downloadAssetResult.getDestination());
            return result;
        }
        List<CompletableFuture<DownloadAssetResult>> tasks = new ArrayList<>();
        for (Map.Entry<?, ?> nextDestination : targets.entrySet()) {
            CompletableFuture<DownloadAssetResult> newTask = downloadAssetAsync(
                resourceResolver,
                String.valueOf(nextDestination.getKey()),
                String.valueOf(nextDestination.getValue()),
                useCache);
            tasks.add(newTask);
        }
        CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();
        for (CompletableFuture<DownloadAssetResult> task : tasks) {
            DownloadAssetResult entry = null;
            try {
                entry = task.get(
                    (long) (HttpClientFactory.DEFAULT_TIMEOUT * HttpClientFactory.DEFAULT_ATTEMPTS_COUNT * 1.1),
                    TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                LOG.warn(EXCEPTION_COULD_NOT_COMPLETE_ASYNC, e);
                Thread.currentThread().interrupt();
            } catch (ExecutionException | TimeoutException e) {
                LOG.error(EXCEPTION_COULD_NOT_COMPLETE_ASYNC);
            }
            if (entry != null) {
                result.put(entry.getSource(), entry.getDestination());
            }
        }
        return result;
    }

    private CompletableFuture<DownloadAssetResult> downloadAssetAsync(
        ResourceResolver resourceResolver,
        String url,
        String destination,
        boolean useCache) {
        return CompletableFuture.supplyAsync(
            () -> {
                try {
                    return downloadAsset(resourceResolver, url, destination, useCache);
                } catch (AssistantException e) {
                    LOG.error(e.getMessage());
                }
                return null;
            },
            threadPoolExecutor);
    }

    private DownloadAssetResult downloadAsset(
        ResourceResolver resourceResolver,
        String url,
        String destination,
        boolean useCache) throws AssistantException {
        String damAddress = getDamAddress(resourceResolver, destination);
        if (useCache) {
            String damAddressParent = StringUtils.substringBeforeLast(damAddress, CoreConstants.SEPARATOR_SLASH);
            DownloadAssetResult cachedResult = getCachedAsset(resourceResolver, url, damAddressParent);
            if (cachedResult != null) {
                return cachedResult;
            }
        }
        try {
            return downloadAsset(url, damAddress, entity -> store(resourceResolver, entity, url, damAddress));
        } catch (IOException e) {
            String exceptionMessage = String.format(EXCEPTION_COULD_NOT_IMPORT, url, destination);
            throw new AssistantException(exceptionMessage, e);
        }
    }

    private DownloadAssetResult downloadAsset(
        String url,
        String destination,
        ThrowingConsumer<HttpEntity, IOException> storage) throws IOException {

        HttpGet request = new HttpGet(url);
        for (int attempt = 0; attempt < HttpClientFactory.DEFAULT_ATTEMPTS_COUNT; attempt++) {
            try (
                CloseableHttpClient client = HttpClientFactory
                    .newClient()
                    .timeout(HttpClientFactory.DEFAULT_TIMEOUT)
                    .skipSsl(LOCALHOST_PATTERN.matcher(url).find())
                    .get();
                CloseableHttpResponse response = client.execute(request)
            ) {
                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    throw new IOException("Unexpected response " + response.getStatusLine().toString());
                }
                HttpEntity entity = response.getEntity();
                storage.accept(entity);
                String mimeType = getMimeType(entity);
                EntityUtils.consume(entity);
                return new DownloadAssetResult(url, mimeType, destination);
            } catch (ConnectTimeoutException | SocketTimeoutException e) {
                LOG.warn(EXCEPTION_TIMEOUT, url);
            }
        }
        throw new IOException(EXCEPTION_NO_RESPONSE);
    }

    private DownloadAssetResult getCachedAsset(ResourceResolver resourceResolver, String url, String folder) {
        Resource parent = resourceResolver.getResource(folder);
        if (parent == null) {
            return null;
        }
        for (Resource nextChild : parent.getChildren()) {
            Resource metadata = nextChild.getChild("jcr:content/metadata");
            if (metadata != null && url.equals(metadata.getValueMap().get("src", String.class))) {
                return new DownloadAssetResult(
                        url,
                        metadata.getValueMap().get("dam:MIMEtype", String.class),
                        nextChild.getPath());
            }
        }
        return null;
    }

    private void store(ResourceResolver resourceResolver, HttpEntity entity, String source, String destination) throws IOException {
        AssetManager assetManager = resourceResolver.adaptTo(AssetManager.class);
        if (assetManager == null) {
            throw new IOException(EXCEPTION_MISSING_ASSET_MANAGER);
        }
        if (entity.getContent() == null) {
            throw new IOException(EXCEPTION_INVALID_CONTENT);
        }
        try {
            if (resourceResolver.getResource(destination) != null) {
                assetManager.removeAssetForBinary(DamUtil.assetToBinaryPath(destination));
            }
            Asset asset = assetManager.createAsset(destination, entity.getContent(), getMimeType(entity), true);
            ModifiableValueMap metadataMap = Optional
                .ofNullable(asset.adaptTo(Resource.class))
                .map(resource -> resource.getChild("jcr:content/metadata"))
                .map(resource -> resource.adaptTo(ModifiableValueMap.class))
                .orElse(null);
            if (metadataMap != null) {
                metadataMap.put("src", source);
                resourceResolver.commit();
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private static String getMimeType(HttpEntity entity) {
        return Optional.ofNullable(entity.getContentType())
            .map(Header::getValue)
            .orElse(ImageUploadConstants.DEFAULT_MIME_TYPE);
    }

    private static String getDamAddress(ResourceResolver resourceResolver, String destination) {
        String decoded = decodeSilently(destination);
        if (StringUtils.isEmpty(decoded) || StringUtils.startsWithIgnoreCase(decoded, "/content/dam/")) {
            return getUniqueDamAddress(resourceResolver, decoded);
        }
        List<String> pathChunks = Pattern.compile(CoreConstants.SEPARATOR_SLASH)
            .splitAsStream(decoded)
            .filter(StringUtils::isNotEmpty)
            .collect(Collectors.toCollection(ArrayList::new));
        if ("content".equals(pathChunks.get(0))) {
            pathChunks.remove(0);
        }
        pathChunks.addAll(0, Arrays.asList("content", "dam", "eak-assistant"));
        return getUniqueDamAddress(
            resourceResolver,
            CoreConstants.SEPARATOR_SLASH + String.join(CoreConstants.SEPARATOR_SLASH, pathChunks));
    }

    private static String getUniqueDamAddress(ResourceResolver resourceResolver, String destination) {
        String unique = destination;
        while (resourceResolver.getResource(unique) != null) {
            String mainPart = Pattern.compile("-\\w+$").matcher(unique).find()
                ? StringUtils.substringBeforeLast(unique, CoreConstants.SEPARATOR_HYPHEN)
                : unique;
            unique = mainPart
                + CoreConstants.SEPARATOR_HYPHEN
                + RandomStringUtils.random(RANDOM_LENGTH, RANDOM_START_CHARCODE, RANDOM_END_CHARCODE, true, true);
        }
        return unique;
    }

    private static String decodeSilently(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    private class Import extends SimpleFacility {

        @Override
        public String getId() {
            return "image.util.import";
        }

        @Override
        public boolean isAllowed(SlingHttpServletRequest request) {
            return request != null && CoreConstants.METHOD_POST.equals(request.getMethod());
        }

        @Override
        public Solution execute(SlingHttpServletRequest request) {
            ValueMap args = getArguments(request);
            String url = args.get(QUERY_PARAMETER_FROM, StringUtils.EMPTY);
            String destination = args.get(QUERY_PARAMETER_TO, StringUtils.EMPTY);
            if (StringUtils.isAnyEmpty(url, destination)) {
                return Solution.from(args).withMessage(HttpStatus.SC_BAD_REQUEST, EXCEPTION_MISSING_PARAMS);
            }
            String damAddress = getDamAddress(request.getResourceResolver(), destination);
            try {
                DownloadAssetResult result = downloadAsset(
                    url,
                    damAddress,
                    entity -> store(request.getResourceResolver(), entity, url, damAddress));
                Map<String, Object> solutionValueMap = new HashMap<>();
                solutionValueMap.put(CoreConstants.PN_TYPE, result.getMimeType());
                solutionValueMap.put(CoreConstants.PN_PATH, damAddress);
                return Solution.from(args).withValueMap(solutionValueMap);
            } catch (IOException e) {
                String exceptionMessage = String.format(EXCEPTION_COULD_NOT_IMPORT, url, destination);
                LOG.error(exceptionMessage, e);
                return Solution
                    .from(args)
                    .withMessage(HttpStatus.SC_INTERNAL_SERVER_ERROR, exceptionMessage + SEPARATOR_COLON + e.getMessage());
            }
        }
    }

    private class BatchImport extends SimpleFacility {

        @Override
        public String getId() {
            return "image.util.batch-import";
        }

        @Override
        public boolean isAllowed(SlingHttpServletRequest request) {
            return request != null && CoreConstants.METHOD_POST.equals(request.getMethod());
        }

        @Override
        public Solution execute(SlingHttpServletRequest request) {
            try {
                Map<String, Object> downloadedResults = downloadAssets(
                    request.getResourceResolver(),
                    (Map<?, ?>) request.getAttribute("targets"),
                    request.getAttribute("cache") instanceof Boolean && (boolean) request.getAttribute("cache"));
                return Solution
                    .from(getArguments(request))
                    .withValueMap(downloadedResults);
            } catch (AssistantException e) {
                LOG.error("Could not implement batch job", e);
            }
            return Solution
                .from(getArguments(request))
                .withValueMap(Collections.emptyMap());
        }
    }

    private static class DownloadAssetResult {
        private final String source;
        private final String mimeType;
        private final String destination;

        public DownloadAssetResult(String source, String mimeType, String destination) {
            this.source = source;
            this.mimeType = mimeType;
            this.destination = destination;
        }

        public String getSource() {
            return source;
        }

        public String getMimeType() {
            return mimeType;
        }

        public String getDestination() {
            return destination;
        }
    }
}