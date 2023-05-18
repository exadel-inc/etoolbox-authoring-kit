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
package com.exadel.aem.toolkit.it.base;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.ByteArrayBody;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

class PackageInstallerUtil {

    private static final Logger LOG = LoggerFactory.getLogger(PackageInstallerUtil.class);

    private static final String FORM_FIELD_PACKAGE = "package";

    private PackageInstallerUtil() {
    }

    public static void install(Package contentPackage) throws HttpException {
        LOG.info("Uploading test package...");
        PackageManagerResponse response = executePackageRequest("?cmd=upload&force=true", null, contentPackage.toByteArray());
        if (!response.isSuccess()) {
            throw new HttpException("Could not upload test package. " + response.getMessage());
        }
        LOG.info("Installing test package...");
        contentPackage.setPath(response.getPath());
        response = executePackageRequest("?cmd=install", response.getPath());
        if (!response.isSuccess()) {
            throw new HttpException("Could not install test package. " + response.getMessage());
        }
        LOG.info("Installation complete");
    }

    public static void uninstall(Package contentPackage) throws HttpException {
        LOG.info("Uninstalling test package...");
        PackageManagerResponse response = executePackageRequest("?cmd=uninstall", contentPackage.getPath());
        if (!response.isSuccess()) {
            throw new HttpException("Could not uninstall test package. " + response.getMessage());
        }

        LOG.info("Removing test package...");
        executePackageRequest("?cmd=delete", contentPackage.getPath());
        if (!response.isSuccess()) {
            throw new HttpException("Could not remove test package. " + response.getMessage());
        }
        LOG.info("Uninstall complete");
    }

    private static PackageManagerResponse executePackageRequest(String queryParams, String suffix) throws HttpException {
        return executePackageRequest(queryParams, suffix, null);
    }

    private static PackageManagerResponse executePackageRequest(String queryParams, String suffix, byte[] payload) throws HttpException {
        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(getPostRequest(queryParams, suffix, payload))) {
            if (response.getCode() != HttpStatus.SC_OK) {
                throw new HttpException(String.format(
                    "Could not install package. Response code %s received",
                    response.getCode()));
            }
            String textResponse = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            return new ObjectMapper().readValue(
                textResponse,
                PackageManagerResponse.class);
        } catch (IOException e) {
            throw new HttpException("Could not install package. " + e.getMessage());
        }
    }

    private static HttpPost getPostRequest(String queryParams, String suffix, byte[] payload) {
        HttpPost httpPost = new HttpPost(AemConnection.getPackageManagerEndpoint(queryParams, suffix));
        if (ArrayUtils.isNotEmpty(payload)) {
            HttpEntity file = MultipartEntityBuilder.create()
                .setMode(HttpMultipartMode.EXTENDED)
                .addPart(FORM_FIELD_PACKAGE, new ByteArrayBody(payload, Package.FILE_NAME))
                .build();
            httpPost.setEntity(file);
        }
        httpPost.setHeader(HttpHeaders.AUTHORIZATION, AemConnection.AUTH_HEADER);
        return httpPost;
    }

    private static class PackageManagerResponse {

        @JsonProperty("success")
        private boolean success;

        @JsonProperty("msg")
        private String message;

        @JsonProperty("path")
        private String path;

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getPath() {
            return path;
        }
    }
}
