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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

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
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.AssetManager;
import com.day.cq.dam.api.renditions.RenditionMaker;
import com.day.cq.dam.api.renditions.RenditionTemplate;

import com.exadel.aem.toolkit.api.annotations.widgets.imageupload.ImageUploadConstants;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.assistant.AssistantConstants;
import com.exadel.aem.toolkit.core.assistant.models.facilities.Facility;
import com.exadel.aem.toolkit.core.assistant.models.facilities.SimpleFacility;
import com.exadel.aem.toolkit.core.assistant.models.solutions.Solution;
import com.exadel.aem.toolkit.core.utils.HttpClientFactory;
import com.exadel.aem.toolkit.core.utils.ThrowingConsumer;

@Component(service = AssistantService.class, immediate = true)
public class ImportService implements AssistantService {
    private static final Logger LOG = LoggerFactory.getLogger(ImportService.class);

    private static final String QUERY_PARAMETER_FROM = "from";
    private static final String QUERY_PARAMETER_TO = "to";

    private static final int THUMBNAIL_DIMENSION = 256;

    private static final String EXCEPTION_TEMPLATE = "Could not import asset from \"%s\" to \"%s\"";
    private static final String EXCEPTION_MISSING_PARAMS = "Either \"from\" or \"to\" parameter is invalid";
    private static final String EXCEPTION_MISSING_ASSET_MANAGER = "Could not obtain Asset Manager";
    private static final String EXCEPTION_INVALID_CONTENT = "Content is missing or invalid";

    private static final String SEPARATOR_COLON = ": ";
    private static final Pattern LOCALHOST_PATTERN = Pattern.compile("//localhost[.:]", Pattern.CASE_INSENSITIVE);

    private List<Facility> facilities;

    @Activate
    private void init() {
        facilities = Collections.singletonList(new Import());
    }

    @Override
    public List<Facility> getFacilities() {
        return facilities;
    }

    @Reference
    private RenditionMaker renditionMaker;

    private class Import extends SimpleFacility {

        @Override
        public String getId() {
            return "image.util.import";
        }

        @Override
        public String getIcon() {
            return ICON_TEXT_ADD;
        }

        @Override
        public boolean isAllowed(SlingHttpServletRequest request) {
            return request != null && AssistantConstants.HTTP_METHOD_POST.equals(request.getMethod());
        }

        @Override
        public Solution execute(SlingHttpServletRequest request) {
            ValueMap args = getArguments(request);
            String from = args.get(QUERY_PARAMETER_FROM, StringUtils.EMPTY);
            String to = args.get(QUERY_PARAMETER_TO, StringUtils.EMPTY);
            if (StringUtils.isAnyEmpty(from, to)) {
                return Solution.from(args).withMessage(HttpStatus.SC_BAD_REQUEST, EXCEPTION_MISSING_PARAMS);
            }
            try {
                Map<String, Object> downloadResult = downloadAsset(from, entity -> store(request.getResourceResolver(), entity, to));
                downloadResult.put(CoreConstants.PN_PATH, to);
                return Solution.from(args).withValueMap(downloadResult);
            } catch (IOException e) {
                String exceptionMessage = String.format(EXCEPTION_TEMPLATE, from, to);
                LOG.error(exceptionMessage, e);
                return Solution
                    .from(args)
                    .withMessage(HttpStatus.SC_INTERNAL_SERVER_ERROR, exceptionMessage + SEPARATOR_COLON + e.getMessage());
            }
        }

        private Map<String, Object> downloadAsset(String url, ThrowingConsumer<HttpEntity, IOException> storage) throws IOException {
            HttpGet request = new HttpGet(url);
            try (
                CloseableHttpClient client = HttpClientFactory
                    .newInstance()
                    .timeout(AssistantConstants.HTTP_TIMEOUT)
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
            }
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
                Asset asset = assetManager.createAsset(destination, entity.getContent(), getMimeType(entity), true);
                RenditionTemplate thumbnailTemplate = renditionMaker.createThumbnailTemplate(
                    asset,
                    THUMBNAIL_DIMENSION,
                    THUMBNAIL_DIMENSION,
                    true);
                renditionMaker.generateRenditions(asset, thumbnailTemplate);

            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        private String getMimeType(HttpEntity entity) {
            return Optional.ofNullable(entity.getContentType())
                .map(Header::getValue)
                .orElse(ImageUploadConstants.DEFAULT_MIME_TYPE);
        }
    }
}
