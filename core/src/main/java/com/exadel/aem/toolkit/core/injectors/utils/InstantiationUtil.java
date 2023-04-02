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
package com.exadel.aem.toolkit.core.injectors.utils;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains utility methods for creating instances ob objects
 * <p><u>Note</u>: This class is not a part of the public API and is subject to change. Do not use it in your own
 * code</p>
 */
public class InstantiationUtil {

    private static final Logger LOG = LoggerFactory.getLogger(InstantiationUtil.class);

    /**
     * Default (instantiation-restricting) constructor
     */
    private InstantiationUtil() {
    }

    /**
     * Creates a new instance of the specified {@code Class}
     * @param type The class that needs to be instantiated
     * @param <T>  Instance type
     * @return New object instance, or null if the creation or initialization failed
     */
    public static <T> T getObjectInstance(Class<? extends T> type) {
        try {
            return type.getConstructor().newInstance();
        } catch (InstantiationException
                 | IllegalAccessException
                 | InvocationTargetException
                 | NoSuchMethodException ex) {
            LOG.error("Could not initialize object {}", type.getName(), ex);
        }
        return null;
    }

    /**
     * Gets an existing resource or else creates a new {@code Resource} that contains properties from the given current
     * resource filtered with a predicate
     * @param current {@code Resource} object contains properties to be filtered
     * @param prefix  {@code String} representing an optional prefix the properties are checked against when filtering
     * @param postfix {@code String} representing an optional postfix the properties are checked against when filtering
     * @return {@code Resource} instance, or null if retrieval failed
     */
    public static Resource getFilteredResource(Resource current, String prefix, String postfix) {
        if (StringUtils.isEmpty(prefix) && StringUtils.isEmpty(postfix)) {
            return current;
        }
        return new FilteredResourceDecorator(current, prefix, postfix);
    }
}
