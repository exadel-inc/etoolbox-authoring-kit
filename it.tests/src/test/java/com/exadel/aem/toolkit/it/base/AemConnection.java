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

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.core.CoreConstants;

public class AemConnection {

    private static final Logger LOG = LoggerFactory.getLogger(AemConnection.class);

    private static final String PROPERTY_ENDPOINT = "endpoint";
    private static final String PROPERTY_LOGIN = "login";
    private static final String PROPERTY_PASSWORD = "password";

    static final String LOGIN = System.getProperty(PROPERTY_LOGIN, "admin");
    static final String PASSWORD = System.getProperty(PROPERTY_PASSWORD, "admin");
    static final String AUTH_HEADER = "Basic "
        + Base64.getEncoder().encodeToString((LOGIN + CoreConstants.SEPARATOR_COLON + PASSWORD).getBytes(StandardCharsets.ISO_8859_1));

    public static final String ENDPOINT_URL = System.getProperty(PROPERTY_ENDPOINT, "http://localhost:4502");
    private static final String PACKAGE_MANAGER_PATH = "/crx/packmgr/service/.json";

    private AemConnection() {
    }

    public static String getUrl(String path) {
        return StringUtils.stripEnd(ENDPOINT_URL, CoreConstants.SEPARATOR_SLASH)
            + CoreConstants.SEPARATOR_SLASH
            + path;
    }

    static String getPackageManagerEndpoint(String params, String suffix) {
        return StringUtils.strip(ENDPOINT_URL, CoreConstants.SEPARATOR_SLASH)
            + PACKAGE_MANAGER_PATH
            + StringUtils.defaultString(suffix)
            + params;
    }
}
