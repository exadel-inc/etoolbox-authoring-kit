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
package com.exadel.aem.toolkit.plugin.sources;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.main.WriteMode;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.core.CoreConstants;

/**
 * Extends {@code Source} with methods specific for a Java class backing an AEM component
 */
public interface ComponentSource extends Source {

    /**
     * Retrieves the absolute path to the component folder as situated in a package. Returns {@code null} if the current
     * Java class is not a valid AEM component
     * @return A nullable string value
     */
    String getPath();

    /**
     * Retrieves the path to the component node as visible in the JCR (the one that usually starts with {@code
     * /apps...}. Returns {@code null} if the current Java class is not a valid AEM component
     * @return A nullable string value
     */
    default String getJcrPath() {
        if (!StringUtils.contains(getPath(), CoreConstants.SEPARATOR_SLASH)) {
            return getPath();
        }
        return CoreConstants.SEPARATOR_SLASH + StringUtils.substringAfter(getPath(), CoreConstants.SEPARATOR_SLASH);
    }

    /**
     * Retrieves the {@link WriteMode} of the current component. Returns {@code null} if the current Java class is not a
     * valid AEM component
     * @return A nullable {@code WriteMode} value
     */
    WriteMode getWriteMode();

    /**
     * Retrieves the collection of views (separate aspect-like Java classes) associated with the current component.
     * Returns an empty collection if the current Java class is not a valid AEM component
     * @return A non-null {@code List} instance
     */
    List<Source> getViews();

    /**
     * Gets whether the current {@code ComponentSource} instance has the path that is equal to the path provided or else
     * contains the given chunk of a path
     * @param path String value; a non-empty string is expected
     * @return True or false
     */
    boolean matches(String path);

    /**
     * Gets whether the current {@code ComponentSource} instance reflects the given {@code Class}
     * @param componentClass {@code Class} reference; a non-null value is expected
     * @return True or false
     */
    default boolean matches(Class<?> componentClass) {
        return adaptTo(Class.class).equals(componentClass);
    }
}
