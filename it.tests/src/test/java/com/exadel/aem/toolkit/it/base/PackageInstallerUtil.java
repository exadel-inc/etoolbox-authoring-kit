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
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Contains utility methods for installing and uninstalling synthetic content packages
 */
class PackageInstallerUtil {

    private static final Logger LOG = LoggerFactory.getLogger(PackageInstallerUtil.class);

    private static final String FORM_FIELD_PACKAGE = "package";

    /**
     * Default (instantiation-blocking) constructor
     */
    private PackageInstallerUtil() {
    }

    /**
     * Installs the provided {@link Package} to an AEM server
     * @param contentPackage {@code Package} instance
     * @throws HttpException if package installation fails
     */
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

    /**
     * Uninstalls the provided {@link Package} from an AEM server
     * @param contentPackage {@code Package} instance
     * @throws HttpException if package uninstallation fails
     */
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

    /**
     * Performs a particular package-related operation with an HTTP request
     * @param params URL query parameters, such as {@code ?cmd=...}
     * @param suffix URL suffix, such as the one specifying the package path
     * @return {@link PackageManagerResponse} object
     * @throws HttpException if the HTTP request fails
     */
    private static PackageManagerResponse executePackageRequest(String params, String suffix) throws HttpException {
        return executePackageRequest(params, suffix, null);
    }

    /**
     * Performs a particular package-related operation with an HTTP request carrying a body
     * @param params  URL query parameters, such as {@code ?cmd=...}
     * @param suffix  URL suffix, such as the one specifying the package path
     * @param payload Request condent (body)
     * @return {@link PackageManagerResponse} object
     * @throws HttpException if the HTTP request fails
     */
    private static PackageManagerResponse executePackageRequest(String params, String suffix, byte[] payload) throws HttpException {
        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(getPostRequest(params, suffix, payload))) {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new HttpException(String.format(
                    "Could not install package. Response code %s received",
                    response.getStatusLine().getStatusCode()));
            }
            String textResponse = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            return new ObjectMapper().readValue(
                textResponse,
                PackageManagerResponse.class);
        } catch (IOException e) {
            throw new HttpException("Could not install package. " + e.getMessage());
        }
    }

    /**
     * Produces a {@link HttpPost} entity with given params and body for an operation with a content package
     * @param params  URL query parameters, such as {@code ?cmd=...}
     * @param suffix  URL suffix, such as the one specifying the package path
     * @param payload Request condent (body)
     * @return {@code HttpPost} object
     */
    private static HttpPost getPostRequest(String params, String suffix, byte[] payload) {
        HttpPost httpPost = new HttpPost(AemConnection.getPackageManagerUrl(params, suffix));
        if (ArrayUtils.isNotEmpty(payload)) {
            HttpEntity file = MultipartEntityBuilder.create()
                .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                .addPart(FORM_FIELD_PACKAGE, new ByteArrayBody(payload, Package.FILE_NAME))
                .build();
            httpPost.setEntity(file);
        }
        httpPost.setHeader(HttpHeaders.AUTHORIZATION, AemConnection.getAuthHeader());
        return httpPost;
    }

    /**
     * This is a DTO entity used to parse a response from an AEM PackageManager endpoint
     */
    private static class PackageManagerResponse {

        @JsonProperty("success")
        private boolean success;

        @JsonProperty("msg")
        private String message;

        @JsonProperty("path")
        private String path;

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
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
