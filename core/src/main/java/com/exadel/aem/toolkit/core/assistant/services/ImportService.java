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
import java.util.AbstractMap;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.day.cq.dam.api.AssetManager;
import com.day.cq.dam.api.renditions.RenditionMaker;
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

    private static final String EXCEPTION_COULD_NOT_IMPORT = "Could not import asset from \"%s\" to \"%s\"";
    private static final String EXCEPTION_COULD_NOT_COMPLETE_ASYNC = "Could not complete download";
    private static final String EXCEPTION_INVALID_CONTENT = "Content is missing or invalid";
    private static final String EXCEPTION_MISSING_ASSET_MANAGER = "Could not obtain Asset Manager";
    private static final String EXCEPTION_MISSING_PARAMS = "Either \"from\" or \"to\" parameter is invalid";
    private static final String EXCEPTION_NO_RESPONSE = "Did not get response";
    private static final String EXCEPTION_TIMEOUT = "Connection to {} timed out";

    private static final String SEPARATOR_COLON = ": ";
    private static final Pattern LOCALHOST_PATTERN = Pattern.compile("//localhost[.:]", Pattern.CASE_INSENSITIVE);

    @Reference
    private RenditionMaker renditionMaker;

    private List<Facility> facilities;
    private ExecutorService threadPoolExecutor;

    @Activate
    private void init() {
        if (facilities == null) {
            facilities = Collections.singletonList(new Import());
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

    public Map<String, String> downloadAssets(ResourceResolver resourceResolver, Map<String, String> destinations) throws AssistantException {
        if (MapUtils.isEmpty(destinations)) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>();
        if (destinations.size() == 1) {
            Map.Entry<String, String> nextDestination = destinations.entrySet().iterator().next();
            result.put(
                nextDestination.getKey(),
                downloadAsset(resourceResolver, nextDestination.getKey(), nextDestination.getValue()));
            return result;
        }
        List<CompletableFuture<Map.Entry<String, String>>> tasks = new ArrayList<>();
        for (Map.Entry<String, String> nextDestination : destinations.entrySet()) {
            tasks.add(downloadAssetAsync(resourceResolver, nextDestination.getKey(), nextDestination.getValue()));
        }
        CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();
        for (CompletableFuture<Map.Entry<String, String>> task : tasks) {
            Map.Entry<String, String> entry = null;
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
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    private CompletableFuture<Map.Entry<String, String>> downloadAssetAsync(
        ResourceResolver resourceResolver,
        String url,
        String destination) {
        return CompletableFuture.supplyAsync(
            () -> {
                try {
                    String dest = downloadAsset(resourceResolver, url, destination);
                    return new AbstractMap.SimpleEntry<>(url, dest);
                } catch (AssistantException e) {
                    LOG.error(e.getMessage());
                }
                return null;
            },
            threadPoolExecutor);
    }

    public String downloadAsset(ResourceResolver resourceResolver, String url, String destination) throws AssistantException {
        String damUrl = toDamUrl(destination);
        try {
            downloadAsset(url, entity -> store(resourceResolver, entity, damUrl));
            return damUrl;
        } catch (IOException e) {
            String exceptionMessage = String.format(EXCEPTION_COULD_NOT_IMPORT, url, destination);
            throw new AssistantException(exceptionMessage, e);
        }
    }

    private Map<String, Object> downloadAsset(String url, ThrowingConsumer<HttpEntity, IOException> storage) throws IOException {
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
                Map<String, Object> result = new HashMap<>();
                result.put(CoreConstants.PN_TYPE, getMimeType(entity));
                EntityUtils.consume(entity);
                return result;
            } catch (ConnectTimeoutException | SocketTimeoutException e) {
                LOG.warn(EXCEPTION_TIMEOUT, url);
            }
        }
        throw new IOException(EXCEPTION_NO_RESPONSE);
    }

    private void store(ResourceResolver resourceResolver, HttpEntity entity, String destination) throws IOException {
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
            assetManager.createAsset(destination, entity.getContent(), getMimeType(entity), true);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private static String getMimeType(HttpEntity entity) {
        return Optional.ofNullable(entity.getContentType())
            .map(Header::getValue)
            .orElse(ImageUploadConstants.DEFAULT_MIME_TYPE);
    }

    private static String toDamUrl(String destination) {
        if (StringUtils.isEmpty(destination) || StringUtils.startsWithIgnoreCase(destination, "/content/dam/")) {
            return decodeSilently(destination);
        }
        List<String> pathChunks = Pattern.compile(CoreConstants.SEPARATOR_SLASH)
            .splitAsStream(decodeSilently(destination))
            .filter(StringUtils::isNotEmpty)
            .collect(Collectors.toCollection(ArrayList::new));
        if ("content".equals(pathChunks.get(0))) {
            pathChunks.remove(0);
        }
        pathChunks.addAll(0, Arrays.asList("content", "dam", "eak-assistant"));
        return CoreConstants.SEPARATOR_SLASH + String.join(CoreConstants.SEPARATOR_SLASH, pathChunks);
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
            String destination = toDamUrl(args.get(QUERY_PARAMETER_TO, StringUtils.EMPTY));
            if (StringUtils.isAnyEmpty(url, destination)) {
                return Solution.from(args).withMessage(HttpStatus.SC_BAD_REQUEST, EXCEPTION_MISSING_PARAMS);
            }
            try {
                Map<String, Object> downloadResult = downloadAsset(url, entity -> store(request.getResourceResolver(), entity, destination));
                downloadResult.put(CoreConstants.PN_PATH, destination);
                return Solution.from(args).withValueMap(downloadResult);
            } catch (IOException e) {
                String exceptionMessage = String.format(EXCEPTION_COULD_NOT_IMPORT, url, destination);
                LOG.error(exceptionMessage, e);
                return Solution
                    .from(args)
                    .withMessage(HttpStatus.SC_INTERNAL_SERVER_ERROR, exceptionMessage + SEPARATOR_COLON + e.getMessage());
            }
        }
    }
}
