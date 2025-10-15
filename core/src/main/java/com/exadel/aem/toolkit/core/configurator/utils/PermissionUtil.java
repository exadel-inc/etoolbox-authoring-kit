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
package com.exadel.aem.toolkit.core.configurator.utils;

import java.util.Objects;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.Privilege;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.configurator.ConfiguratorConstants;

/**
 * Contains utility methods related to permission checking in the context of the {@code EToolbox Configurator}
 * <p><u>Note</u>: This class is not a part of the public API and is subject to change. Do not use it in your own code
 */
public class PermissionUtil {

    private static final Logger LOG = LoggerFactory.getLogger(PermissionUtil.class);

    private static final String ERROR_PERMISSIONS_FAILURE = "Could not read permissions for the current user";
    private static final String ERROR_CONFIG_NOT_FOUND = "Configuration not found at {}";

    /**
     * Default (instantiation-restricting) constructor
     */
    private PermissionUtil() {
    }

    /**
     * Checks whether the current user has global permissions to modify configurations globally
     * @param request The current request
     * @return True or false
     */
    public static boolean hasGlobalModifyPermission(HttpServletRequest request) {
        Session session = RequestUtil.getSession(request);
        try {
            return Objects.requireNonNull(session).hasPermission(ConfiguratorConstants.ROOT_PATH, Session.ACTION_SET_PROPERTY);
        } catch (RepositoryException | NullPointerException e) {
            LOG.error(ERROR_PERMISSIONS_FAILURE, e);
        }
        return false;
    }

    /**
     * Checks whether the current user has permissions to modify a specific configuration (the one identified by the
     * request suffix)
     * @param request The current request
     * @return True or false
     */
    public static boolean hasModifyPermission(HttpServletRequest request) {
        String configId = RequestUtil.getConfigPid(request);
        if (StringUtils.isBlank(configId)) {
            return hasGlobalModifyPermission(request);
        }

        Session session = RequestUtil.getSession(request);
        String resourcePath = ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + configId;
        try {
            return Objects.
                    requireNonNull(session)
                    .hasPermission(resourcePath, Session.ACTION_SET_PROPERTY);
        } catch (PathNotFoundException e) {
            LOG.debug(ERROR_CONFIG_NOT_FOUND, resourcePath);
            return hasGlobalModifyPermission(request);
        } catch (RepositoryException e) {
            LOG.error(ERROR_PERMISSIONS_FAILURE, e);
        }
        return false;
    }

    /**
     * Checks whether the current configuration (the one identified by the request suffix) has specific permissions. In
     * this case it must be treated specially in regard to user's ability to erase it, etc.
     * @param request The current request
     * @return True or false
     */
    public static boolean hasOverridingPermissions(HttpServletRequest request) {
        String configId = RequestUtil.getConfigPid(request);
        if (StringUtils.isBlank(configId)) {
            return false;
        }
        Session session = RequestUtil.getSession(request);
        String resourcePath = ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + configId;
        try {
            AccessControlManager acm = Objects.requireNonNull(session).getAccessControlManager();
            Privilege[] rootPrivileges = acm.getPrivileges(ConfiguratorConstants.ROOT_PATH);
            Privilege[] configPrivileges = acm.getPrivileges(resourcePath);
            return !Objects.deepEquals(rootPrivileges, configPrivileges);
        } catch (PathNotFoundException e) {
            LOG.debug(ERROR_CONFIG_NOT_FOUND, resourcePath);
        } catch (RepositoryException | NullPointerException e) {
            LOG.error(ERROR_PERMISSIONS_FAILURE, e);
        }
        return false;
    }

    /**
     * Checks whether the current user has permissions to replicate configurations
     * @param request The current request
     * @return True or false
     */
    public static boolean hasReplicatePermission(HttpServletRequest request) {
        ResourceResolver resolver = RequestUtil.getResourceResolver(request);
        Session session = resolver != null ? resolver.adaptTo(Session.class) : null;
        if (session == null) {
            LOG.error(ERROR_PERMISSIONS_FAILURE);
            return false;
        }
        String configId = RequestUtil.getConfigPid(request);
        if (StringUtils.isBlank(configId)) {
            return false;
        }
        String resourcePath = ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + configId;
        if (resolver.getResource(resourcePath) == null) {
            resourcePath = ConfiguratorConstants.ROOT_PATH;
        }
        try {
            AccessControlManager acm = session.getAccessControlManager();
            Privilege[] userPrivileges = acm.getPrivileges(resourcePath);
            for (Privilege privilege : userPrivileges) {
                if (StringUtils.equalsAny(privilege.getName(), "crx:replicate", "jcr:all", Privilege.JCR_ALL)) {
                    return true;
                }
            }
        } catch (RepositoryException e) {
            LOG.error(ERROR_PERMISSIONS_FAILURE, e);
        }
        return false;
    }
}
