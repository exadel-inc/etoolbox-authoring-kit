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
package com.exadel.aem.toolkit.core.configurator.servlets;

import java.util.Objects;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.Privilege;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.configurator.ConfiguratorConstants;

/**
 * Contains utility methods related to permission checking in the context of the {@code EToolbox Configurator}
 */
class PermissionUtil {

    private static final Logger LOG = LoggerFactory.getLogger(PermissionUtil.class);

    private static final String ERROR_PERMISSIONS_FAILURE = "Could not read permissions for the current user";

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
    public static boolean hasGlobalModifyPermission(SlingHttpServletRequest request) {
        Session session = request.getResourceResolver().adaptTo(Session.class);
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
    public static boolean hasModifyPermission(SlingHttpServletRequest request) {
        String configId = StringUtils.strip(request.getRequestPathInfo().getSuffix(), CoreConstants.SEPARATOR_SLASH);
        if (StringUtils.isBlank(configId)) {
            return hasGlobalModifyPermission(request);
        }
        Session session = request.getResourceResolver().adaptTo(Session.class);
        try {
            return Objects.
                requireNonNull(session)
                .hasPermission(
                    ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + configId,
                    Session.ACTION_SET_PROPERTY);
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
    public static boolean hasOverridingPermissions(SlingHttpServletRequest request) {
        String configId = StringUtils.strip(request.getRequestPathInfo().getSuffix(), CoreConstants.SEPARATOR_SLASH);
        if (StringUtils.isBlank(configId)) {
            return false;
        }
        Session session = request.getResourceResolver().adaptTo(Session.class);
        try {
            AccessControlManager acm = Objects.requireNonNull(session).getAccessControlManager();
            Privilege[] rootPrivileges = acm.getPrivileges(ConfiguratorConstants.ROOT_PATH);
            Privilege[] configPrivileges = acm.getPrivileges(ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + configId);
            return !Objects.deepEquals(rootPrivileges, configPrivileges);
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
    public static boolean hasReplicatePermission(SlingHttpServletRequest request) {
        Session session = request.getResourceResolver().adaptTo(Session.class);
        String configId = StringUtils.strip(request.getRequestPathInfo().getSuffix(), CoreConstants.SEPARATOR_SLASH);
        if (StringUtils.isBlank(configId)) {
            return false;
        }
        try {
            AccessControlManager acm = Objects.requireNonNull(session).getAccessControlManager();
            Privilege replicatePrivilege = acm.privilegeFromName("crx:replicate");
            Privilege[] userPrivileges = acm.getPrivileges(ConfiguratorConstants.ROOT_PATH + CoreConstants.SEPARATOR_SLASH + configId);
            for (Privilege privilege : userPrivileges) {
                if (privilege.equals(replicatePrivilege) || privilege.getName().equals(Privilege.JCR_ALL)) {
                    return true;
                }
            }
        } catch (RepositoryException | NullPointerException e) {
            LOG.error(ERROR_PERMISSIONS_FAILURE, e);
        }
        return false;
    }
}
