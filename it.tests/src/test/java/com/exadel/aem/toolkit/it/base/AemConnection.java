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
import java.util.Arrays;
import java.util.Base64;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;

import com.exadel.aem.toolkit.core.CoreConstants;

/**
 * Contains utility methods for handling HTTP connections to an AEM instance
 */
public class AemConnection {

    private static final String PROPERTY_HOST = "aem.host";
    private static final String PROPERTY_PORT = "aem.port";
    private static final String PROPERTY_LOGIN = "aem.login";
    private static final String PROPERTY_PASSWORD = "aem.password";

    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "4502";
    private static final String DEFAULT_LOGIN = "admin";

    private static final String ENDPOINT_URL = "http://"
        + System.getProperty(PROPERTY_HOST, DEFAULT_HOST)
        + CoreConstants.SEPARATOR_COLON
        + System.getProperty(PROPERTY_PORT, DEFAULT_PORT);

    private static final String PACKAGE_MANAGER_PATH = "/crx/packmgr/service/.json";
    private static final String LOGIN_PATH = "/libs/granite/core/content/login.html";
    private static final String LANDING_PATH = "/aem/start.html";

    /**
     * Default (instantiation-blocking) constructor
     */
    private AemConnection() {
    }

    /**
     * Attempts to open an AEM page with the given {@code path}. Waits untill the page is loaded, and the actual page
     * address matches the one specified (or, optionally, one of the additionally provided path variants). Else, throws
     * an exception
     * @param path     A string representing a relative path within an AEM server
     * @param variants Optional array of strings representing path variants that are used to certify the successful
     *                 load. E.g., if the provided path was {@code /libs/granite/core/content/login.html} but the state
     *                 of the current session is already logged in, the browser will redirect to another page:
     *                 {@code /aem/start.html}. But still if {@code /aem/start.html} was provided as a {@code variant},
     *                 the connection is considered successful
     * @throws TimeoutException if the given path was not successfully loaded
     */
    public static void open(String path, String... variants) {
        String url = getUrl(path);
        Selenide.open(url);
        Selenide.Wait().until(webDriver ->
            (webDriver.getCurrentUrl().equals(url)
                || Arrays.stream(ArrayUtils.nullToEmpty(variants)).anyMatch(v -> webDriver.getCurrentUrl().equals(getUrl(v))))
                && ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
    }

    /**
     * Attempts to log into AEM with the {@code login} and {@code password} provided as runtime properties. If login and
     * password are not provided, defaults are used
     * @throws TimeoutException if the login was not successful
     */
    public static void login() {
        open(LOGIN_PATH, LANDING_PATH);
        if (!WebDriverRunner.getWebDriver().getCurrentUrl().contains(LOGIN_PATH)) {
            return;
        }
        Selenide.element(By.name("j_username")).setValue(getProperty(PROPERTY_LOGIN));
        Selenide.element(By.name("j_password")).setValue(getProperty(PROPERTY_PASSWORD));
        Selenide.element(By.id("submit-button")).click();
        Selenide.Wait().until(webDriver -> webDriver.getCurrentUrl().contains(LANDING_PATH));
    }

    /**
     * Retrieves a connection-related property by the given name. Provides fallback values for selected properties
     * @param name String value that represents the name of the property
     * @return A non-null string value
     */
    static String getProperty(String name) {
        String fallbackValue = StringUtils.EMPTY;
        if (PROPERTY_LOGIN.equals(name) || PROPERTY_PASSWORD.equals(name)) {
            fallbackValue = DEFAULT_LOGIN;
        } else if (PROPERTY_HOST.equals(name)) {
            fallbackValue = DEFAULT_HOST;
        } else if (PROPERTY_PORT.equals(name)) {
            fallbackValue = DEFAULT_PORT;
        }
        return StringUtils.defaultIfBlank(
            System.getProperty(name, fallbackValue),
            fallbackValue);
    }

    /**
     * Retrieves the header suitable for HTTP requests that need basic authorization
     * @return String value
     */
    static String getAuthHeader() {
        String authInfo = getProperty(PROPERTY_LOGIN) + CoreConstants.SEPARATOR_COLON + getProperty(PROPERTY_PASSWORD);
        return "Basic " + Base64.getEncoder().encodeToString(authInfo.getBytes(StandardCharsets.ISO_8859_1));
    }

    /**
     * Retrieves the AEM's {@code PackageManager} endpoint including the given resource {@code suffix} and query
     * {@code params}
     * @param params URL query parameters, such as {@code ?cmd=...}
     * @param suffix URL suffix, such as the one specifying the package path
     * @return A fully qualified URL string
     */
    static String getPackageManagerUrl(String params, String suffix) {
        return StringUtils.strip(ENDPOINT_URL, CoreConstants.SEPARATOR_SLASH)
            + PACKAGE_MANAGER_PATH
            + StringUtils.defaultString(suffix)
            + params;
    }

    /**
     * Retrieves a complete URL for the given relative path
     * @param path A string representing a relative path within an AEM server
     * @return A fully qualified URL string
     */
    private static String getUrl(String path) {
        return StringUtils.stripEnd(ENDPOINT_URL, CoreConstants.SEPARATOR_SLASH)
            + CoreConstants.SEPARATOR_SLASH
            + StringUtils.stripStart(path, CoreConstants.SEPARATOR_SLASH);
    }
}
