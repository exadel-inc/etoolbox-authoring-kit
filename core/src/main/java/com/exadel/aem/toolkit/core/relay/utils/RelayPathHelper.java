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
package com.exadel.aem.toolkit.core.relay.utils;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.core.CoreConstants;

/**
 * Provides utility methods for JCR path manipulation within the relay infrastructure
 * <p><u>Note</u>: This class is not a part of the public API and is subject to change. Do not use it in your own
 * code</p>
 */
public class RelayPathHelper {

    /**
     * Default (instantiation-blocking) constructor
     */
    private RelayPathHelper() {
    }

    /**
     * Gets whether the provided path is a descendant of the provided root path
     * @param path JCR path to check
     * @param root Root JCR path to check against
     * @return True or false
     */
    public static boolean isSubpath(String path, String root) {
        String normalizedPath = StringUtils.stripEnd(path, CoreConstants.SEPARATOR_SLASH);
        String normalizedRoot = StringUtils.stripEnd(StringUtils.defaultString(root), CoreConstants.SEPARATOR_SLASH);
        return StringUtils.equals(normalizedPath, normalizedRoot)
                || StringUtils.startsWith(normalizedPath, normalizedRoot + CoreConstants.SEPARATOR_SLASH);
    }

    /**
     * Replaces the {@code source} prefix in the provided path with the {@code target} prefix. Returns the original
     * path unchanged if it does not start with {@code source}
     * @param path   JCR path to transform
     * @param source Prefix to replace
     * @param target Replacement prefix
     * @return A transformed path string
     */
    public static String replace(String path, String source, String target) {
        if (StringUtils.equals(path, source)) {
            return target;
        }
        if (StringUtils.startsWith(path, source + CoreConstants.SEPARATOR_SLASH)) {
            return target + path.substring(source.length());
        }
        return path;
    }
}
