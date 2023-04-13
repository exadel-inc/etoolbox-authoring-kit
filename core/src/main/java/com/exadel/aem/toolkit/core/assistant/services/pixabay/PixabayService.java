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
package com.exadel.aem.toolkit.core.assistant.services.pixabay;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.assistant.models.facilities.Facility;
import com.exadel.aem.toolkit.core.assistant.models.facilities.SimpleFacility;
import com.exadel.aem.toolkit.core.assistant.models.solutions.Solution;
import com.exadel.aem.toolkit.core.assistant.services.AssistantService;
import com.exadel.aem.toolkit.core.assistant.services.writesonic.WritesonicService;
import com.exadel.aem.toolkit.core.utils.HttpClientFactory;
import com.exadel.aem.toolkit.core.utils.ObjectConversionUtil;

@Component(service = AssistantService.class, immediate = true, property = "service.ranking:Integer=102")
@Designate(ocd = PixabayServiceConfig.class)
public class PixabayService implements AssistantService {

    private static final Logger LOG = LoggerFactory.getLogger(PixabayService.class);

    private static final String VENDOR_NAME = "Pixabay";

    private static final String PN_HITS = "hits";
    private static final String PN_URL = "largeImageURL";

    private static final String EXCEPTION_EMPTY_PROMPT = "Empty prompt";
    private static final String EXCEPTION_REQUEST_FAILED = "Request to Pixabay failed";
    private static final String EXCEPTION_NO_RESULTS = "Search didn't return any results";
    private static final String EXCEPTION_TIMEOUT = "Connection to {} timed out";


    private static final String LOGO_RESOURCE = "assistant/logo-pixabay";
    private static final String LOGO;

    static {
        URL logoUrl = WritesonicService.class.getClassLoader().getResource(LOGO_RESOURCE);
        String logo = null;
        try {
            logo = logoUrl != null ? IOUtils.toString(logoUrl, StandardCharsets.UTF_8).trim() : null;
        } catch (IOException e) {
            LOG.error("Could not read resource at {}", LOGO_RESOURCE, e);
        }
        LOGO = logo;
    }

    private PixabayServiceConfig config;
    private List<Facility> facilities;

    @Activate
    @Modified
    private void init(PixabayServiceConfig config) {
        this.config = config;
        if (facilities == null) {
            facilities = Collections.singletonList(new ImagesFacility());
        }
    }

    @Override
    public String getVendorName() {
        return VENDOR_NAME;
    }

    @Override
    public String getLogo() {
        return LOGO;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public List<Facility> getFacilities() {
        return facilities;
    }

    private class ImagesFacility extends SimpleFacility {

        @Override
        public String getVendorName() {
            return VENDOR_NAME;
        }

        @Override
        public String getTitle() {
            return "Produce Image";
        }

        @Override
        public String getId() {
            return "image.produce.pixabay";
        }

        @Override
        public String getIcon() {
            return ICON_IMAGE_ADD;
        }

        @Override
        public int getRanking() {
            return 1001;
        }

        @Override
        public Solution execute(SlingHttpServletRequest request) {
            ValueMap args = getArguments(request);
            String prompt = args.get(CoreConstants.PN_TEXT, String.class);
            if (StringUtils.isBlank(prompt)) {
                return Solution.from(args).withMessage(EXCEPTION_EMPTY_PROMPT);
            }
            String encodedPrompt = prompt;
            try {
                encodedPrompt = URLEncoder.encode(prompt, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException ignored) {
                // Not likely to happen
            }
            String requestString = String.format(
                "%s?key=%s&q=%s&image_type=photo&min_width=500&min_height=500&per_page=%d",
                config.endpoint(),
                config.authKey(),
                encodedPrompt,
                config.maxOptions());
            HttpPost httpPost = new HttpPost(requestString);
            int lastExceptionStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
            String lastExceptionMessage = null;
            for (int attempt = 0; attempt < HttpClientFactory.DEFAULT_ATTEMPTS_COUNT; attempt++) {
                try (
                    CloseableHttpClient client = HttpClientFactory.newClient(HttpClientFactory.DEFAULT_TIMEOUT);
                    CloseableHttpResponse response = client.execute(httpPost)
                ) {
                    Solution solution = parseResponse(response, args);
                    EntityUtils.consume(response.getEntity());
                    return solution;
                } catch (ConnectTimeoutException | SocketTimeoutException e) {
                    LOG.warn(EXCEPTION_TIMEOUT, config.endpoint());
                    lastExceptionStatus = HttpStatus.SC_REQUEST_TIMEOUT;
                    lastExceptionMessage = e.getMessage();
                } catch (IOException e) {
                    LOG.error(EXCEPTION_REQUEST_FAILED, e);
                    lastExceptionStatus = HttpStatus.SC_BAD_GATEWAY;
                    lastExceptionMessage = e.getMessage();
                }
            }
            return Solution
                .from(args)
                .withMessage(lastExceptionStatus, StringUtils.defaultIfEmpty(lastExceptionMessage, EXCEPTION_REQUEST_FAILED));
        }

        private Solution parseResponse(CloseableHttpResponse response, ValueMap args) throws IOException {
            String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            JsonNode jsonTree = ObjectConversionUtil.toNodeTree(content);
            if (!jsonTree.has(PN_HITS) || !jsonTree.get(PN_HITS).isArray()) {
                return Solution.from(args).withMessage(EXCEPTION_NO_RESULTS);
            }
            ArrayNode hits = (ArrayNode) jsonTree.get(PN_HITS);
            List<String> options = new ArrayList<>();
            for (int i = 0; i < hits.size(); i++) {
                JsonNode next = hits.get(i);
                JsonNode largeImageUrl = next.get(PN_URL);
                if (largeImageUrl == null) {
                    continue;
                }
                options.add(largeImageUrl.asText());
            }
            return Solution.from(args).withOptions(options);
        }
    }
}
